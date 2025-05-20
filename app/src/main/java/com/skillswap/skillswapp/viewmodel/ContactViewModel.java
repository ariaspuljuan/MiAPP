package com.skillswap.skillswapp.viewmodel;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.skillswap.skillswapp.data.local.LocalStorageManager;
import com.skillswap.skillswapp.data.model.User;
import com.skillswap.skillswapp.data.repository.ContactRepository;

import java.util.List;

/**
 * ViewModel para gestionar los contactos recientes.
 */
public class ContactViewModel extends ViewModel {

    private final ContactRepository contactRepository;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private LocalStorageManager localStorageManager;
    private Context context;

    public ContactViewModel() {
        this.contactRepository = ContactRepository.getInstance();
    }
    
    /**
     * Inicializa el contexto para acceder al almacenamiento local.
     * Debe llamarse después de crear el ViewModel.
     * @param context Contexto de la aplicación
     */
    public void initContext(Context context) {
        this.context = context.getApplicationContext();
        this.localStorageManager = LocalStorageManager.getInstance(this.context);
        this.contactRepository.initContext(this.context);
    }
    
    /**
     * Obtiene el mensaje de error.
     * @return LiveData con el mensaje de error
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Obtiene los contactos recientes de un usuario.
     * @param userId ID del usuario
     * @return LiveData con la lista de contactos recientes
     */
    public LiveData<List<User>> getRecentContacts(String userId) {
        isLoading.setValue(true);
        LiveData<List<User>> contacts = contactRepository.getRecentContacts(userId);
        isLoading.setValue(false);
        return contacts;
    }

    /**
     * Agrega un contacto reciente.
     * @param contactUserId ID del usuario a agregar como contacto reciente
     * @return LiveData con el resultado (true si se agregó correctamente)
     */
    public LiveData<Boolean> addRecentContact(String contactUserId) {
        isLoading.setValue(true);
        LiveData<Boolean> result = contactRepository.addRecentContact(contactUserId);
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
