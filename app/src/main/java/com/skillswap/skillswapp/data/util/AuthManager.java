package com.skillswap.skillswapp.data.util;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.skillswap.skillswapp.data.model.User;
import com.skillswap.skillswapp.data.repository.UserRepository;

/**
 * Clase utilitaria para gestionar la autenticación de usuarios con Firebase.
 */
public class AuthManager {
    private FirebaseAuth firebaseAuth;
    private UserRepository userRepository;
    private static AuthManager instance;
    
    private AuthManager() {
        firebaseAuth = FirebaseAuth.getInstance();
        userRepository = UserRepository.getInstance();
    }
    
    public static AuthManager getInstance() {
        if (instance == null) {
            instance = new AuthManager();
        }
        return instance;
    }
    
    /**
     * Registra un nuevo usuario con email y contraseña.
     */
    public MutableLiveData<Boolean> registerUser(String name, String email, String password) {
        MutableLiveData<Boolean> registerResult = new MutableLiveData<>();
        
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        
                        // Actualizar el perfil del usuario en Firebase Auth
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build();
                        
                        firebaseUser.updateProfile(profileUpdates)
                                .addOnCompleteListener(profileTask -> {
                                    // Crear el usuario en la base de datos
                                    User user = new User(firebaseUser.getUid(), name, email);
                                    userRepository.createUser(user).observeForever(createResult -> {
                                        registerResult.setValue(createResult);
                                    });
                                });
                    } else {
                        registerResult.setValue(false);
                    }
                });
        
        return registerResult;
    }
    
    /**
     * Inicia sesión con email y contraseña.
     */
    public MutableLiveData<Boolean> loginUser(String email, String password) {
        MutableLiveData<Boolean> loginResult = new MutableLiveData<>();
        
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    loginResult.setValue(task.isSuccessful());
                });
        
        return loginResult;
    }
    
    /**
     * Cierra la sesión del usuario actual.
     */
    public void logoutUser() {
        firebaseAuth.signOut();
    }
    
    /**
     * Envía un correo para restablecer la contraseña.
     */
    public MutableLiveData<Boolean> resetPassword(String email) {
        MutableLiveData<Boolean> resetResult = new MutableLiveData<>();
        
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    resetResult.setValue(task.isSuccessful());
                });
        
        return resetResult;
    }
    
    /**
     * Obtiene el usuario actualmente autenticado.
     */
    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }
    
    /**
     * Verifica si hay un usuario autenticado.
     */
    public boolean isUserLoggedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }
    
    /**
     * Actualiza el perfil del usuario en Firebase Auth.
     */
    public MutableLiveData<Boolean> updateUserProfile(String name, String photoUrl) {
        MutableLiveData<Boolean> updateResult = new MutableLiveData<>();
        
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest.Builder profileUpdates = new UserProfileChangeRequest.Builder();
            
            if (name != null && !name.isEmpty()) {
                profileUpdates.setDisplayName(name);
            }
            
            if (photoUrl != null && !photoUrl.isEmpty()) {
                profileUpdates.setPhotoUri(android.net.Uri.parse(photoUrl));
            }
            
            user.updateProfile(profileUpdates.build())
                    .addOnCompleteListener(task -> {
                        updateResult.setValue(task.isSuccessful());
                    });
        } else {
            updateResult.setValue(false);
        }
        
        return updateResult;
    }
    
    /**
     * Actualiza el email del usuario.
     */
    public MutableLiveData<Boolean> updateUserEmail(String email) {
        MutableLiveData<Boolean> updateResult = new MutableLiveData<>();
        
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            user.updateEmail(email)
                    .addOnCompleteListener(task -> {
                        updateResult.setValue(task.isSuccessful());
                    });
        } else {
            updateResult.setValue(false);
        }
        
        return updateResult;
    }
    
    /**
     * Actualiza la contraseña del usuario.
     */
    public MutableLiveData<Boolean> updateUserPassword(String password) {
        MutableLiveData<Boolean> updateResult = new MutableLiveData<>();
        
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            user.updatePassword(password)
                    .addOnCompleteListener(task -> {
                        updateResult.setValue(task.isSuccessful());
                    });
        } else {
            updateResult.setValue(false);
        }
        
        return updateResult;
    }
}
