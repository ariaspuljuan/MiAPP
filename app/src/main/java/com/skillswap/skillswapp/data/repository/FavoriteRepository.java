package com.skillswap.skillswapp.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.skillswap.skillswapp.data.model.Favorite;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
}
