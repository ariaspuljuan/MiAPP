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
     * Obtiene sugerencias de búsqueda basadas en una consulta parcial.
     * @param query Consulta parcial
     * @return LiveData con la lista de sugerencias
     */
    public MutableLiveData<List<String>> getSearchSuggestions(String query) {
        MutableLiveData<List<String>> suggestionsLiveData = new MutableLiveData<>();
        
        if (query == null || query.isEmpty()) {
            suggestionsLiveData.setValue(new ArrayList<>());
            return suggestionsLiveData;
        }
        
        // Buscar habilidades que coincidan con la consulta
        skillsRef.orderByChild("title").startAt(query).endAt(query + "\uf8ff").limitToFirst(5)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<String> suggestions = new ArrayList<>();
                        
                        for (DataSnapshot skillSnapshot : dataSnapshot.getChildren()) {
                            String title = skillSnapshot.child("title").getValue(String.class);
                            if (title != null && !title.isEmpty()) {
                                suggestions.add(title);
                            }
                        }
                        
                        suggestionsLiveData.setValue(suggestions);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        suggestionsLiveData.setValue(new ArrayList<>());
                    }
                });
        
        return suggestionsLiveData;
    }
    
    /**
     * Obtiene las habilidades destacadas para mostrar en la pantalla de exploración.
     * @return LiveData con la lista de habilidades destacadas
     */
    public MutableLiveData<List<Skill>> getFeaturedSkills() {
        MutableLiveData<List<Skill>> skillsLiveData = new MutableLiveData<>();
        
        // Consultar las habilidades más populares (con más usuarios enseñándolas)
        // Limitamos a 10 resultados para mostrar en la sección destacada
        skillsRef.orderByChild("popularity").limitToLast(10).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Skill> skills = new ArrayList<>();
                
                for (DataSnapshot skillSnapshot : dataSnapshot.getChildren()) {
                    try {
                        Skill skill = new Skill();
                        skill.setSkillId(skillSnapshot.getKey());
                        skill.setTitle(skillSnapshot.child("title").getValue(String.class));
                        skill.setCategory(skillSnapshot.child("category").getValue(String.class));
                        skill.setDescription(skillSnapshot.child("description").getValue(String.class));
                        
                        // Obtener nivel de la habilidad
                        if (skillSnapshot.child("level").exists()) {
                            skill.setLevel(skillSnapshot.child("level").getValue(Integer.class));
                        }
                        
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
     * Busca habilidades con filtros avanzados.
     * @param query Texto de búsqueda
     * @param categoryId ID de categoría para filtrar
     * @param level Nivel de habilidad (0: cualquiera, 1: principiante, 2: intermedio, 3: avanzado)
     * @return LiveData con la lista de habilidades que coinciden
     */
    public MutableLiveData<List<Skill>> searchSkillsAdvanced(String query, String categoryId, int level) {
        MutableLiveData<List<Skill>> skillsLiveData = new MutableLiveData<>();
        
        // Si la consulta está vacía, no hay categoría y el nivel es 0, devolver todas las habilidades
        if ((query == null || query.trim().isEmpty()) && 
            (categoryId == null || categoryId.isEmpty()) && 
            level <= 0) {
            return getAllSkills();
        }
        
        // Convertir a minúsculas para búsqueda insensible a mayúsculas/minúsculas
        final String lowercaseQuery = query != null ? query.toLowerCase().trim() : "";
        final int minLevel = level > 0 ? level : 0;
        
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
                        
                        if (!matchesQuery) {
                            continue; // Saltar esta habilidad si no coincide con la consulta
                        }
                        
                        // Verificar filtro de nivel si está presente
                        if (minLevel > 0) {
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
                            
                            if (skillLevel < minLevel) {
                                continue; // Saltar esta habilidad si no alcanza el nivel mínimo
                            }
                        }
                        
                        // Si pasa todos los filtros, crear el objeto Skill y añadirlo a la lista
                        Skill skill = new Skill();
                        skill.setSkillId(skillSnapshot.getKey());
                        skill.setTitle(title);
                        skill.setCategory(category);
                        skill.setDescription(skillSnapshot.child("description").getValue(String.class));
                        
                        // Obtener el nivel de la habilidad
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
                            }
                        } else {
                            skill.setLevel(1); // Valor predeterminado si es nulo
                        }
                        
                        // Obtener la imagen de la habilidad si existe
                        String imageUrl = skillSnapshot.child("imageUrl").getValue(String.class);
                        skill.setImageUrl(imageUrl);
                        
                        // Obtener la lista de usuarios que enseñan esta habilidad
                        List<String> teachingUsers = new ArrayList<>();
                        DataSnapshot teachingSnapshot = skillSnapshot.child("teaching_users");
                        if (teachingSnapshot.exists()) {
                            for (DataSnapshot userSnapshot : teachingSnapshot.getChildren()) {
                                teachingUsers.add(userSnapshot.getKey());
                            }
                        }
                        skill.setTeachingUsers(teachingUsers);
                        
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
    
    /**
     * Añade una habilidad a favoritos para el usuario actual.
     * @param skillId ID de la habilidad
     * @return LiveData con el resultado (true si se añadió correctamente)
     */
    public MutableLiveData<Boolean> addFavoriteSkill(String skillId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        
        // Obtener el ID del usuario actual
        String userId = AuthRepository.getInstance().getCurrentUserId();
        if (userId == null || userId.isEmpty() || skillId == null || skillId.isEmpty()) {
            result.setValue(false);
            return result;
        }
        
        // Añadir la habilidad a favoritos del usuario
        databaseRef.child("users").child(userId).child("favorite_skills").child(skillId).setValue(true)
                .addOnSuccessListener(aVoid -> result.setValue(true))
                .addOnFailureListener(e -> result.setValue(false));
        
        return result;
    }
    
    /**
     * Elimina una habilidad de favoritos para el usuario actual.
     * @param skillId ID de la habilidad
     * @return LiveData con el resultado (true si se eliminó correctamente)
     */
    public MutableLiveData<Boolean> removeFavoriteSkill(String skillId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        
        // Obtener el ID del usuario actual
        String userId = AuthRepository.getInstance().getCurrentUserId();
        if (userId == null || userId.isEmpty() || skillId == null || skillId.isEmpty()) {
            result.setValue(false);
            return result;
        }
        
        // Eliminar la habilidad de favoritos del usuario
        databaseRef.child("users").child(userId).child("favorite_skills").child(skillId).removeValue()
                .addOnSuccessListener(aVoid -> result.setValue(true))
                .addOnFailureListener(e -> result.setValue(false));
        
        return result;
    }
    
    /**
     * Verifica si una habilidad está en favoritos para el usuario actual.
     * @param skillId ID de la habilidad
     * @return LiveData con el resultado (true si está en favoritos)
     */
    public MutableLiveData<Boolean> isSkillFavorite(String skillId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        
        // Obtener el ID del usuario actual
        String userId = AuthRepository.getInstance().getCurrentUserId();
        if (userId == null || userId.isEmpty() || skillId == null || skillId.isEmpty()) {
            result.setValue(false);
            return result;
        }
        
        // Verificar si la habilidad está en favoritos
        databaseRef.child("users").child(userId).child("favorite_skills").child(skillId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        result.setValue(dataSnapshot.exists());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        result.setValue(false);
                    }
                });
        
        return result;
    }
}
