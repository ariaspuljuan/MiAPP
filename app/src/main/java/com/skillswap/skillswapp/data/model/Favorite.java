package com.skillswap.skillswapp.data.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Modelo de datos para representar un usuario favorito en la aplicación SkillSwap.
 */
public class Favorite {
    private String userId;
    private String favoriteUserId;
    private Date timestamp;
    private String notes;

    // Constructor vacío requerido para Firebase
    public Favorite() {
    }

    public Favorite(String userId, String favoriteUserId) {
        this.userId = userId;
        this.favoriteUserId = favoriteUserId;
        this.timestamp = new Date();
        this.notes = "";
    }

    public Favorite(String userId, String favoriteUserId, String notes) {
        this.userId = userId;
        this.favoriteUserId = favoriteUserId;
        this.timestamp = new Date();
        this.notes = notes;
    }

    // Getters y setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFavoriteUserId() {
        return favoriteUserId;
    }

    public void setFavoriteUserId(String favoriteUserId) {
        this.favoriteUserId = favoriteUserId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Método para convertir a Map para Firebase
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("timestamp", timestamp);
        result.put("notes", notes);
        return result;
    }
}
