package com.skillswap.skillswapp.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.skillswap.skillswapp.data.model.Skill;

import java.util.ArrayList;
import java.util.List;

/**
 * Repositorio para manejar las habilidades en Firebase Realtime Database.
 */
public class SkillRepository {
    private DatabaseReference databaseRef;
    private DatabaseReference skillsRef;
    private static SkillRepository instance;

    private SkillRepository() {
        databaseRef = FirebaseDatabase.getInstance().getReference();
        skillsRef = databaseRef.child("skills");
    }

    public static SkillRepository getInstance() {
        if (instance == null) {
            instance = new SkillRepository();
        }
        return instance;
    }

    /**
     * Crea o actualiza una habilidad en la base de datos.
     */
    public MutableLiveData<Boolean> saveSkill(Skill skill) {
        MutableLiveData<Boolean> saveResult = new MutableLiveData<>();
        
        // Si no tiene ID, generar uno nuevo
        if (skill.getSkillId() == null || skill.getSkillId().isEmpty()) {
            skill.setSkillId(skillsRef.push().getKey());
        }
        
        skillsRef.child(skill.getSkillId()).setValue(skill.toMap())
                .addOnSuccessListener(aVoid -> saveResult.setValue(true))
                .addOnFailureListener(e -> saveResult.setValue(false));
        
        return saveResult;
    }

