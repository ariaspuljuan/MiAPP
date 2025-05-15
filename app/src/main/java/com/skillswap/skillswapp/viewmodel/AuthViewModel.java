package com.skillswap.skillswapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.skillswap.skillswapp.data.repository.AuthRepository;

/**
 * ViewModel para manejar la lógica de autenticación.
 */
public class AuthViewModel extends ViewModel {
    private AuthRepository authRepository;
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public AuthViewModel() {
        authRepository = AuthRepository.getInstance();
    }

    /**
     * Registra un nuevo usuario.
     */
    public LiveData<Boolean> register(String name, String email, String password) {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        
        MutableLiveData<Boolean> registerResult = authRepository.registerUser(name, email, password);
        
        // Observar el resultado para actualizar el estado de carga
        registerResult.observeForever(result -> {
            isLoading.setValue(false);
            if (Boolean.FALSE.equals(result)) {
                errorMessage.setValue("Error al registrar usuario. Verifica tus datos e intenta nuevamente.");
            }
        });
        
        return registerResult;
    }

    /**
     * Inicia sesión con email y contraseña.
     */
    public LiveData<Boolean> login(String email, String password) {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        
        MutableLiveData<Boolean> loginResult = authRepository.loginUser(email, password);
        
        // Observar el resultado para actualizar el estado de carga
        loginResult.observeForever(result -> {
            isLoading.setValue(false);
            if (Boolean.FALSE.equals(result)) {
                errorMessage.setValue("Error al iniciar sesión. Verifica tus credenciales e intenta nuevamente.");
            }
        });
        
        return loginResult;
    }

    /**
     * Envía un correo para restablecer la contraseña.
     */
    public LiveData<Boolean> resetPassword(String email) {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        
        MutableLiveData<Boolean> resetResult = authRepository.resetPassword(email);
        
        // Observar el resultado para actualizar el estado de carga
        resetResult.observeForever(result -> {
            isLoading.setValue(false);
            if (Boolean.FALSE.equals(result)) {
                errorMessage.setValue("Error al enviar correo de recuperación. Verifica tu email e intenta nuevamente.");
            }
        });
        
        return resetResult;
    }

    /**
     * Cierra la sesión del usuario actual.
     */
    public void logout() {
        authRepository.logout();
    }

    /**
     * Verifica si hay un usuario con sesión activa.
     */
    public boolean isUserLoggedIn() {
        return authRepository.isUserLoggedIn();
    }

    /**
     * Obtiene el usuario actual.
     */
    public FirebaseUser getCurrentUser() {
        return authRepository.getCurrentUser();
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
