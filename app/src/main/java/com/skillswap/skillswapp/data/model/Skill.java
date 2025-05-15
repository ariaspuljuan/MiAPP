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
    private List<String> usersTeaching;

    // Constructor vacío requerido para Firebase
    public Skill() {
        usersTeaching = new ArrayList<>();
    }

    public Skill(String skillId, String title, String category) {
        this.skillId = skillId;
        this.title = title;
        this.category = category;
        this.usersTeaching = new ArrayList<>();
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

    // Método para convertir a Map para Firebase
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("title", title);
        result.put("category", category);
        result.put("users_teaching", usersTeaching);
        return result;
    }
}
