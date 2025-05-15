package com.skillswap.skillswapp;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.skillswap.skillswapp.data.util.DatabaseInitializer;

/**
 * Clase de aplicación principal para SkillSwap.
 * Se encarga de inicializar Firebase y otros componentes necesarios.
 */
public class SkillSwapApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Inicializar Firebase
        FirebaseApp.initializeApp(this);
        
        // Inicializar la base de datos con datos predeterminados
        new DatabaseInitializer().initializeDatabase();
    }
}
