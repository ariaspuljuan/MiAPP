package com.skillswap.skillswapp.viewmodel;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.skillswap.skillswapp.data.local.LocalStorageManager;
import com.skillswap.skillswapp.data.model.User;
import com.skillswap.skillswapp.data.repository.UserRepository;

import java.util.List;

/**
 * ViewModel para manejar la lógica de datos de usuario.
 */
public class UserViewModel extends ViewModel {
    private final UserRepository userRepository;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private LocalStorageManager localStorageManager;
    private Context context;

    public UserViewModel() {
        userRepository = UserRepository.getInstance();
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
     * Obtiene los datos de un usuario por su ID.
     */
    public LiveData<User> getUserById(String userId) {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        
        MutableLiveData<User> userLiveData = userRepository.getUserById(userId);
        
        // Observar el resultado para actualizar el estado de carga
        userLiveData.observeForever(user -> {
            isLoading.setValue(false);
            if (user == null) {
                errorMessage.setValue("Error al obtener datos del usuario.");
            }
        });
        
        return userLiveData;
    }
    
    /**
     * Obtiene todos los usuarios.
     * @return LiveData con la lista de usuarios
     */
    public LiveData<List<User>> getAllUsers() {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        
        MutableLiveData<List<User>> usersLiveData = userRepository.getAllUsers();
        
        // Observar el resultado para actualizar el estado de carga
        usersLiveData.observeForever(users -> {
            isLoading.setValue(false);
            if (users == null) {
                errorMessage.setValue("Error al obtener la lista de usuarios.");
            }
        });
        
        return usersLiveData;
    }
    
    /**
     * Busca usuarios por nombre o habilidades.
     * @param query Texto de búsqueda
     * @param categoryId Categoría para filtrar (opcional)
     * @return LiveData con la lista de usuarios que coinciden
     */
    public LiveData<List<User>> searchUsers(String query, String categoryId) {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        
        MutableLiveData<List<User>> usersLiveData = userRepository.searchUsers(query, categoryId);
        
        // Observar el resultado para actualizar el estado de carga
        usersLiveData.observeForever(users -> {
            isLoading.setValue(false);
            if (users == null) {
                errorMessage.setValue("Error en la búsqueda de usuarios.");
            }
        });
        
        return usersLiveData;
    }
    
    /**
     * Busca usuarios con filtros avanzados.
     * @param query Texto de búsqueda
     * @param categoryId Categoría para filtrar (opcional)
     * @param level Nivel de habilidad (0: cualquiera, 1: principiante, 2: intermedio, 3: avanzado)
     * @return LiveData con la lista de usuarios que coinciden
     */
    public LiveData<List<User>> searchUsersAdvanced(String query, String categoryId, int level) {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        
        MutableLiveData<List<User>> usersLiveData = userRepository.searchUsersAdvanced(query, categoryId, level);
        
        // Observar el resultado para actualizar el estado de carga
        Observer<List<User>> observer = new Observer<List<User>>() {
            public void onChanged(List<User> users) {
                isLoading.setValue(false);
                if (users == null) {
                    errorMessage.setValue("Error en la búsqueda avanzada de usuarios.");
                }
                // Eliminar el observador para evitar fugas de memoria
                usersLiveData.removeObserver(this);
            }
        };
        
        usersLiveData.observeForever(observer);
        
        return usersLiveData;
    }

    /**
     * Actualiza los datos de un usuario.
     */
    public LiveData<Boolean> updateUser(User user) {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        
        MutableLiveData<Boolean> updateResult = userRepository.updateUser(user);
        
        // Observar el resultado para actualizar el estado de carga
        updateResult.observeForever(result -> {
            isLoading.setValue(false);
            if (Boolean.FALSE.equals(result)) {
                errorMessage.setValue("Error al actualizar perfil. Intenta nuevamente.");
            }
        });
        
        return updateResult;
    }

    /**
     * Actualiza un campo específico del perfil de usuario.
     */
    public LiveData<Boolean> updateUserField(String userId, String field, Object value) {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        
        MutableLiveData<Boolean> updateResult = userRepository.updateUserField(userId, field, value);
        
        // Observar el resultado para actualizar el estado de carga
        updateResult.observeForever(result -> {
            isLoading.setValue(false);
            if (Boolean.FALSE.equals(result)) {
                errorMessage.setValue("Error al actualizar " + field + ". Intenta nuevamente.");
            }
        });
        
        return updateResult;
    }

    /**
     * Obtiene el estado de carga.
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    /**
     * Obtiene el mensaje de error.
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    /**
     * Genera un ID único para una habilidad.
     * @return ID generado
     */
    public String generateSkillId() {
        return userRepository.generateId();
    }
    
    /**
     * Añade una habilidad a la colección global de habilidades.
     * @param skillId ID de la habilidad
     * @param title Título de la habilidad
     * @param categoryId ID de la categoría
     * @param userId ID del usuario que enseña la habilidad
     */
    public void addSkillToGlobal(String skillId, String title, String categoryId, String userId) {
        userRepository.addSkillToGlobal(skillId, title, categoryId, userId);
    }
    
    /**
     * Actualiza una habilidad en la colección global de habilidades.
     * @param skillId ID de la habilidad
     * @param title Título de la habilidad
     * @param categoryId ID de la categoría
     */
    public void updateSkillInGlobal(String skillId, String title, String categoryId) {
        userRepository.updateSkillInGlobal(skillId, title, categoryId);
    }
    
    /**
     * Elimina un usuario de la lista de usuarios que enseñan una habilidad.
     * @param skillId ID de la habilidad
     * @param userId ID del usuario a eliminar
     */
    public void removeUserFromSkill(String skillId, String userId) {
        userRepository.removeUserFromSkill(skillId, userId);
    }
    
    /**
     * Añade un usuario a la lista de contactos recientes.
     * @param contactId ID del usuario a añadir como contacto reciente
     * @return LiveData con el resultado de la operación
     */
    public LiveData<Boolean> addRecentContact(String contactId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        
        try {
            if (context == null) {
                result.setValue(false);
                errorMessage.setValue("Error: Contexto no inicializado. Llama a initContext primero.");
                return result;
            }
            
            // Añadir directamente en el hilo principal
            boolean success = localStorageManager.addRecentContact(contactId);
            result.setValue(success);
            if (!success) {
                errorMessage.setValue("Error al añadir contacto reciente");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setValue(false);
            errorMessage.setValue("Error: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Añade un usuario a la lista de favoritos.
     * @param favoriteId ID del usuario a añadir como favorito
     * @return LiveData con el resultado de la operación
     */
    public LiveData<Boolean> addFavorite(String favoriteId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        
        try {
            if (context == null) {
                result.setValue(false);
                errorMessage.setValue("Error: Contexto no inicializado. Llama a initContext primero.");
                return result;
            }
            
            // Añadir directamente en el hilo principal
            boolean success = localStorageManager.addFavorite(favoriteId);
            result.setValue(success);
            if (!success) {
                errorMessage.setValue("Error al añadir favorito");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setValue(false);
            errorMessage.setValue("Error: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Verifica si un usuario está en la lista de favoritos.
     * @param userId ID del usuario a verificar
     * @return LiveData con el resultado (true si es favorito, false si no)
     */
    public LiveData<Boolean> isFavorite(String userId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        
        try {
            if (context == null) {
                result.setValue(false);
                return result;
            }
            
            // Verificar directamente en el hilo principal
            boolean isFav = localStorageManager.isFavorite(userId);
            result.setValue(isFav);
        } catch (Exception e) {
            e.printStackTrace();
            result.setValue(false);
        }
        
        return result;
    }
    
    /**
     * Elimina un usuario de la lista de favoritos.
     * @param favoriteId ID del usuario a eliminar de favoritos
     * @return LiveData con el resultado de la operación
     */
    public LiveData<Boolean> removeFavorite(String favoriteId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        
        try {
            if (context == null) {
                result.setValue(false);
                errorMessage.setValue("Error: Contexto no inicializado. Llama a initContext primero.");
                return result;
            }
            
            // Eliminar directamente en el hilo principal
            boolean success = localStorageManager.removeFavorite(favoriteId);
            result.setValue(success);
            if (!success) {
                errorMessage.setValue("Error al eliminar favorito");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setValue(false);
            errorMessage.setValue("Error: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Obtiene la lista de IDs de usuarios favoritos.
     * @return LiveData con la lista de IDs
     */
    public LiveData<List<String>> getFavoriteIds() {
        MutableLiveData<List<String>> result = new MutableLiveData<>();
        
        try {
            if (context == null) {
                result.setValue(null);
                errorMessage.setValue("Error: Contexto no inicializado. Llama a initContext primero.");
                return result;
            }
            
            // Obtener directamente en el hilo principal
            List<String> ids = localStorageManager.getFavoriteIds();
            result.setValue(ids);
        } catch (Exception e) {
            e.printStackTrace();
            result.setValue(null);
            errorMessage.setValue("Error al obtener favoritos: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Obtiene la lista de IDs de contactos recientes.
     * @return LiveData con la lista de IDs
     */
    public LiveData<List<String>> getRecentContactIds() {
        MutableLiveData<List<String>> result = new MutableLiveData<>();
        
        try {
            if (context == null) {
                result.setValue(null);
                errorMessage.setValue("Error: Contexto no inicializado. Llama a initContext primero.");
                return result;
            }
            
            // Obtener directamente en el hilo principal
            List<String> ids = localStorageManager.getRecentContactIds();
            result.setValue(ids);
        } catch (Exception e) {
            e.printStackTrace();
            result.setValue(null);
            errorMessage.setValue("Error al obtener contactos recientes: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Sube una imagen de perfil a Firebase Storage y devuelve la URL de descarga.
     * @param userId ID del usuario
     * @param imageUri URI de la imagen a subir
     * @return LiveData con la URL de descarga de la imagen
     */
    public LiveData<String> uploadProfileImage(String userId, android.net.Uri imageUri) {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        
        MutableLiveData<String> urlLiveData = userRepository.uploadProfileImage(userId, imageUri);
        
        // Observar el resultado para actualizar el estado de carga
        urlLiveData.observeForever(url -> {
            isLoading.setValue(false);
            if (url == null || url.isEmpty()) {
                errorMessage.setValue("Error al subir la imagen de perfil.");
            }
        });
        
        return urlLiveData;
    }
}
