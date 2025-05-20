package com.skillswap.skillswapp.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.skillswap.skillswapp.data.model.Favorite;
import com.skillswap.skillswapp.data.model.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Repositorio para manejar los favoritos en Firebase Realtime Database.
 */
public class FavoriteRepository {
    private DatabaseReference databaseRef;
    private DatabaseReference favoritesRef;
    private static FavoriteRepository instance;

    private FavoriteRepository() {
        databaseRef = FirebaseDatabase.getInstance().getReference();
        favoritesRef = databaseRef.child("favorites");
    }

    public static FavoriteRepository getInstance() {
        if (instance == null) {
            instance = new FavoriteRepository();
        }
        return instance;
    }

    /**
     * Agrega un usuario a favoritos.
     */
    public MutableLiveData<Boolean> addFavorite(String userId, String favoriteUserId, String notes) {
        MutableLiveData<Boolean> addResult = new MutableLiveData<>();
        
        Favorite favorite = new Favorite(userId, favoriteUserId, notes);
        
        favoritesRef.child(userId).child(favoriteUserId).setValue(favorite.toMap())
                .addOnSuccessListener(aVoid -> addResult.setValue(true))
                .addOnFailureListener(e -> addResult.setValue(false));
        
        return addResult;
    }

    /**
     * Elimina un usuario de favoritos.
     */
    public MutableLiveData<Boolean> removeFavorite(String userId, String favoriteUserId) {
        MutableLiveData<Boolean> removeResult = new MutableLiveData<>();
        
        favoritesRef.child(userId).child(favoriteUserId).removeValue()
                .addOnSuccessListener(aVoid -> removeResult.setValue(true))
                .addOnFailureListener(e -> removeResult.setValue(false));
        
        return removeResult;
    }

    /**
     * Actualiza las notas de un favorito.
     */
    public MutableLiveData<Boolean> updateFavoriteNotes(String userId, String favoriteUserId, String notes) {
        MutableLiveData<Boolean> updateResult = new MutableLiveData<>();
        
        favoritesRef.child(userId).child(favoriteUserId).child("notes").setValue(notes)
                .addOnSuccessListener(aVoid -> updateResult.setValue(true))
                .addOnFailureListener(e -> updateResult.setValue(false));
        
        return updateResult;
    }

