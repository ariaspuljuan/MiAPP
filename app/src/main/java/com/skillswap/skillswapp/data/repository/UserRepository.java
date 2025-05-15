package com.skillswap.skillswapp.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.skillswap.skillswapp.data.model.User;
import com.skillswap.skillswapp.data.model.User.SkillToLearn;
import com.skillswap.skillswapp.data.model.User.SkillToTeach;
import com.skillswap.skillswapp.data.model.User.UserProfile;

import java.util.Date;
import java.util.HashMap;
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
}
