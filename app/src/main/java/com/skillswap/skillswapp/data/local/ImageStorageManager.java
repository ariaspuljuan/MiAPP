package com.skillswap.skillswapp.data.local;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Clase para gestionar el almacenamiento local de imágenes.
 */
public class ImageStorageManager {
    private static final String TAG = "ImageStorageManager";
    private static final String PROFILE_IMAGES_DIR = "profile_images";
    private static ImageStorageManager instance;
    private final Context context;

    private ImageStorageManager(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Obtiene una instancia del gestor de almacenamiento de imágenes.
     * @param context Contexto de la aplicación
     * @return Instancia del gestor
     */
    public static synchronized ImageStorageManager getInstance(Context context) {
        if (instance == null) {
            instance = new ImageStorageManager(context);
        }
        return instance;
    }

    /**
     * Guarda una imagen de perfil localmente.
     * @param userId ID del usuario
     * @param imageUri URI de la imagen a guardar
     * @return URI de la imagen guardada o null si ocurre un error
     */
    public String saveProfileImage(String userId, Uri imageUri) {
        try {
            // Crear directorio si no existe
            File directory = new File(context.getFilesDir(), PROFILE_IMAGES_DIR);
            if (!directory.exists()) {
                if (!directory.mkdirs()) {
                    Log.e(TAG, "No se pudo crear el directorio de imágenes");
                    return null;
                }
            }

            // Crear archivo para la imagen
            String fileName = "profile_" + userId + ".jpg";
            File outputFile = new File(directory, fileName);

            // Leer la imagen desde el URI y guardarla en el archivo
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                return null;
            }

            // Comprimir la imagen para reducir su tamaño
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            FileOutputStream outputStream = new FileOutputStream(outputFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
            outputStream.flush();
            outputStream.close();

            // Devolver la ruta del archivo
            return outputFile.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Error al guardar la imagen: " + e.getMessage());
            return null;
        }
    }

    /**
     * Obtiene la ruta de la imagen de perfil de un usuario.
     * @param userId ID del usuario
     * @return Ruta de la imagen o null si no existe
     */
    public String getProfileImagePath(String userId) {
        File directory = new File(context.getFilesDir(), PROFILE_IMAGES_DIR);
        String fileName = "profile_" + userId + ".jpg";
        File imageFile = new File(directory, fileName);
        
        if (imageFile.exists()) {
            return imageFile.getAbsolutePath();
        }
        
        return null;
    }

    /**
     * Elimina la imagen de perfil de un usuario.
     * @param userId ID del usuario
     * @return true si se eliminó correctamente, false en caso contrario
     */
    public boolean deleteProfileImage(String userId) {
        File directory = new File(context.getFilesDir(), PROFILE_IMAGES_DIR);
        String fileName = "profile_" + userId + ".jpg";
        File imageFile = new File(directory, fileName);
        
        return imageFile.exists() && imageFile.delete();
    }
}