    /**
     * Obtiene todos los favoritos de un usuario.
     */
    public MutableLiveData<List<Favorite>> getFavoritesByUserId(String userId) {
        MutableLiveData<List<Favorite>> favoritesLiveData = new MutableLiveData<>();
        
        favoritesRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Favorite> favorites = new ArrayList<>();
                
                for (DataSnapshot favoriteSnapshot : dataSnapshot.getChildren()) {
                    try {
                        String favoriteUserId = favoriteSnapshot.getKey();
                        Date timestamp = favoriteSnapshot.child("timestamp").getValue(Date.class);
                        String notes = favoriteSnapshot.child("notes").getValue(String.class);
                        
                        Favorite favorite = new Favorite(userId, favoriteUserId, notes);
                        favorite.setTimestamp(timestamp);
                        
                        favorites.add(favorite);
                    } catch (Exception e) {
                        // Ignorar favoritos con formato incorrecto
                    }
                }
                
                favoritesLiveData.setValue(favorites);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                favoritesLiveData.setValue(new ArrayList<>());
            }
        });
        
        return favoritesLiveData;
    }

    /**
     * Verifica si un usuario está en favoritos.
     */
    public MutableLiveData<Boolean> isFavorite(String userId, String favoriteUserId) {
        MutableLiveData<Boolean> isFavoriteLiveData = new MutableLiveData<>();
        
        favoritesRef.child(userId).child(favoriteUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                isFavoriteLiveData.setValue(dataSnapshot.exists());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                isFavoriteLiveData.setValue(false);
            }
        });
        
        return isFavoriteLiveData;
    }
    
    /**
     * Obtiene los usuarios favoritos del usuario actual.
     * @param userId ID del usuario
     * @return LiveData con la lista de usuarios favoritos
     */
    public MutableLiveData<List<User>> getFavoriteUsers(String userId) {
        MutableLiveData<List<User>> usersLiveData = new MutableLiveData<>();
        UserRepository userRepository = UserRepository.getInstance();
        
        favoritesRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    usersLiveData.setValue(new ArrayList<>());
                    return;
                }
                
                List<String> favoriteUserIds = new ArrayList<>();
                for (DataSnapshot favoriteSnapshot : dataSnapshot.getChildren()) {
                    favoriteUserIds.add(favoriteSnapshot.getKey());
                }
                
                // Obtener los datos de los usuarios favoritos
                getUsersFromIds(favoriteUserIds, usersLiveData, userRepository);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                usersLiveData.setValue(new ArrayList<>());
            }
        });
        
        return usersLiveData;
    }
    
    /**
     * Obtiene los IDs de los usuarios favoritos del usuario actual.
     * @param userId ID del usuario
     * @return LiveData con el conjunto de IDs de usuarios favoritos
     */
    public MutableLiveData<Set<String>> getFavoriteUserIds(String userId) {
        MutableLiveData<Set<String>> favoriteIdsLiveData = new MutableLiveData<>();
        
        favoritesRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Set<String> favoriteIds = new HashSet<>();
                
                for (DataSnapshot favoriteSnapshot : dataSnapshot.getChildren()) {
                    favoriteIds.add(favoriteSnapshot.getKey());
                }
                
                favoriteIdsLiveData.setValue(favoriteIds);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                favoriteIdsLiveData.setValue(new HashSet<>());
            }
        });
        
        return favoriteIdsLiveData;
    }
    
    /**
     * Método auxiliar para obtener los datos de los usuarios a partir de sus IDs.
     */
    private void getUsersFromIds(List<String> userIds, MutableLiveData<List<User>> usersLiveData, UserRepository userRepository) {
        if (userIds.isEmpty()) {
            usersLiveData.setValue(new ArrayList<>());
            return;
        }
        
        List<User> users = new ArrayList<>();
        final int[] remaining = {userIds.size()};
        
        for (String userId : userIds) {
            userRepository.getUserById(userId).observeForever(user -> {
                if (user != null) {
                    // Marcar como favorito para la UI
                    user.setFavorite(true);
                    users.add(user);
                }
                
                remaining[0]--;
                if (remaining[0] == 0) {
                    usersLiveData.setValue(users);
                }
            });
        }
    }
    
    /**
     * Agrega un usuario a favoritos para el usuario actual.
     * @param favoriteUserId ID del usuario a agregar a favoritos
     * @return LiveData con el resultado (true si se agregó correctamente)
     */
    public MutableLiveData<Boolean> addFavorite(String favoriteUserId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        
        try {
            // Verificar que el usuario esté autenticado
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                result.setValue(false);
                return result;
            }
            
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            
            // Crear un objeto Favorite con los datos necesarios
            Favorite favorite = new Favorite(currentUserId, favoriteUserId, "");
            favorite.setTimestamp(new Date());
            
            // Guardar en la base de datos
            favoritesRef.child(currentUserId).child(favoriteUserId).setValue(favorite.toMap())
                    .addOnSuccessListener(aVoid -> {
                        // También guardar en la colección de usuarios para compatibilidad
                        FirebaseDatabase.getInstance().getReference()
                                .child("users")
                                .child(currentUserId)
                                .child("favorites")
                                .child(favoriteUserId)
                                .setValue(true)
                                .addOnSuccessListener(aVoid2 -> result.setValue(true))
                                .addOnFailureListener(e -> result.setValue(true)); // Aún consideramos éxito si solo se guardó en favoritesRef
                    })
                    .addOnFailureListener(e -> result.setValue(false));
        } catch (Exception e) {
            e.printStackTrace();
            result.setValue(false);
        }
        
        return result;
    }
    
    /**
     * Elimina un usuario de favoritos para el usuario actual.
     * @param favoriteUserId ID del usuario a eliminar de favoritos
     * @return LiveData con el resultado (true si se eliminó correctamente)
     */
    public MutableLiveData<Boolean> removeFavorite(String favoriteUserId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        
        try {
            // Verificar que el usuario esté autenticado
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                result.setValue(false);
                return result;
            }
            
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            
            // Eliminar de la colección de favoritos
            favoritesRef.child(currentUserId).child(favoriteUserId).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        // También eliminar de la colección de usuarios para compatibilidad
                        FirebaseDatabase.getInstance().getReference()
                                .child("users")
                                .child(currentUserId)
                                .child("favorites")
                                .child(favoriteUserId)
                                .removeValue()
                                .addOnSuccessListener(aVoid2 -> result.setValue(true))
                                .addOnFailureListener(e -> result.setValue(true)); // Aún consideramos éxito si solo se eliminó de favoritesRef
                    })
                    .addOnFailureListener(e -> result.setValue(false));
        } catch (Exception e) {
            e.printStackTrace();
            result.setValue(false);
        }
        
        return result;
    }
}
