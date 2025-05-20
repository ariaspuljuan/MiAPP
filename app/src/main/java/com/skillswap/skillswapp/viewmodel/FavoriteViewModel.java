package com.skillswap.skillswapp.viewmodel;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.skillswap.skillswapp.data.local.LocalStorageManager;
import com.skillswap.skillswapp.data.model.User;
import com.skillswap.skillswapp.data.repository.FavoriteRepository;
import com.skillswap.skillswapp.data.repository.UserRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ViewModel para gestionar los favoritos.
 */
public class FavoriteViewModel extends ViewModel {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private LocalStorageManager localStorageManager;
    private Context context;

    public FavoriteViewModel() {
        this.favoriteRepository = FavoriteRepository.getInstance();
        this.userRepository = UserRepository.getInstance();
    }
    
    /**
     * Inicializa el contexto para acceder al almacenamiento local.
     * Debe llamarse después de crear el ViewModel.
     * @param context Contexto de la aplicación
     */
    public void initContext(Context context) {
        this.context = context.getApplicationContext();
        this.localStorageManager = LocalStorageManager.getInstance(this.context);
    }

    /**
     * Obtiene los usuarios favoritos de un usuario.
     * @param userId ID del usuario
     * @return LiveData con la lista de usuarios favoritos
     */
    public LiveData<List<User>> getFavoriteUsers(String userId) {
        MutableLiveData<List<User>> result = new MutableLiveData<>();
        isLoading.setValue(true);
        
        try {
            if (context == null) {
                result.setValue(new ArrayList<>());
                errorMessage.setValue("Error: Contexto no inicializado. Llama a initContext primero.");
                isLoading.setValue(false);
                return result;
            }
            
            // Obtener los IDs de favoritos en el hilo principal
            List<String> favoriteIds = localStorageManager.getFavoriteIds();
            
            if (favoriteIds.isEmpty()) {
                result.setValue(new ArrayList<>());
                isLoading.setValue(false);
                return result;
            }
            
            // Limitar a 20 por rendimiento
            if (favoriteIds.size() > 20) {
                favoriteIds = favoriteIds.subList(0, 20);
            }
            
            // Crear una lista para almacenar los usuarios
            List<User> users = new ArrayList<>();
            final List<String> finalFavoriteIds = favoriteIds;
            final int[] loadedCount = {0};
            
            // Obtener cada usuario en el hilo principal
            for (String favId : favoriteIds) {
                userRepository.getUserById(favId).observeForever(user -> {
                    if (user != null) {
                        user.setFavorite(true); // Marcar como favorito
                        users.add(user);
                    }
                    
                    loadedCount[0]++;
                    
                    // Si hemos procesado todos los IDs, actualizar el resultado
                    if (loadedCount[0] >= finalFavoriteIds.size()) {
                        result.setValue(users);
                        isLoading.setValue(false);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setValue(new ArrayList<>());
            errorMessage.setValue("Error al obtener favoritos: " + e.getMessage());
            isLoading.setValue(false);
        }
        
        return result;
    }

    /**
     * Obtiene los IDs de los usuarios favoritos de un usuario.
     * @param userId ID del usuario
     * @return LiveData con el conjunto de IDs de usuarios favoritos
     */
    public LiveData<Set<String>> getFavoriteUserIds(String userId) {
        MutableLiveData<Set<String>> result = new MutableLiveData<>();
        isLoading.setValue(true);
        
        try {
            if (context == null) {
                result.setValue(new HashSet<>());
                errorMessage.setValue("Error: Contexto no inicializado. Llama a initContext primero.");
                isLoading.setValue(false);
                return result;
            }
            
            // Obtener los IDs de favoritos directamente en el hilo principal
            List<String> favoriteIds = localStorageManager.getFavoriteIds();
            Set<String> favoriteIdSet = new HashSet<>(favoriteIds);
            result.setValue(favoriteIdSet);
            isLoading.setValue(false);
        } catch (Exception e) {
            e.printStackTrace();
            result.setValue(new HashSet<>());
            errorMessage.setValue("Error al obtener IDs de favoritos: " + e.getMessage());
            isLoading.setValue(false);
        }
        
        return result;
    }

    /**
     * Verifica si un usuario es favorito.
     * @param userId ID del usuario
     * @param favoriteId ID del usuario a verificar
     * @return LiveData con el resultado (true si es favorito, false si no)
     */
    public LiveData<Boolean> isFavorite(String userId, String favoriteId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        
        try {
            if (context == null) {
                result.setValue(false);
                errorMessage.setValue("Error: Contexto no inicializado. Llama a initContext primero.");
                return result;
            }
            
            // Verificar directamente en el hilo principal
            boolean isFav = localStorageManager.isFavorite(favoriteId);
            result.setValue(isFav);
        } catch (Exception e) {
            e.printStackTrace();
            result.setValue(false);
            errorMessage.setValue("Error al verificar favorito: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Verifica si un usuario es favorito (método simplificado).
     * @param favoriteId ID del usuario a verificar
     * @return LiveData con el resultado (true si es favorito, false si no)
     */
    public LiveData<Boolean> isFavorite(String favoriteId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        
        try {
            if (context == null) {
                result.setValue(false);
                errorMessage.setValue("Error: Contexto no inicializado. Llama a initContext primero.");
                return result;
            }
            
            // Verificar directamente en el hilo principal
            boolean isFav = localStorageManager.isFavorite(favoriteId);
            result.setValue(isFav);
        } catch (Exception e) {
            e.printStackTrace();
            result.setValue(false);
            errorMessage.setValue("Error al verificar favorito: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * Añade un usuario a favoritos.
     * @param userId ID del usuario a añadir como favorito
     * @return LiveData con el resultado de la operación
     */
    public LiveData<Boolean> addFavorite(String userId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        isLoading.setValue(true);
        
        try {
            if (context == null) {
                result.setValue(false);
                errorMessage.setValue("Error: Contexto no inicializado. Llama a initContext primero.");
                isLoading.setValue(false);
                return result;
            }
            
            // Añadir directamente en el hilo principal
            boolean success = localStorageManager.addFavorite(userId);
            result.setValue(success);
            if (!success) {
                errorMessage.setValue("Error al añadir favorito");
            }
            isLoading.setValue(false);
        } catch (Exception e) {
            e.printStackTrace();
            result.setValue(false);
            errorMessage.setValue("Error al añadir favorito: " + e.getMessage());
            isLoading.setValue(false);
        }
        
        return result;
    }

    /**
     * Elimina un usuario de favoritos.
     * @param userId ID del usuario a eliminar de favoritos
     * @return LiveData con el resultado de la operación
     */
    public LiveData<Boolean> removeFavorite(String userId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        isLoading.setValue(true);
        
        try {
            if (context == null) {
                result.setValue(false);
                errorMessage.setValue("Error: Contexto no inicializado. Llama a initContext primero.");
                isLoading.setValue(false);
                return result;
            }
            
            // Eliminar directamente en el hilo principal
            boolean success = localStorageManager.removeFavorite(userId);
            result.setValue(success);
            if (!success) {
                errorMessage.setValue("Error al eliminar favorito");
            }
            isLoading.setValue(false);
        } catch (Exception e) {
            e.printStackTrace();
            result.setValue(false);
            errorMessage.setValue("Error al eliminar favorito: " + e.getMessage());
            isLoading.setValue(false);
        }
        
        return result;
    }

    /**
     * Obtiene el estado de carga.
     * @return LiveData con el estado de carga
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    /**
     * Obtiene el mensaje de error.
     * @return LiveData con el mensaje de error
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
}
