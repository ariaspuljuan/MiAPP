package com.skillswap.skillswapp.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gestor de almacenamiento local para favoritos y contactos recientes.
 */
public class LocalStorageManager {
    private static final String PREFS_NAME = "SkillSwapPrefs";
    private static final String KEY_FAVORITES = "favorites_";
    private static final String KEY_RECENT_CONTACTS = "recent_contacts_";
    
    private static LocalStorageManager instance;
    private final SharedPreferences sharedPreferences;
    private final Gson gson;
    
    private LocalStorageManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }
    
    public static synchronized LocalStorageManager getInstance(Context context) {
        if (instance == null) {
            instance = new LocalStorageManager(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * Añade un usuario a favoritos.
     * @param userId ID del usuario a añadir a favoritos
     * @return true si se añadió correctamente
     */
    public boolean addFavorite(String userId) {
        try {
            String currentUserId = getCurrentUserId();
            if (currentUserId == null) return false;
            
            List<FavoriteItem> favorites = getFavorites();
            
            // Verificar si ya existe
            for (FavoriteItem item : favorites) {
                if (item.getUserId().equals(userId)) {
                    return true; // Ya existe
                }
            }
            
            // Añadir nuevo favorito
            FavoriteItem newFavorite = new FavoriteItem(userId, new Date().getTime());
            favorites.add(newFavorite);
            
            // Guardar la lista actualizada
            return saveFavorites(favorites);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Elimina un usuario de favoritos.
     * @param userId ID del usuario a eliminar de favoritos
     * @return true si se eliminó correctamente
     */
    public boolean removeFavorite(String userId) {
        try {
            String currentUserId = getCurrentUserId();
            if (currentUserId == null) return false;
            
            List<FavoriteItem> favorites = getFavorites();
            boolean removed = false;
            
            // Buscar y eliminar
            for (int i = 0; i < favorites.size(); i++) {
                if (favorites.get(i).getUserId().equals(userId)) {
                    favorites.remove(i);
                    removed = true;
                    break;
                }
            }
            
            if (removed) {
                return saveFavorites(favorites);
            }
            return true; // No existía, consideramos éxito
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Verifica si un usuario está en favoritos.
     * @param userId ID del usuario a verificar
     * @return true si está en favoritos
     */
    public boolean isFavorite(String userId) {
        try {
            String currentUserId = getCurrentUserId();
            if (currentUserId == null) return false;
            
            List<FavoriteItem> favorites = getFavorites();
            
            for (FavoriteItem item : favorites) {
                if (item.getUserId().equals(userId)) {
                    return true;
                }
            }
            
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Obtiene la lista de IDs de usuarios favoritos.
     * @return Lista de IDs de usuarios favoritos
     */
    public List<String> getFavoriteIds() {
        List<String> ids = new ArrayList<>();
        List<FavoriteItem> favorites = getFavorites();
        
        for (FavoriteItem item : favorites) {
            ids.add(item.getUserId());
        }
        
        return ids;
    }
    
    /**
     * Añade un usuario a contactos recientes.
     * @param userId ID del usuario a añadir a contactos recientes
     * @return true si se añadió correctamente
     */
    public boolean addRecentContact(String userId) {
        try {
            String currentUserId = getCurrentUserId();
            if (currentUserId == null) return false;
            
            List<RecentContactItem> contacts = getRecentContacts();
            
            // Eliminar si ya existe para actualizarlo
            for (int i = 0; i < contacts.size(); i++) {
                if (contacts.get(i).getUserId().equals(userId)) {
                    contacts.remove(i);
                    break;
                }
            }
            
            // Añadir al principio (más reciente)
            RecentContactItem newContact = new RecentContactItem(userId, new Date().getTime());
            contacts.add(0, newContact);
            
            // Limitar a 20 contactos recientes
            if (contacts.size() > 20) {
                contacts = contacts.subList(0, 20);
            }
            
            // Guardar la lista actualizada
            return saveRecentContacts(contacts);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Obtiene la lista de IDs de contactos recientes.
     * @return Lista de IDs de contactos recientes
     */
    public List<String> getRecentContactIds() {
        List<String> ids = new ArrayList<>();
        List<RecentContactItem> contacts = getRecentContacts();
        
        for (RecentContactItem item : contacts) {
            ids.add(item.getUserId());
        }
        
        return ids;
    }
    
    /**
     * Obtiene la lista de favoritos.
     * @return Lista de favoritos
     */
    private List<FavoriteItem> getFavorites() {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) return new ArrayList<>();
        
        String key = KEY_FAVORITES + currentUserId;
        String json = sharedPreferences.getString(key, null);
        
        if (json == null) {
            return new ArrayList<>();
        }
        
        Type type = new TypeToken<List<FavoriteItem>>(){}.getType();
        return gson.fromJson(json, type);
    }
    
    /**
     * Guarda la lista de favoritos.
     * @param favorites Lista de favoritos a guardar
     * @return true si se guardó correctamente
     */
    private boolean saveFavorites(List<FavoriteItem> favorites) {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) return false;
        
        String key = KEY_FAVORITES + currentUserId;
        String json = gson.toJson(favorites);
        
        return sharedPreferences.edit().putString(key, json).commit();
    }
    
    /**
     * Obtiene la lista de contactos recientes.
     * @return Lista de contactos recientes
     */
    private List<RecentContactItem> getRecentContacts() {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) return new ArrayList<>();
        
        String key = KEY_RECENT_CONTACTS + currentUserId;
        String json = sharedPreferences.getString(key, null);
        
        if (json == null) {
            return new ArrayList<>();
        }
        
        Type type = new TypeToken<List<RecentContactItem>>(){}.getType();
        return gson.fromJson(json, type);
    }
    
    /**
     * Guarda la lista de contactos recientes.
     * @param contacts Lista de contactos recientes a guardar
     * @return true si se guardó correctamente
     */
    private boolean saveRecentContacts(List<RecentContactItem> contacts) {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) return false;
        
        String key = KEY_RECENT_CONTACTS + currentUserId;
        String json = gson.toJson(contacts);
        
        return sharedPreferences.edit().putString(key, json).commit();
    }
    
    /**
     * Obtiene el ID del usuario actual.
     * @return ID del usuario actual o null si no hay usuario autenticado
     */
    private String getCurrentUserId() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        return null;
    }
    
    /**
     * Clase para almacenar información de un favorito.
     */
    public static class FavoriteItem {
        private String userId;
        private long timestamp;
        private String notes;
        
        public FavoriteItem(String userId, long timestamp) {
            this.userId = userId;
            this.timestamp = timestamp;
            this.notes = "";
        }
        
        public String getUserId() {
            return userId;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        public String getNotes() {
            return notes;
        }
        
        public void setNotes(String notes) {
            this.notes = notes;
        }
    }
    
    /**
     * Clase para almacenar información de un contacto reciente.
     */
    public static class RecentContactItem {
        private String userId;
        private long timestamp;
        
        public RecentContactItem(String userId, long timestamp) {
            this.userId = userId;
            this.timestamp = timestamp;
        }
        
        public String getUserId() {
            return userId;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
    }
}
