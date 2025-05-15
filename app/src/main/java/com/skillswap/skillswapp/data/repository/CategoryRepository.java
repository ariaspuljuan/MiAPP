package com.skillswap.skillswapp.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.skillswap.skillswapp.data.model.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * Repositorio para manejar las categorías de habilidades en Firebase Realtime Database.
 */
public class CategoryRepository {
    private DatabaseReference databaseRef;
    private DatabaseReference categoriesRef;
    private static CategoryRepository instance;

    private CategoryRepository() {
        databaseRef = FirebaseDatabase.getInstance().getReference();
        categoriesRef = databaseRef.child("categories");
    }

    public static CategoryRepository getInstance() {
        if (instance == null) {
            instance = new CategoryRepository();
        }
        return instance;
    }

    /**
     * Crea o actualiza una categoría en la base de datos.
     */
    public MutableLiveData<Boolean> saveCategory(Category category) {
        MutableLiveData<Boolean> saveResult = new MutableLiveData<>();
        
        // Si no tiene ID, generar uno nuevo
        if (category.getCategoryId() == null || category.getCategoryId().isEmpty()) {
            category.setCategoryId(categoriesRef.push().getKey());
        }
        
        categoriesRef.child(category.getCategoryId()).setValue(category.toMap())
                .addOnSuccessListener(aVoid -> saveResult.setValue(true))
                .addOnFailureListener(e -> saveResult.setValue(false));
        
        return saveResult;
    }

    /**
     * Obtiene una categoría por su ID.
     */
    public MutableLiveData<Category> getCategoryById(String categoryId) {
        MutableLiveData<Category> categoryLiveData = new MutableLiveData<>();
        
        categoriesRef.child(categoryId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        Category category = new Category();
                        category.setCategoryId(dataSnapshot.getKey());
                        category.setName(dataSnapshot.child("name").getValue(String.class));
                        category.setDescription(dataSnapshot.child("description").getValue(String.class));
                        category.setIconUrl(dataSnapshot.child("icon_url").getValue(String.class));
                        
                        categoryLiveData.setValue(category);
                    } catch (Exception e) {
                        categoryLiveData.setValue(null);
                    }
                } else {
                    categoryLiveData.setValue(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                categoryLiveData.setValue(null);
            }
        });
        
        return categoryLiveData;
    }

    /**
     * Obtiene todas las categorías.
     */
    public MutableLiveData<List<Category>> getAllCategories() {
        MutableLiveData<List<Category>> categoriesLiveData = new MutableLiveData<>();
        
        categoriesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Category> categories = new ArrayList<>();
                
                for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                    try {
                        Category category = new Category();
                        category.setCategoryId(categorySnapshot.getKey());
                        category.setName(categorySnapshot.child("name").getValue(String.class));
                        category.setDescription(categorySnapshot.child("description").getValue(String.class));
                        category.setIconUrl(categorySnapshot.child("icon_url").getValue(String.class));
                        
                        categories.add(category);
                    } catch (Exception e) {
                        // Ignorar categorías con formato incorrecto
                    }
                }
                
                categoriesLiveData.setValue(categories);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                categoriesLiveData.setValue(new ArrayList<>());
            }
        });
        
        return categoriesLiveData;
    }

    /**
     * Elimina una categoría.
     */
    public MutableLiveData<Boolean> deleteCategory(String categoryId) {
        MutableLiveData<Boolean> deleteResult = new MutableLiveData<>();
        
        categoriesRef.child(categoryId).removeValue()
                .addOnSuccessListener(aVoid -> deleteResult.setValue(true))
                .addOnFailureListener(e -> deleteResult.setValue(false));
        
        return deleteResult;
    }
    
    /**
     * Busca categorías por nombre.
     * @param query Texto de búsqueda
     * @return LiveData con la lista de categorías que coinciden
     */
    public MutableLiveData<List<Category>> searchCategories(String query) {
        MutableLiveData<List<Category>> categoriesLiveData = new MutableLiveData<>();
        
        // Si la consulta está vacía, devolver todas las categorías
        if (query == null || query.trim().isEmpty()) {
            return getAllCategories();
        }
        
        // Convertir a minúsculas para búsqueda sin distinción entre mayúsculas y minúsculas
        final String queryLowerCase = query.toLowerCase().trim();
        
        categoriesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Category> categories = new ArrayList<>();
                
                for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                    try {
                        String name = categorySnapshot.child("name").getValue(String.class);
                        
                        // Verificar si el nombre contiene la consulta
                        if (name != null && name.toLowerCase().contains(queryLowerCase)) {
                            Category category = new Category();
                            category.setCategoryId(categorySnapshot.getKey());
                            category.setName(name);
                            category.setDescription(categorySnapshot.child("description").getValue(String.class));
                            category.setIconUrl(categorySnapshot.child("icon_url").getValue(String.class));
                            
                            categories.add(category);
                        }
                    } catch (Exception e) {
                        // Ignorar categorías con formato incorrecto
                    }
                }
                
                categoriesLiveData.setValue(categories);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                categoriesLiveData.setValue(new ArrayList<>());
            }
        });
        
        return categoriesLiveData;
    }
}
