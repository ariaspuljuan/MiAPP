package com.skillswap.skillswapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.skillswap.skillswapp.data.model.User;
import com.skillswap.skillswapp.data.repository.UserRepository;

/**
 * ViewModel para manejar la lógica de datos de usuario.
 */
public class UserViewModel extends ViewModel {
    private UserRepository userRepository;
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public UserViewModel() {
        userRepository = UserRepository.getInstance();
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
}
