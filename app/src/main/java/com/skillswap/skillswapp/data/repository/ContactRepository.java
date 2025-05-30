package com.skillswap.skillswapp.data.repository;

import android.content.Context;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.skillswap.skillswapp.data.local.ImageStorageManager;
import com.skillswap.skillswapp.data.model.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repositorio para manejar los contactos recientes en Firebase Realtime Database.
 */
public class ContactRepository {
    private DatabaseReference databaseRef;
    private DatabaseReference contactsRef;
    private DatabaseReference usersRef;
    private static ContactRepository instance;
    private UserRepository userRepository;
    private ImageStorageManager imageStorageManager;
    private Context context;

    private ContactRepository() {
        databaseRef = FirebaseDatabase.getInstance().getReference();
        contactsRef = databaseRef.child("recent_contacts");
        usersRef = databaseRef.child("users");
        userRepository = UserRepository.getInstance();
    }
    
    /**
     * Inicializa el contexto para acceder al almacenamiento local de imágenes.
     * Debe llamarse después de crear la instancia del repositorio.
     * @param context Contexto de la aplicación
     */
    public void initContext(Context context) {
        this.context = context.getApplicationContext();
        this.imageStorageManager = ImageStorageManager.getInstance(this.context);
        this.userRepository.initContext(this.context);
    }

    public static ContactRepository getInstance() {
        if (instance == null) {
            instance = new ContactRepository();
        }
        return instance;
    }

    /**
     * Agrega un contacto reciente para el usuario actual.
     * @param contactUserId ID del usuario a agregar como contacto reciente
     * @return LiveData con el resultado (true si se agregó correctamente)
     */
    public MutableLiveData<Boolean> addRecentContact(String contactUserId) {
        MutableLiveData<Boolean> addResult = new MutableLiveData<>();
        
        try {
            // Verificar que el usuario esté autenticado
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                addResult.setValue(false);
                return addResult;
            }
            
            // Obtener el ID del usuario actual
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            
            // Crear un mapa con los datos del contacto
            Map<String, Object> contactData = new HashMap<>();
            contactData.put("timestamp", new Date().getTime());
            
            // Guardar el contacto en la base de datos de contactos recientes
            contactsRef.child(currentUserId).child(contactUserId).setValue(contactData)
                    .addOnSuccessListener(aVoid -> {
                        // También guardar en la colección de usuarios para compatibilidad
                        FirebaseDatabase.getInstance().getReference()
                                .child("users")
                                .child(currentUserId)
                                .child("recent_contacts")
                                .child(contactUserId)
                                .setValue(new Date().getTime())
                                .addOnSuccessListener(aVoid2 -> addResult.setValue(true))
                                .addOnFailureListener(e -> addResult.setValue(true)); // Aún consideramos éxito si solo se guardó en contactsRef
                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                        addResult.setValue(false);
                    });
        } catch (Exception e) {
            e.printStackTrace();
            addResult.setValue(false);
        }
        
        return addResult;
    }

    /**
     * Obtiene los contactos recientes del usuario actual.
     * @param userId ID del usuario
     * @return LiveData con la lista de usuarios contactados recientemente
     */
    public MutableLiveData<List<User>> getRecentContacts(String userId) {
        MutableLiveData<List<User>> contactsLiveData = new MutableLiveData<>();
        
        // Obtener los contactos recientes ordenados por timestamp (más recientes primero)
        Query query = contactsRef.child(userId).orderByChild("timestamp");
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> contactUserIds = new ArrayList<>();
                
                // Recorrer los contactos en orden inverso (más recientes primero)
                for (DataSnapshot contactSnapshot : dataSnapshot.getChildren()) {
                    contactUserIds.add(0, contactSnapshot.getKey());
                }
                
                // Obtener los datos de los usuarios
                getUsersFromIds(contactUserIds, contactsLiveData);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                contactsLiveData.setValue(new ArrayList<>());
            }
        });
        
        return contactsLiveData;
    }

    /**
     * Método auxiliar para obtener los datos de los usuarios a partir de sus IDs.
     */
    private void getUsersFromIds(List<String> userIds, MutableLiveData<List<User>> usersLiveData) {
        if (userIds.isEmpty()) {
            usersLiveData.setValue(new ArrayList<>());
            return;
        }
        
        List<User> users = new ArrayList<>();
        final int[] remaining = {userIds.size()};
        
        for (String userId : userIds) {
            userRepository.getUserById(userId).observeForever(user -> {
                if (user != null) {
                    users.add(user);
                }
                
                remaining[0]--;
                if (remaining[0] == 0) {
                    usersLiveData.setValue(users);
                }
            });
        }
    }
}
