package com.skillswap.skillswapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.skillswap.skillswapp.data.model.Category;
import com.skillswap.skillswapp.data.repository.CategoryRepository;

import java.util.List;

/**
 * ViewModel para gestionar las categorías.
 */
public class CategoryViewModel extends ViewModel {

    private final CategoryRepository categoryRepository;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public CategoryViewModel() {
        this.categoryRepository = CategoryRepository.getInstance();
    }

    /**
     * Obtiene todas las categorías.
     * @return LiveData con la lista de categorías
     */
    public LiveData<List<Category>> getAllCategories() {
        isLoading.setValue(true);
        LiveData<List<Category>> categories = categoryRepository.getAllCategories();
        isLoading.setValue(false);
        return categories;
    }

    /**
     * Obtiene una categoría por su ID.
     * @param categoryId ID de la categoría
     * @return LiveData con la categoría
     */
    public LiveData<Category> getCategoryById(String categoryId) {
        isLoading.setValue(true);
        LiveData<Category> category = categoryRepository.getCategoryById(categoryId);
        isLoading.setValue(false);
        return category;
    }

    /**
     * Busca categorías por nombre.
     * @param query Texto de búsqueda
     * @return LiveData con la lista de categorías que coinciden
     */
    public LiveData<List<Category>> searchCategories(String query) {
        isLoading.setValue(true);
        LiveData<List<Category>> categories = categoryRepository.searchCategories(query);
        isLoading.setValue(false);
        return categories;
    }

    /**
     * Obtiene el estado de carga.
     * @return LiveData con el estado de carga
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
}
