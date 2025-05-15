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
     * Crea una nueva habilidad.
     * @param skill Habilidad a crear
     * @return LiveData con el resultado (true si se creó correctamente)
     */
    public MutableLiveData<Boolean> createSkill(Skill skill) {
        // Generar un nuevo ID para la habilidad
        skill.setSkillId(skillsRef.push().getKey());
        return saveSkill(skill);
    }
    
    /**
     * Actualiza una habilidad existente.
     * @param skill Habilidad con los datos actualizados
     * @return LiveData con el resultado (true si se actualizó correctamente)
     */
    public MutableLiveData<Boolean> updateSkill(Skill skill) {
        // Verificar que la habilidad tenga un ID válido
        if (skill.getSkillId() == null || skill.getSkillId().isEmpty()) {
            MutableLiveData<Boolean> result = new MutableLiveData<>();
            result.setValue(false);
            return result;
        }
        
        return saveSkill(skill);
    }
    
    /**
     * Elimina una habilidad.
     * @param skillId ID de la habilidad a eliminar
     * @return LiveData con el resultado (true si se eliminó correctamente)
     */
    public MutableLiveData<Boolean> deleteSkill(String skillId) {
        MutableLiveData<Boolean> deleteResult = new MutableLiveData<>();
        
        if (skillId == null || skillId.isEmpty()) {
            deleteResult.setValue(false);
            return deleteResult;
        }
        
        skillsRef.child(skillId).removeValue()
                .addOnSuccessListener(aVoid -> deleteResult.setValue(true))
                .addOnFailureListener(e -> deleteResult.setValue(false));
        
        return deleteResult;
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
        return searchSkills(query, null);
    }
    
    /**
     * Obtiene las habilidades por categoría.
     * @param categoryId ID de la categoría
     * @return LiveData con la lista de habilidades de esa categoría
     */
    public MutableLiveData<List<Skill>> getSkillsByCategory(String categoryId) {
        if (categoryId == null || categoryId.isEmpty()) {
            return getAllSkills();
        }
        
        return searchSkills("", categoryId);
    }
    
    /**
     * Busca habilidades por título y/o categoría.
     * @param query Texto de búsqueda
     * @param categoryId ID de categoría para filtrar
     * @return LiveData con la lista de habilidades que coinciden
     */
    public MutableLiveData<List<Skill>> searchSkills(String query, String categoryId) {
        MutableLiveData<List<Skill>> skillsLiveData = new MutableLiveData<>();
        
        // Si la consulta está vacía y no hay categoría, devolver todas las habilidades
        if ((query == null || query.trim().isEmpty()) && (categoryId == null || categoryId.isEmpty())) {
            return getAllSkills();
        }
        
        // Convertir a minúsculas para búsqueda insensible a mayúsculas/minúsculas
        final String lowercaseQuery = query != null ? query.toLowerCase().trim() : "";
        
        skillsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Skill> skills = new ArrayList<>();
                
                for (DataSnapshot skillSnapshot : dataSnapshot.getChildren()) {
                    try {
                        String title = skillSnapshot.child("title").getValue(String.class);
                        String category = skillSnapshot.child("category").getValue(String.class);
                        
                        // Verificar filtro de categoría si está presente
                        if (categoryId != null && !categoryId.isEmpty() && 
                            (category == null || !category.equals(categoryId))) {
                            continue; // Saltar esta habilidad si no coincide con la categoría
                        }
                        
                        // Verificar filtro de texto si está presente
                        boolean matchesQuery = lowercaseQuery.isEmpty() || 
                            (title != null && title.toLowerCase().contains(lowercaseQuery)) ||
                            (category != null && category.toLowerCase().contains(lowercaseQuery));
                            
                        if (matchesQuery) {
                            
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
