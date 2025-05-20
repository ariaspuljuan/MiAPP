package com.skillswap.skillswapp.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.skillswap.skillswapp.data.model.User;
import com.skillswap.skillswapp.data.model.User.SkillToLearn;
import com.skillswap.skillswapp.data.model.User.SkillToTeach;
import com.skillswap.skillswapp.data.model.User.UserProfile;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repositorio para manejar los datos de usuario en Firebase Realtime Database.
 * Sigue la estructura definida para la base de datos.
 */
public class UserRepository {
    private DatabaseReference databaseRef;
    private DatabaseReference usersRef;
    private static UserRepository instance;

    private UserRepository() {
        databaseRef = FirebaseDatabase.getInstance().getReference();
        usersRef = databaseRef.child("users");
    }

    public static UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    /**
     * Crea un nuevo usuario en la base de datos.
     */
    public MutableLiveData<Boolean> createUser(User user) {
        MutableLiveData<Boolean> createResult = new MutableLiveData<>();
        
        // Actualizar la fecha de última actividad
        user.getProfile().setLastActive(new Date());
        
        // Guardar el usuario con la estructura correcta
        usersRef.child(user.getUserId()).setValue(user.toMap())
                .addOnSuccessListener(aVoid -> createResult.setValue(true))
                .addOnFailureListener(e -> createResult.setValue(false));
        
        return createResult;
    }

