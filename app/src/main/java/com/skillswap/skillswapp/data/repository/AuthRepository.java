package com.skillswap.skillswapp.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.skillswap.skillswapp.data.model.User;

import java.util.HashMap;

/**
 * Repositorio para manejar la autenticación de usuarios con Firebase.
 */
public class AuthRepository {
    private FirebaseAuth firebaseAuth;
    private DatabaseReference usersRef;
    private static AuthRepository instance;

    private AuthRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
    }

    public static AuthRepository getInstance() {
        if (instance == null) {
            instance = new AuthRepository();
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
                    if (task.isSuccessful() && task.getResult().getUser() != null) {
                        // Crear perfil de usuario en la base de datos
                        FirebaseUser firebaseUser = task.getResult().getUser();
                        User newUser = new User(firebaseUser.getUid(), name, email);
                        
                        // Asegurarse de que los maps de habilidades estén inicializados
                        if (newUser.getSkillsToTeach() == null) {
                            newUser.setSkillsToTeach(new HashMap<>());
                        }
                        if (newUser.getSkillsToLearn() == null) {
                            newUser.setSkillsToLearn(new HashMap<>());
                        }
                        
                        usersRef.child(firebaseUser.getUid()).setValue(newUser.toMap())
                                .addOnSuccessListener(aVoid -> registerResult.setValue(true))
                                .addOnFailureListener(e -> registerResult.setValue(false));
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
                .addOnCompleteListener(task -> loginResult.setValue(task.isSuccessful()));
        
        return loginResult;
    }

    /**
     * Envía un correo para restablecer la contraseña.
     */
    public MutableLiveData<Boolean> resetPassword(String email) {
        MutableLiveData<Boolean> resetResult = new MutableLiveData<>();
        
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> resetResult.setValue(task.isSuccessful()));
        
        return resetResult;
    }

    /**
     * Cierra la sesión del usuario actual.
     */
    public void logout() {
        firebaseAuth.signOut();
    }

    /**
     * Verifica si hay un usuario con sesión activa.
     */
    public boolean isUserLoggedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }

    /**
     * Obtiene el usuario actual.
     */
    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }
}
