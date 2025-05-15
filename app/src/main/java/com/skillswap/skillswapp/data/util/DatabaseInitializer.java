package com.skillswap.skillswapp.data.util;

import android.util.Log;

import androidx.lifecycle.Observer;

import com.skillswap.skillswapp.data.model.Category;
import com.skillswap.skillswapp.data.repository.CategoryRepository;

import java.util.Arrays;
import java.util.List;

/**
 * Clase utilitaria para inicializar la base de datos con datos predeterminados.
 */
public class DatabaseInitializer {
    private static final String TAG = "DatabaseInitializer";
    
    // Lista de categorías predeterminadas
    private static final List<String> DEFAULT_CATEGORIES = Arrays.asList(
            "Tecnología", "Idiomas", "Música", "Arte", "Deportes", 
            "Cocina", "Educación", "Negocios", "Salud", "Hogar"
    );
    
    // Descripciones para las categorías predeterminadas
    private static final List<String> DEFAULT_DESCRIPTIONS = Arrays.asList(
            "Habilidades relacionadas con la tecnología, programación, diseño web, etc.",
            "Aprendizaje de idiomas, traducción, conversación, etc.",
            "Tocar instrumentos, canto, teoría musical, etc.",
            "Dibujo, pintura, escultura, fotografía, etc.",
            "Entrenamiento físico, deportes de equipo, deportes individuales, etc.",
            "Preparación de alimentos, repostería, nutrición, etc.",
            "Enseñanza, tutoría, metodologías de aprendizaje, etc.",
            "Emprendimiento, marketing, finanzas, administración, etc.",
            "Bienestar, primeros auxilios, ejercicio, meditación, etc.",
            "Jardinería, decoración, reparaciones, organización, etc."
    );
    
    private CategoryRepository categoryRepository;
    
    public DatabaseInitializer() {
        categoryRepository = CategoryRepository.getInstance();
    }
    
    /**
     * Inicializa la base de datos con categorías predeterminadas si no existen.
     */
    public void initializeDatabase() {
        // Verificar si ya existen categorías
        categoryRepository.getAllCategories().observeForever(new Observer<List<Category>>() {
            @Override
            public void onChanged(List<Category> categories) {
                // Si no hay categorías, crear las predeterminadas
                if (categories == null || categories.isEmpty()) {
                    createDefaultCategories();
                }
                // Dejar de observar después de la verificación inicial
                categoryRepository.getAllCategories().removeObserver(this);
            }
        });
    }
    
    /**
     * Crea las categorías predeterminadas en la base de datos.
     */
    private void createDefaultCategories() {
        for (int i = 0; i < DEFAULT_CATEGORIES.size(); i++) {
            String name = DEFAULT_CATEGORIES.get(i);
            String description = DEFAULT_DESCRIPTIONS.get(i);
            
            // Crear una nueva categoría
            Category category = new Category(null, name, description, "");
            
            // Guardar la categoría en la base de datos
            categoryRepository.saveCategory(category).observeForever(success -> {
                if (success) {
                    Log.d(TAG, "Categoría creada: " + name);
                } else {
                    Log.e(TAG, "Error al crear la categoría: " + name);
                }
            });
        }
    }
}