    /**
     * Obtiene una habilidad por su ID.
     */
    public MutableLiveData<Skill> getSkillById(String skillId) {
        MutableLiveData<Skill> skillLiveData = new MutableLiveData<>();
        
        skillsRef.child(skillId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        Skill skill = new Skill();
                        skill.setSkillId(dataSnapshot.getKey());
                        skill.setTitle(dataSnapshot.child("title").getValue(String.class));
                        skill.setCategory(dataSnapshot.child("category").getValue(String.class));
                        
                        // Obtener la lista de usuarios que enseñan esta habilidad
                        List<String> usersTeaching = new ArrayList<>();
                        if (dataSnapshot.child("users_teaching").exists()) {
                            for (DataSnapshot userSnapshot : dataSnapshot.child("users_teaching").getChildren()) {
                                usersTeaching.add(userSnapshot.getValue(String.class));
                            }
                        }
                        skill.setUsersTeaching(usersTeaching);
                        
                        skillLiveData.setValue(skill);
                    } catch (Exception e) {
                        skillLiveData.setValue(null);
                    }
                } else {
                    skillLiveData.setValue(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                skillLiveData.setValue(null);
            }
        });
        
        return skillLiveData;
    }

    /**
     * Obtiene todas las habilidades.
     */
    public MutableLiveData<List<Skill>> getAllSkills() {
        MutableLiveData<List<Skill>> skillsLiveData = new MutableLiveData<>();
        
        skillsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Skill> skills = new ArrayList<>();
                
                for (DataSnapshot skillSnapshot : dataSnapshot.getChildren()) {
                    try {
                        Skill skill = new Skill();
                        skill.setSkillId(skillSnapshot.getKey());
                        skill.setTitle(skillSnapshot.child("title").getValue(String.class));
                        skill.setCategory(skillSnapshot.child("category").getValue(String.class));
                        
                        // Obtener la lista de usuarios que enseñan esta habilidad
                        List<String> usersTeaching = new ArrayList<>();
                        if (skillSnapshot.child("users_teaching").exists()) {
                            for (DataSnapshot userSnapshot : skillSnapshot.child("users_teaching").getChildren()) {
                                usersTeaching.add(userSnapshot.getValue(String.class));
                            }
                        }
                        skill.setUsersTeaching(usersTeaching);
                        
                        skills.add(skill);
                    } catch (Exception e) {
                        // Ignorar habilidades con formato incorrecto
                    }
                }
                
                skillsLiveData.setValue(skills);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                skillsLiveData.setValue(new ArrayList<>());
            }
        });
        
        return skillsLiveData;
    }

    /**
     * Busca habilidades por título o categoría.
     */
    public MutableLiveData<List<Skill>> searchSkills(String query) {
        MutableLiveData<List<Skill>> skillsLiveData = new MutableLiveData<>();
        
        // Convertir a minúsculas para búsqueda insensible a mayúsculas/minúsculas
        String lowercaseQuery = query.toLowerCase();
        
        skillsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Skill> skills = new ArrayList<>();
                
                for (DataSnapshot skillSnapshot : dataSnapshot.getChildren()) {
                    try {
                        String title = skillSnapshot.child("title").getValue(String.class);
                        String category = skillSnapshot.child("category").getValue(String.class);
                        
                        // Verificar si el título o la categoría contienen la consulta
                        if ((title != null && title.toLowerCase().contains(lowercaseQuery)) ||
                            (category != null && category.toLowerCase().contains(lowercaseQuery))) {
                            
                            Skill skill = new Skill();
                            skill.setSkillId(skillSnapshot.getKey());
                            skill.setTitle(title);
                            skill.setCategory(category);
                            
                            // Obtener la lista de usuarios que enseñan esta habilidad
                            List<String> usersTeaching = new ArrayList<>();
                            if (skillSnapshot.child("users_teaching").exists()) {
                                for (DataSnapshot userSnapshot : skillSnapshot.child("users_teaching").getChildren()) {
                                    usersTeaching.add(userSnapshot.getValue(String.class));
                                }
                            }
                            skill.setUsersTeaching(usersTeaching);
                            
                            skills.add(skill);
                        }
                    } catch (Exception e) {
                        // Ignorar habilidades con formato incorrecto
                    }
                }
                
                skillsLiveData.setValue(skills);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                skillsLiveData.setValue(new ArrayList<>());
            }
        });
        
        return skillsLiveData;
    }

    /**
     * Agrega un usuario a la lista de usuarios que enseñan una habilidad.
     */
    public MutableLiveData<Boolean> addUserTeaching(String skillId, String userId) {
        MutableLiveData<Boolean> addResult = new MutableLiveData<>();
        
        // Primero obtenemos la habilidad para verificar si el usuario ya está en la lista
        getSkillById(skillId).observeForever(skill -> {
            if (skill != null) {
                List<String> usersTeaching = skill.getUsersTeaching();
                
                // Verificar si el usuario ya está en la lista
                if (!usersTeaching.contains(userId)) {
                    usersTeaching.add(userId);
                    skill.setUsersTeaching(usersTeaching);
                    
                    // Actualizar la habilidad en la base de datos
                    skillsRef.child(skillId).child("users_teaching").setValue(usersTeaching)
                            .addOnSuccessListener(aVoid -> addResult.setValue(true))
                            .addOnFailureListener(e -> addResult.setValue(false));
                } else {
                    // El usuario ya está en la lista
                    addResult.setValue(true);
                }
            } else {
                // La habilidad no existe
                addResult.setValue(false);
            }
        });
        
        return addResult;
    }

    /**
     * Elimina un usuario de la lista de usuarios que enseñan una habilidad.
     */
    public MutableLiveData<Boolean> removeUserTeaching(String skillId, String userId) {
        MutableLiveData<Boolean> removeResult = new MutableLiveData<>();
        
        // Primero obtenemos la habilidad para verificar si el usuario está en la lista
        getSkillById(skillId).observeForever(skill -> {
            if (skill != null) {
                List<String> usersTeaching = skill.getUsersTeaching();
                
                // Verificar si el usuario está en la lista
                if (usersTeaching.contains(userId)) {
                    usersTeaching.remove(userId);
                    skill.setUsersTeaching(usersTeaching);
                    
                    // Actualizar la habilidad en la base de datos
                    skillsRef.child(skillId).child("users_teaching").setValue(usersTeaching)
                            .addOnSuccessListener(aVoid -> removeResult.setValue(true))
                            .addOnFailureListener(e -> removeResult.setValue(false));
                } else {
                    // El usuario no está en la lista
                    removeResult.setValue(true);
                }
            } else {
                // La habilidad no existe
                removeResult.setValue(false);
            }
        });
        
        return removeResult;
    }
}