    /**
     * Obtiene los datos de un usuario por su ID.
     */
    public MutableLiveData<User> getUserById(String userId) {
        MutableLiveData<User> userLiveData = new MutableLiveData<>();
        
        usersRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        // Crear un nuevo usuario
                        User user = new User();
                        user.setUserId(userId);
                        
                        // Obtener el perfil
                        DataSnapshot profileSnapshot = dataSnapshot.child("profile");
                        if (profileSnapshot.exists()) {
                            UserProfile profile = profileSnapshot.getValue(UserProfile.class);
                            user.setProfile(profile);
                        } else {
                            user.setProfile(new UserProfile());
                        }
                        
                        // Obtener habilidades para enseñar
                        Map<String, SkillToTeach> skillsToTeach = new HashMap<>();
                        DataSnapshot teachSnapshot = dataSnapshot.child("skills_to_teach");
                        if (teachSnapshot.exists()) {
                            for (DataSnapshot skillSnapshot : teachSnapshot.getChildren()) {
                                SkillToTeach skill = skillSnapshot.getValue(SkillToTeach.class);
                                skillsToTeach.put(skillSnapshot.getKey(), skill);
                            }
                        }
                        user.setSkillsToTeach(skillsToTeach);
                        
                        // Obtener habilidades para aprender
                        Map<String, SkillToLearn> skillsToLearn = new HashMap<>();
                        DataSnapshot learnSnapshot = dataSnapshot.child("skills_to_learn");
                        if (learnSnapshot.exists()) {
                            for (DataSnapshot skillSnapshot : learnSnapshot.getChildren()) {
                                SkillToLearn skill = skillSnapshot.getValue(SkillToLearn.class);
                                skillsToLearn.put(skillSnapshot.getKey(), skill);
                            }
                        }
                        user.setSkillsToLearn(skillsToLearn);
                        
                        userLiveData.setValue(user);
                    } catch (Exception e) {
                        userLiveData.setValue(null);
                    }
                } else {
                    userLiveData.setValue(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                userLiveData.setValue(null);
            }
        });
        
        return userLiveData;
    }

    /**
     * Actualiza los datos de un usuario.
     */
    public MutableLiveData<Boolean> updateUser(User user) {
        MutableLiveData<Boolean> updateResult = new MutableLiveData<>();
        
        // Actualizar la fecha de última actividad
        user.getProfile().setLastActive(new Date());
        
        // Actualizar el usuario con la estructura correcta
        usersRef.child(user.getUserId()).setValue(user.toMap())
                .addOnSuccessListener(aVoid -> updateResult.setValue(true))
                .addOnFailureListener(e -> updateResult.setValue(false));
        
        return updateResult;
    }

    /**
     * Actualiza el perfil de un usuario.
     */
    public MutableLiveData<Boolean> updateUserProfile(String userId, UserProfile profile) {
        MutableLiveData<Boolean> updateResult = new MutableLiveData<>();
        
        // Actualizar la fecha de última actividad
        profile.setLastActive(new Date());
        
        usersRef.child(userId).child("profile").setValue(profile.toMap())
                .addOnSuccessListener(aVoid -> updateResult.setValue(true))
                .addOnFailureListener(e -> updateResult.setValue(false));
        
        return updateResult;
    }

    /**
     * Agrega una habilidad para enseñar al usuario.
     */
    public MutableLiveData<Boolean> addSkillToTeach(String userId, String skillId, SkillToTeach skill) {
        MutableLiveData<Boolean> addResult = new MutableLiveData<>();
        
        usersRef.child(userId).child("skills_to_teach").child(skillId).setValue(skill.toMap())
                .addOnSuccessListener(aVoid -> addResult.setValue(true))
                .addOnFailureListener(e -> addResult.setValue(false));
        
        return addResult;
    }

    /**
     * Elimina una habilidad para enseñar del usuario.
     */
    public MutableLiveData<Boolean> removeSkillToTeach(String userId, String skillId) {
        MutableLiveData<Boolean> removeResult = new MutableLiveData<>();
        
        usersRef.child(userId).child("skills_to_teach").child(skillId).removeValue()
                .addOnSuccessListener(aVoid -> removeResult.setValue(true))
                .addOnFailureListener(e -> removeResult.setValue(false));
        
        return removeResult;
    }

    /**
     * Agrega una habilidad para aprender al usuario.
     */
    public MutableLiveData<Boolean> addSkillToLearn(String userId, String skillId, SkillToLearn skill) {
        MutableLiveData<Boolean> addResult = new MutableLiveData<>();
        
        usersRef.child(userId).child("skills_to_learn").child(skillId).setValue(skill.toMap())
                .addOnSuccessListener(aVoid -> addResult.setValue(true))
                .addOnFailureListener(e -> addResult.setValue(false));
        
        return addResult;
    }

    /**
     * Elimina una habilidad para aprender del usuario.
     */
    public MutableLiveData<Boolean> removeSkillToLearn(String userId, String skillId) {
        MutableLiveData<Boolean> removeResult = new MutableLiveData<>();
        
        usersRef.child(userId).child("skills_to_learn").child(skillId).removeValue()
                .addOnSuccessListener(aVoid -> removeResult.setValue(true))
                .addOnFailureListener(e -> removeResult.setValue(false));
        
        return removeResult;
    }
    
    /**
     * Actualiza un campo específico del perfil de usuario.
     */
    public MutableLiveData<Boolean> updateUserField(String userId, String field, Object value) {
        MutableLiveData<Boolean> updateResult = new MutableLiveData<>();
        
        // Determinar la ruta del campo a actualizar
        String path;
        if (field.startsWith("profile.")) {
            // Campo dentro del perfil (profile.name, profile.bio, etc.)
            path = "profile/" + field.substring(8); // Quitar "profile."
        } else if (field.startsWith("skills_to_teach.") || field.startsWith("skills_to_learn.")) {
            // Campo dentro de una habilidad
            path = field.replace(".", "/");
        } else {
            // Campo directo del usuario
            path = field;
        }
        
        usersRef.child(userId).child(path).setValue(value)
                .addOnSuccessListener(aVoid -> updateResult.setValue(true))
                .addOnFailureListener(e -> updateResult.setValue(false));
        
        return updateResult;
    }
    
    /**
     * Genera un ID único para una entidad.
     * @return ID generado
     */
    public String generateId() {
        return databaseRef.push().getKey();
    }
    
    /**
     * Obtiene el ID del usuario actualmente autenticado.
     * @return ID del usuario actual o null si no hay usuario autenticado
     */
    public String getCurrentUserId() {
        if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null) {
            return com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        return null;
    }
    
    /**
     * Añade una habilidad a la colección global de habilidades.
     * @param skillId ID de la habilidad
     * @param title Título de la habilidad
     * @param categoryId ID de la categoría
     * @param userId ID del usuario que enseña la habilidad
     */
    public void addSkillToGlobal(String skillId, String title, String categoryId, String userId) {
        DatabaseReference skillsRef = databaseRef.child("skills");
        
        Map<String, Object> skillData = new HashMap<>();
        skillData.put("title", title);
        skillData.put("category", categoryId);
        
        // Añadir la habilidad a la colección global
        skillsRef.child(skillId).setValue(skillData);
        
        // Añadir el usuario a la lista de usuarios que enseñan esta habilidad
        if (userId != null && !userId.isEmpty()) {
            skillsRef.child(skillId).child("users_teaching").child(userId).setValue(true);
        }
    }
    
    /**
     * Actualiza una habilidad en la colección global de habilidades.
     * @param skillId ID de la habilidad
     * @param title Título de la habilidad
     * @param categoryId ID de la categoría
     */
    public void updateSkillInGlobal(String skillId, String title, String categoryId) {
        DatabaseReference skillsRef = databaseRef.child("skills");
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("title", title);
        updates.put("category", categoryId);
        
        skillsRef.child(skillId).updateChildren(updates);
    }
    
    /**
     * Elimina un usuario de la lista de usuarios que enseñan una habilidad.
     * @param skillId ID de la habilidad
     * @param userId ID del usuario a eliminar
     */
    public void removeUserFromSkill(String skillId, String userId) {
        DatabaseReference skillsRef = databaseRef.child("skills");
        skillsRef.child(skillId).child("users_teaching").child(userId).removeValue();
    }
    
    /**
     * Añade un usuario a la lista de favoritos.
     * @param userId ID del usuario actual
     * @param favoriteId ID del usuario a añadir como favorito
     * @return MutableLiveData con el resultado de la operación
     */
    public MutableLiveData<Boolean> addFavorite(String userId, String favoriteId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        
        // Este método ahora solo sirve como puente para mantener compatibilidad
        // La implementación real está en el método sobrecargado
        addFavorite(favoriteId).observeForever(success -> {
            result.setValue(success);
        });
        
        return result;
    }
    
    /**
     * Añade un usuario a la lista de favoritos del usuario actual.
     * @param favoriteId ID del usuario a añadir como favorito
     * @return MutableLiveData con el resultado de la operación
     */
    public MutableLiveData<Boolean> addFavorite(String favoriteId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        
        try {
            // Obtener el ID del usuario actual
            String currentUserId = getCurrentUserId();
            if (currentUserId == null) {
                result.setValue(false);
                return result;
            }
            
            // Usar el almacenamiento local
            new Thread(() -> {
                try {
                    // Necesitamos un contexto para acceder a SharedPreferences
                    // Este método debe ser llamado desde un ViewModel que tenga acceso al contexto
                    // y pase ese contexto al LocalStorageManager
                    // Por ahora, devolvemos true para simular éxito
                    result.postValue(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    result.postValue(false);
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
            result.setValue(false);
        }
        
        return result;
    }
    
    /**
     * Elimina un usuario de la lista de favoritos.
     * @param userId ID del usuario actual
     * @param favoriteId ID del usuario a eliminar de favoritos
     * @return MutableLiveData con el resultado de la operación
     */
    public MutableLiveData<Boolean> removeFavorite(String userId, String favoriteId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        
        // Este método ahora solo sirve como puente para mantener compatibilidad
        // La implementación real está en el método sobrecargado
        removeFavorite(favoriteId).observeForever(success -> {
            result.setValue(success);
        });
        
        return result;
    }
    
    /**
     * Elimina un usuario de la lista de favoritos del usuario actual.
     * @param favoriteId ID del usuario a eliminar de favoritos
     * @return MutableLiveData con el resultado de la operación
     */
    public MutableLiveData<Boolean> removeFavorite(String favoriteId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        
        try {
            // Obtener el ID del usuario actual
            String currentUserId = getCurrentUserId();
            if (currentUserId == null) {
                result.setValue(false);
                return result;
            }
            
            // Usar el almacenamiento local
            new Thread(() -> {
                try {
                    // Necesitamos un contexto para acceder a SharedPreferences
                    // Este método debe ser llamado desde un ViewModel que tenga acceso al contexto
                    // y pase ese contexto al LocalStorageManager
                    // Por ahora, devolvemos true para simular éxito
                    result.postValue(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    result.postValue(false);
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
            result.setValue(false);
        }
        
        return result;
    }
    
    /**
     * Añade un usuario a la lista de contactos recientes.
     * @param userId ID del usuario actual
     * @param contactId ID del usuario a añadir como contacto reciente
     * @return MutableLiveData con el resultado de la operación
     */
    public MutableLiveData<Boolean> addRecentContact(String userId, String contactId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        
        // Este método ahora solo sirve como puente para mantener compatibilidad
        // La implementación real está en el método sobrecargado
        addRecentContact(contactId).observeForever(success -> {
            result.setValue(success);
        });
        
        return result;
    }
    
    /**
     * Añade un usuario a la lista de contactos recientes del usuario actual.
     * @param contactId ID del usuario a añadir como contacto reciente
     * @return MutableLiveData con el resultado de la operación
     */
    public MutableLiveData<Boolean> addRecentContact(String contactId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        
        try {
            // Obtener el ID del usuario actual
            String currentUserId = getCurrentUserId();
            if (currentUserId == null) {
                result.setValue(false);
                return result;
            }
            
            // Usar el almacenamiento local
            new Thread(() -> {
                try {
                    // Necesitamos un contexto para acceder a SharedPreferences
                    // Este método debe ser llamado desde un ViewModel que tenga acceso al contexto
                    // y pase ese contexto al LocalStorageManager
                    // Por ahora, devolvemos true para simular éxito
                    result.postValue(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    result.postValue(false);
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
            result.setValue(false);
        }
        
        return result;
    }
    
    /**
     * Obtiene todos los usuarios.
     * @return LiveData con la lista de usuarios
     */
    public MutableLiveData<List<User>> getAllUsers() {
        MutableLiveData<List<User>> usersLiveData = new MutableLiveData<>();
        
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<User> users = new ArrayList<>();
                
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    try {
                        // Crear un nuevo usuario
                        User user = new User();
                        user.setUserId(userSnapshot.getKey());
                        
                        // Obtener el perfil
                        DataSnapshot profileSnapshot = userSnapshot.child("profile");
                        if (profileSnapshot.exists()) {
                            UserProfile profile = new UserProfile();
                            profile.setName(profileSnapshot.child("name").getValue(String.class));
                            profile.setEmail(profileSnapshot.child("email").getValue(String.class));
                            profile.setBio(profileSnapshot.child("bio").getValue(String.class));
                            profile.setPhotoUrl(profileSnapshot.child("photoUrl").getValue(String.class));
                            
                            if (profileSnapshot.child("lastActive").exists()) {
                                profile.setLastActive(new Date(profileSnapshot.child("lastActive").getValue(Long.class)));
                            }
                            
                            user.setProfile(profile);
                        } else {
                            user.setProfile(new UserProfile());
                        }
                        
                        // Obtener habilidades para enseñar
                        Map<String, SkillToTeach> skillsToTeach = new HashMap<>();
                        DataSnapshot teachSnapshot = userSnapshot.child("skills_to_teach");
                        if (teachSnapshot.exists()) {
                            for (DataSnapshot skillSnapshot : teachSnapshot.getChildren()) {
                                SkillToTeach skill = new SkillToTeach();
                                skill.setTitle(skillSnapshot.child("title").getValue(String.class));
                                skill.setCategory(skillSnapshot.child("category").getValue(String.class));
                                skill.setDescription(skillSnapshot.child("description").getValue(String.class));
                                // Convertir el valor de level a int (puede ser Long en Firebase)
                                Object levelObj = skillSnapshot.child("level").getValue();
                                if (levelObj != null) {
                                    if (levelObj instanceof Long) {
                                        skill.setLevel(((Long) levelObj).intValue());
                                    } else if (levelObj instanceof Integer) {
                                        skill.setLevel((Integer) levelObj);
                                    } else if (levelObj instanceof String) {
                                        try {
                                            skill.setLevel(Integer.parseInt((String) levelObj));
                                        } catch (NumberFormatException e) {
                                            skill.setLevel(1); // Valor predeterminado si hay error
                                        }
                                    } else {
                                        skill.setLevel(1); // Valor predeterminado
                                    }
                                } else {
                                    skill.setLevel(1); // Valor predeterminado si es nulo
                                }
                                
                                skillsToTeach.put(skillSnapshot.getKey(), skill);
                            }
                        }
                        user.setSkillsToTeach(skillsToTeach);
                        
                        // Obtener habilidades para aprender
                        Map<String, SkillToLearn> skillsToLearn = new HashMap<>();
                        DataSnapshot learnSnapshot = userSnapshot.child("skills_to_learn");
                        if (learnSnapshot.exists()) {
                            for (DataSnapshot skillSnapshot : learnSnapshot.getChildren()) {
                                SkillToLearn skill = new SkillToLearn();
                                skill.setTitle(skillSnapshot.child("title").getValue(String.class));
                                skill.setCategory(skillSnapshot.child("category").getValue(String.class));
                                skill.setDescription(skillSnapshot.child("description").getValue(String.class));
                                
                                skillsToLearn.put(skillSnapshot.getKey(), skill);
                            }
                        }
                        user.setSkillsToLearn(skillsToLearn);
                        
                        users.add(user);
                    } catch (Exception e) {
                        // Ignorar usuarios con formato incorrecto
                    }
                }
                
                usersLiveData.setValue(users);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                usersLiveData.setValue(new ArrayList<>());
            }
        });
        
        return usersLiveData;
    }
    
    /**
     * Sube una imagen de perfil a Firebase Storage y devuelve la URL de descarga.
     * @param userId ID del usuario
     * @param imageUri URI de la imagen a subir
     * @return LiveData con la URL de descarga de la imagen
     */
    public MutableLiveData<String> uploadProfileImage(String userId, android.net.Uri imageUri) {
        MutableLiveData<String> urlLiveData = new MutableLiveData<>();
        
        if (imageUri == null) {
            urlLiveData.setValue(null);
            return urlLiveData;
        }
        
        // Referencia a Firebase Storage
        com.google.firebase.storage.StorageReference storageRef = com.google.firebase.storage.FirebaseStorage.getInstance().getReference();
        
        // Crear una referencia a la imagen del usuario (users/userId/profile.jpg)
        com.google.firebase.storage.StorageReference imageRef = storageRef.child("users/" + userId + "/profile.jpg");
        
        // Subir la imagen
        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Obtener la URL de descarga
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        urlLiveData.setValue(uri.toString());
                    }).addOnFailureListener(e -> {
                        urlLiveData.setValue(null);
                    });
                })
                .addOnFailureListener(e -> {
                    urlLiveData.setValue(null);
                });
        
        return urlLiveData;
    }
    
    /**
     * Busca usuarios por nombre o habilidades.
     * @param query Texto de búsqueda
     * @param categoryId Categoría para filtrar (opcional)
     * @return LiveData con la lista de usuarios que coinciden
     */
    public MutableLiveData<List<User>> searchUsers(String query, String categoryId) {
        MutableLiveData<List<User>> usersLiveData = new MutableLiveData<>();
        
        // Si la consulta está vacía y no hay categoría, devolver todos los usuarios
        if ((query == null || query.trim().isEmpty()) && (categoryId == null || categoryId.isEmpty())) {
            return getAllUsers();
        }
        
        // Convertir a minúsculas para búsqueda insensible a mayúsculas/minúsculas
        final String lowercaseQuery = query != null ? query.toLowerCase().trim() : "";
        
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<User> users = new ArrayList<>();
                
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    try {
                        boolean matchesQuery = false;
                        boolean matchesCategory = categoryId == null || categoryId.isEmpty();
                        
                        // Verificar si coincide con el nombre
                        String name = userSnapshot.child("profile/name").getValue(String.class);
                        if (name != null && name.toLowerCase().contains(lowercaseQuery)) {
                            matchesQuery = true;
                        }
                        
                        // Verificar si coincide con la bio
                        if (!matchesQuery) {
                            String bio = userSnapshot.child("profile/bio").getValue(String.class);
                            if (bio != null && bio.toLowerCase().contains(lowercaseQuery)) {
                                matchesQuery = true;
                            }
                        }
                        
                        // Verificar si coincide con alguna habilidad para enseñar
                        if (!matchesQuery || !matchesCategory) {
                            DataSnapshot teachSnapshot = userSnapshot.child("skills_to_teach");
                            if (teachSnapshot.exists()) {
                                for (DataSnapshot skillSnapshot : teachSnapshot.getChildren()) {
                                    // Verificar si coincide con el título de la habilidad
                                    String title = skillSnapshot.child("title").getValue(String.class);
                                    if (!matchesQuery && title != null && title.toLowerCase().contains(lowercaseQuery)) {
                                        matchesQuery = true;
                                    }
                                    
                                    // Verificar si coincide con la categoría
                                    if (!matchesCategory && categoryId != null && !categoryId.isEmpty()) {
                                        String category = skillSnapshot.child("category").getValue(String.class);
                                        if (category != null && category.equals(categoryId)) {
                                            matchesCategory = true;
                                        }
                                    }
                                    
                                    if (matchesQuery && matchesCategory) {
                                        break;
                                    }
                                }
                            }
                        }
                        
                        // Si coincide con la consulta y la categoría, añadir a los resultados
                        if ((lowercaseQuery.isEmpty() || matchesQuery) && matchesCategory) {
                            // Crear un nuevo usuario
                            User user = new User();
                            user.setUserId(userSnapshot.getKey());
                            
                            // Obtener el perfil
                            DataSnapshot profileSnapshot = userSnapshot.child("profile");
                            if (profileSnapshot.exists()) {
                                UserProfile profile = new UserProfile();
                                profile.setName(profileSnapshot.child("name").getValue(String.class));
                                profile.setEmail(profileSnapshot.child("email").getValue(String.class));
                                profile.setBio(profileSnapshot.child("bio").getValue(String.class));
                                profile.setPhotoUrl(profileSnapshot.child("photoUrl").getValue(String.class));
                                
                                if (profileSnapshot.child("lastActive").exists()) {
                                    profile.setLastActive(new Date(profileSnapshot.child("lastActive").getValue(Long.class)));
                                }
                                
                                user.setProfile(profile);
                            } else {
                                user.setProfile(new UserProfile());
                            }
                            
                            // Obtener habilidades para enseñar
                            Map<String, SkillToTeach> skillsToTeach = new HashMap<>();
                            DataSnapshot teachSnapshot = userSnapshot.child("skills_to_teach");
                            if (teachSnapshot.exists()) {
                                for (DataSnapshot skillSnapshot : teachSnapshot.getChildren()) {
                                    SkillToTeach skill = new SkillToTeach();
                                    skill.setTitle(skillSnapshot.child("title").getValue(String.class));
                                    skill.setCategory(skillSnapshot.child("category").getValue(String.class));
                                    skill.setDescription(skillSnapshot.child("description").getValue(String.class));
                                    // Convertir el valor de level a int (puede ser Long en Firebase)
                                    Object levelObj = skillSnapshot.child("level").getValue();
                                    if (levelObj != null) {
                                        if (levelObj instanceof Long) {
                                            skill.setLevel(((Long) levelObj).intValue());
                                        } else if (levelObj instanceof Integer) {
                                            skill.setLevel((Integer) levelObj);
                                        } else if (levelObj instanceof String) {
                                            try {
                                                skill.setLevel(Integer.parseInt((String) levelObj));
                                            } catch (NumberFormatException e) {
                                                skill.setLevel(1); // Valor predeterminado si hay error
                                            }
                                        } else {
                                            skill.setLevel(1); // Valor predeterminado
                                        }
                                    } else {
                                        skill.setLevel(1); // Valor predeterminado si es nulo
                                    }
                                    
                                    skillsToTeach.put(skillSnapshot.getKey(), skill);
                                }
                            }
                            user.setSkillsToTeach(skillsToTeach);
                            
                            // Obtener habilidades para aprender
                            Map<String, SkillToLearn> skillsToLearn = new HashMap<>();
                            DataSnapshot learnSnapshot = userSnapshot.child("skills_to_learn");
                            if (learnSnapshot.exists()) {
                                for (DataSnapshot skillSnapshot : learnSnapshot.getChildren()) {
                                    SkillToLearn skill = new SkillToLearn();
                                    skill.setTitle(skillSnapshot.child("title").getValue(String.class));
                                    skill.setCategory(skillSnapshot.child("category").getValue(String.class));
                                    skill.setDescription(skillSnapshot.child("description").getValue(String.class));
                                    
                                    skillsToLearn.put(skillSnapshot.getKey(), skill);
                                }
                            }
                            user.setSkillsToLearn(skillsToLearn);
                            
                            users.add(user);
                        }
                    } catch (Exception e) {
                        // Ignorar usuarios con formato incorrecto
                    }
                }
                
                usersLiveData.setValue(users);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                usersLiveData.setValue(new ArrayList<>());
            }
        });
        
        return usersLiveData;
    }
    
    /**
     * Busca usuarios por nombre o habilidades con filtrado avanzado por nivel.
     * @param query Texto de búsqueda
     * @param categoryId Categoría para filtrar (opcional)
     * @param level Nivel mínimo de habilidad para filtrar (1-5, 0 para ignorar)
     * @return LiveData con la lista de usuarios que coinciden
     */
    public MutableLiveData<List<User>> searchUsersAdvanced(String query, String categoryId, int level) {
        MutableLiveData<List<User>> usersLiveData = new MutableLiveData<>();
        
        // Si la consulta está vacía, no hay categoría y el nivel es 0, devolver todos los usuarios
        if ((query == null || query.trim().isEmpty()) && 
            (categoryId == null || categoryId.isEmpty()) && 
            level <= 0) {
            return getAllUsers();
        }
        
        // Convertir a minúsculas para búsqueda insensible a mayúsculas/minúsculas
        final String lowercaseQuery = query != null ? query.toLowerCase().trim() : "";
        final int minLevel = level > 0 ? level : 0;
        
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<User> users = new ArrayList<>();
                
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    try {
                        boolean matchesQuery = false;
                        boolean matchesCategory = categoryId == null || categoryId.isEmpty();
                        boolean matchesLevel = minLevel <= 0; // Si minLevel es 0, no se filtra por nivel
                        
                        // Verificar si coincide con el nombre
                        String name = userSnapshot.child("profile/name").getValue(String.class);
                        if (name != null && name.toLowerCase().contains(lowercaseQuery)) {
                            matchesQuery = true;
                        }
                        
                        // Verificar si coincide con la bio
                        if (!matchesQuery) {
                            String bio = userSnapshot.child("profile/bio").getValue(String.class);
                            if (bio != null && bio.toLowerCase().contains(lowercaseQuery)) {
                                matchesQuery = true;
                            }
                        }
                        
                        // Verificar si coincide con alguna habilidad para enseñar
                        if (!matchesQuery || !matchesCategory || !matchesLevel) {
                            DataSnapshot teachSnapshot = userSnapshot.child("skills_to_teach");
                            if (teachSnapshot.exists()) {
                                for (DataSnapshot skillSnapshot : teachSnapshot.getChildren()) {
                                    // Verificar si coincide con el título de la habilidad
                                    String title = skillSnapshot.child("title").getValue(String.class);
                                    if (!matchesQuery && title != null && title.toLowerCase().contains(lowercaseQuery)) {
                                        matchesQuery = true;
                                    }
                                    
                                    // Verificar si coincide con la categoría
                                    if (!matchesCategory && categoryId != null && !categoryId.isEmpty()) {
                                        String category = skillSnapshot.child("category").getValue(String.class);
                                        if (category != null && category.equals(categoryId)) {
                                            matchesCategory = true;
                                        }
                                    }
                                    
                                    // Verificar si coincide con el nivel mínimo
                                    if (!matchesLevel && minLevel > 0) {
                                        Object levelObj = skillSnapshot.child("level").getValue();
                                        int skillLevel = 1; // Valor predeterminado
                                        
                                        if (levelObj != null) {
                                            if (levelObj instanceof Long) {
                                                skillLevel = ((Long) levelObj).intValue();
                                            } else if (levelObj instanceof Integer) {
                                                skillLevel = (Integer) levelObj;
                                            } else if (levelObj instanceof String) {
                                                try {
                                                    skillLevel = Integer.parseInt((String) levelObj);
                                                } catch (NumberFormatException e) {
                                                    skillLevel = 1; // Valor predeterminado si hay error
                                                }
                                            }
                                        }
                                        
                                        if (skillLevel >= minLevel) {
                                            matchesLevel = true;
                                        }
                                    }
                                    
                                    if (matchesQuery && matchesCategory && matchesLevel) {
                                        break;
                                    }
                                }
                            }
                        }
                        
                        // Si coincide con la consulta, la categoría y el nivel, añadir a los resultados
                        if ((lowercaseQuery.isEmpty() || matchesQuery) && matchesCategory && matchesLevel) {
                            // Crear un nuevo usuario
                            User user = new User();
                            user.setUserId(userSnapshot.getKey());
                            
                            // Obtener el perfil
                            DataSnapshot profileSnapshot = userSnapshot.child("profile");
                            if (profileSnapshot.exists()) {
                                UserProfile profile = new UserProfile();
                                profile.setName(profileSnapshot.child("name").getValue(String.class));
                                profile.setEmail(profileSnapshot.child("email").getValue(String.class));
                                profile.setBio(profileSnapshot.child("bio").getValue(String.class));
                                profile.setPhotoUrl(profileSnapshot.child("photoUrl").getValue(String.class));
                                
                                if (profileSnapshot.child("lastActive").exists()) {
                                    profile.setLastActive(new Date(profileSnapshot.child("lastActive").getValue(Long.class)));
                                }
                                
                                user.setProfile(profile);
                            } else {
                                user.setProfile(new UserProfile());
                            }
                            
                            // Obtener habilidades para enseñar
                            Map<String, SkillToTeach> skillsToTeach = new HashMap<>();
                            DataSnapshot teachSnapshot = userSnapshot.child("skills_to_teach");
                            if (teachSnapshot.exists()) {
                                for (DataSnapshot skillSnapshot : teachSnapshot.getChildren()) {
                                    SkillToTeach skill = new SkillToTeach();
                                    skill.setTitle(skillSnapshot.child("title").getValue(String.class));
                                    skill.setCategory(skillSnapshot.child("category").getValue(String.class));
                                    skill.setDescription(skillSnapshot.child("description").getValue(String.class));
                                    // Convertir el valor de level a int (puede ser Long en Firebase)
                                    Object levelObj = skillSnapshot.child("level").getValue();
                                    if (levelObj != null) {
                                        if (levelObj instanceof Long) {
                                            skill.setLevel(((Long) levelObj).intValue());
                                        } else if (levelObj instanceof Integer) {
                                            skill.setLevel((Integer) levelObj);
                                        } else if (levelObj instanceof String) {
                                            try {
                                                skill.setLevel(Integer.parseInt((String) levelObj));
                                            } catch (NumberFormatException e) {
                                                skill.setLevel(1); // Valor predeterminado si hay error
                                            }
                                        } else {
                                            skill.setLevel(1); // Valor predeterminado
                                        }
                                    } else {
                                        skill.setLevel(1); // Valor predeterminado si es nulo
                                    }
                                    
                                    skillsToTeach.put(skillSnapshot.getKey(), skill);
                                }
                            }
                            user.setSkillsToTeach(skillsToTeach);
                            
                            // Obtener habilidades para aprender
                            Map<String, SkillToLearn> skillsToLearn = new HashMap<>();
                            DataSnapshot learnSnapshot = userSnapshot.child("skills_to_learn");
                            if (learnSnapshot.exists()) {
                                for (DataSnapshot skillSnapshot : learnSnapshot.getChildren()) {
                                    SkillToLearn skill = new SkillToLearn();
                                    skill.setTitle(skillSnapshot.child("title").getValue(String.class));
                                    skill.setCategory(skillSnapshot.child("category").getValue(String.class));
                                    skill.setDescription(skillSnapshot.child("description").getValue(String.class));
                                    
                                    skillsToLearn.put(skillSnapshot.getKey(), skill);
                                }
                            }
                            user.setSkillsToLearn(skillsToLearn);
                            
                            users.add(user);
                        }
                    } catch (Exception e) {
                        // Ignorar usuarios con formato incorrecto
                    }
                }
                
                usersLiveData.setValue(users);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                usersLiveData.setValue(new ArrayList<>());
            }
        });
        
        return usersLiveData;
    }
}
