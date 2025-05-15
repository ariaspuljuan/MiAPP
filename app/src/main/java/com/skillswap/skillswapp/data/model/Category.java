package com.skillswap.skillswapp.data.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Modelo de datos para representar una categoría de habilidades en la aplicación SkillSwap.
 */
public class Category {
    private String categoryId;
    private String name;
    private String description;
    private String iconUrl;

    // Constructor vacío requerido para Firebase
    public Category() {
    }

    public Category(String categoryId, String name) {
        this.categoryId = categoryId;
        this.name = name;
        this.description = "";
        this.iconUrl = "";
    }

    public Category(String categoryId, String name, String description, String iconUrl) {
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.iconUrl = iconUrl;
    }

    // Getters y setters
    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    // Método para convertir a Map para Firebase
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("description", description);
        result.put("icon_url", iconUrl);
        return result;
    }
}
