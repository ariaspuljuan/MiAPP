package com.skillswap.skillswapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.skillswap.skillswapp.data.model.User;
import com.skillswap.skillswapp.data.repository.FavoriteRepository;

import java.util.List;
import java.util.Set;

/**
 * ViewModel para gestionar los favoritos.
 */
public class FavoriteViewModel extends ViewModel {

    private final FavoriteRepository favoriteRepository;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public FavoriteViewModel() {
        this.favoriteRepository = FavoriteRepository.getInstance();
    }

    /**
     * Obtiene los usuarios favoritos de un usuario.
     * @param userId ID del usuario
     * @return LiveData con la lista de usuarios favoritos
     */
    public LiveData<List<User>> getFavoriteUsers(String userId) {
        isLoading.setValue(true);
        LiveData<List<User>> favorites = favoriteRepository.getFavoriteUsers(userId);
        isLoading.setValue(false);
        return favorites;
    }

    /**
     * Obtiene los IDs de los usuarios favoritos de un usuario.
     * @param userId ID del usuario
     * @return LiveData con el conjunto de IDs de usuarios favoritos
     */
    public LiveData<Set<String>> getFavoriteUserIds(String userId) {
        isLoading.setValue(true);
        LiveData<Set<String>> favoriteIds = favoriteRepository.getFavoriteUserIds(userId);
        isLoading.setValue(false);
        return favoriteIds;
    }

    /**
     * Verifica si un usuario es favorito.
     * @param userId ID del usuario
     * @param favoriteUserId ID del usuario a verificar
     * @return LiveData con el resultado (true si es favorito)
     */
    public LiveData<Boolean> isFavorite(String userId, String favoriteUserId) {
        isLoading.setValue(true);
        LiveData<Boolean> result = favoriteRepository.isFavorite(userId, favoriteUserId);
        isLoading.setValue(false);
        return result;
    }

    /**
     * Agrega un usuario a favoritos.
     * @param favoriteUserId ID del usuario a agregar a favoritos
     * @return LiveData con el resultado (true si se agregó correctamente)
     */
    public LiveData<Boolean> addFavorite(String favoriteUserId) {
        isLoading.setValue(true);
        LiveData<Boolean> result = favoriteRepository.addFavorite(favoriteUserId);
        isLoading.setValue(false);
        return result;
    }

    /**
     * Elimina un usuario de favoritos.
     * @param favoriteUserId ID del usuario a eliminar de favoritos
     * @return LiveData con el resultado (true si se eliminó correctamente)
     */
    public LiveData<Boolean> removeFavorite(String favoriteUserId) {
        isLoading.setValue(true);
        LiveData<Boolean> result = favoriteRepository.removeFavorite(favoriteUserId);
        isLoading.setValue(false);
        return result;
    }

    /**
     * Obtiene el estado de carga.
     * @return LiveData con el estado de carga
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
}
