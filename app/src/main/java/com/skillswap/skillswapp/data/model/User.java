package com.skillswap.skillswapp.data.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Modelo de datos para representar un usuario en la aplicación SkillSwap.
 * Sigue la estructura de la base de datos de Firebase.
 */
public class User {
    // Datos del perfil del usuario
    private String userId;
    private UserProfile profile;
    private Map<String, SkillToTeach> skillsToTeach;
    private Map<String, SkillToLearn> skillsToLearn;

    // Constructor vacío requerido para Firebase
    public User() {
    }

    public User(String userId, String name, String email) {
        this.userId = userId;
        this.profile = new UserProfile(name, email);
        this.skillsToTeach = new HashMap<>();
        this.skillsToLearn = new HashMap<>();
    }

    // Getters y setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public UserProfile getProfile() {
        return profile;
    }

    public void setProfile(UserProfile profile) {
        this.profile = profile;
    }

    public Map<String, SkillToTeach> getSkillsToTeach() {
        return skillsToTeach;
    }

    public void setSkillsToTeach(Map<String, SkillToTeach> skillsToTeach) {
        this.skillsToTeach = skillsToTeach;
    }

    public Map<String, SkillToLearn> getSkillsToLearn() {
        return skillsToLearn;
    }

    public void setSkillsToLearn(Map<String, SkillToLearn> skillsToLearn) {
        this.skillsToLearn = skillsToLearn;
    }

    /**
     * Clase interna para representar el perfil del usuario
     */
    public static class UserProfile {
        private String name;
        private String email;
        private String photoUrl;
        private String bio;
        private Date lastActive;

        public UserProfile() {
        }

        public UserProfile(String name, String email) {
            this.name = name;
            this.email = email;
            this.photoUrl = "";
            this.bio = "";
            this.lastActive = new Date();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhotoUrl() {
            return photoUrl;
        }

        public void setPhotoUrl(String photoUrl) {
            this.photoUrl = photoUrl;
        }

        public String getBio() {
            return bio;
        }

        public void setBio(String bio) {
            this.bio = bio;
        }

        public Date getLastActive() {
            return lastActive;
        }

        public void setLastActive(Date lastActive) {
            this.lastActive = lastActive;
        }

        public Map<String, Object> toMap() {
            HashMap<String, Object> result = new HashMap<>();
            result.put("name", name);
            result.put("email", email);
            result.put("photoUrl", photoUrl);
            result.put("bio", bio);
            result.put("lastActive", lastActive);
            return result;
        }
    }

    /**
     * Clase interna para representar una habilidad que el usuario puede enseñar
     */
    public static class SkillToTeach {
        private String title;
        private int level; // 1-5
        private String category;
        private String description;

        public SkillToTeach() {
        }

        public SkillToTeach(String title, int level, String category, String description) {
            this.title = title;
            this.level = level;
            this.category = category;
            this.description = description;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Map<String, Object> toMap() {
            HashMap<String, Object> result = new HashMap<>();
            result.put("title", title);
            result.put("level", level);
            result.put("category", category);
            result.put("description", description);
            return result;
        }
    }

    /**
     * Clase interna para representar una habilidad que el usuario quiere aprender
     */
    public static class SkillToLearn {
        private String title;
        private int priority; // 1-3

        public SkillToLearn() {
        }

        public SkillToLearn(String title, int priority) {
            this.title = title;
            this.priority = priority;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int priority) {
            this.priority = priority;
        }

        public Map<String, Object> toMap() {
            HashMap<String, Object> result = new HashMap<>();
            result.put("title", title);
            result.put("priority", priority);
            return result;
        }
    }

    // Método para convertir a Map para Firebase siguiendo la estructura requerida
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        
        // Agregar perfil con verificación de nulos
        if (profile != null) {
            result.put("profile", profile.toMap());
        } else {
            result.put("profile", new UserProfile().toMap());
        }
        
        // Convertir skillsToTeach a Map con verificación de nulos
        HashMap<String, Object> teachMap = new HashMap<>();
        if (skillsToTeach != null) {
            for (Map.Entry<String, SkillToTeach> entry : skillsToTeach.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    teachMap.put(entry.getKey(), entry.getValue().toMap());
                }
            }
        }
        result.put("skills_to_teach", teachMap);
        
        // Convertir skillsToLearn a Map con verificación de nulos
        HashMap<String, Object> learnMap = new HashMap<>();
        if (skillsToLearn != null) {
            for (Map.Entry<String, SkillToLearn> entry : skillsToLearn.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    learnMap.put(entry.getKey(), entry.getValue().toMap());
                }
            }
        }
        result.put("skills_to_learn", learnMap);
        
        return result;
    }
}
