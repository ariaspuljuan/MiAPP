package com.skillswap.skillswapp.data.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Modelo de datos para representar una habilidad en la aplicación SkillSwap.
 */
public class Skill {
    private String skillId;
    private String title;
    private String category;
    private String description;
    private int level;
    private String imageUrl;
    private List<String> usersTeaching;
    private List<String> teachingUsers;

    // Constructor vacío requerido para Firebase
    public Skill() {
        usersTeaching = new ArrayList<>();
        teachingUsers = new ArrayList<>();
        level = 1; // Nivel por defecto
    }

    public Skill(String skillId, String title, String category) {
        this.skillId = skillId;
        this.title = title;
        this.category = category;
        this.description = "";
        this.level = 1; // Nivel por defecto
        this.imageUrl = "";
        this.usersTeaching = new ArrayList<>();
        this.teachingUsers = new ArrayList<>();
    }
    
    public Skill(String skillId, String title, String category, String description, int level) {
        this.skillId = skillId;
        this.title = title;
        this.category = category;
        this.description = description;
        this.level = level;
        this.imageUrl = "";
        this.usersTeaching = new ArrayList<>();
        this.teachingUsers = new ArrayList<>();
    }

    // Getters y setters
    public String getSkillId() {
        return skillId;
    }

    public void setSkillId(String skillId) {
        this.skillId = skillId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getUsersTeaching() {
        return usersTeaching;
    }

    public void setUsersTeaching(List<String> usersTeaching) {
        this.usersTeaching = usersTeaching;
    }

    public void addUserTeaching(String userId) {
        if (!usersTeaching.contains(userId)) {
            usersTeaching.add(userId);
        }
    }

    public void removeUserTeaching(String userId) {
        usersTeaching.remove(userId);
    }

    // Nuevos getters y setters
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public List<String> getTeachingUsers() {
        return teachingUsers;
    }

    public void setTeachingUsers(List<String> teachingUsers) {
        this.teachingUsers = teachingUsers;
    }

    // Método para convertir a Map para Firebase
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("title", title);
        result.put("category", category);
        result.put("description", description);
        result.put("level", level);
        result.put("imageUrl", imageUrl);
        result.put("users_teaching", usersTeaching);
        return result;
    }
}
