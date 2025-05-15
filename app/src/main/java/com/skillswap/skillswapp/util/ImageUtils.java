package com.skillswap.skillswapp.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.skillswap.skillswapp.R;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Clase de utilidad para manejar operaciones con imágenes.
 */
public class ImageUtils {

    private static final String TAG = "ImageUtils";
    private static final String PROFILE_IMAGES_PATH = "profile_images/";
    private static final int MAX_IMAGE_SIZE = 1024; // Tamaño máximo en píxeles

    /**
     * Carga una imagen desde una URL en un ImageView utilizando Glide.
     *
     * @param context   Contexto de la aplicación
     * @param imageUrl  URL de la imagen
     * @param imageView ImageView donde cargar la imagen
     */
    public static void loadImage(Context context, String imageUrl, ImageView imageView) {
        if (context == null || imageView == null) {
            return;
        }

        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder)
                .into(imageView);
    }

    /**
     * Carga una imagen de perfil circular desde una URL en un ImageView.
     *
     * @param context   Contexto de la aplicación
     * @param imageUrl  URL de la imagen
     * @param imageView ImageView donde cargar la imagen
     */
    public static void loadProfileImage(Context context, String imageUrl, ImageView imageView) {
        if (context == null || imageView == null) {
            return;
        }

        Glide.with(context)
                .load(imageUrl)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder)
                .into(imageView);
    }

    /**
     * Sube una imagen a Firebase Storage y devuelve la URL de descarga.
     *
     * @param context     Contexto de la aplicación
     * @param imageUri    URI de la imagen a subir
     * @param userId      ID del usuario (para nombrar el archivo)
     * @param callback    Callback para manejar el resultado
     */
    public static void uploadProfileImage(Context context, Uri imageUri, String userId, 
                                          OnImageUploadListener callback) {
        if (imageUri == null || userId == null || userId.isEmpty()) {
            if (callback != null) {
                callback.onFailure("Parámetros inválidos");
            }
            return;
        }

        try {
            // Comprimir la imagen antes de subirla
            InputStream imageStream = context.getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
            
            // Redimensionar si es necesario
            bitmap = resizeBitmap(bitmap, MAX_IMAGE_SIZE);
            
            // Comprimir a JPEG
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] imageData = baos.toByteArray();

            // Crear referencia en Firebase Storage
            String fileName = userId + "_" + UUID.randomUUID().toString() + ".jpg";
            StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                    .child(PROFILE_IMAGES_PATH + fileName);

            // Subir la imagen
            UploadTask uploadTask = storageRef.putBytes(imageData);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                // Obtener URL de descarga
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    if (callback != null) {
                        callback.onSuccess(uri.toString());
                    }
                }).addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onFailure(e.getMessage());
                    }
                });
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error al subir imagen: " + e.getMessage());
                if (callback != null) {
                    callback.onFailure(e.getMessage());
                }
            });

        } catch (FileNotFoundException e) {
            Log.e(TAG, "Archivo no encontrado: " + e.getMessage());
            if (callback != null) {
                callback.onFailure("Archivo no encontrado");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al procesar imagen: " + e.getMessage());
            if (callback != null) {
                callback.onFailure("Error al procesar imagen");
            }
        }
    }

    /**
     * Redimensiona un bitmap si es más grande que el tamaño máximo especificado.
     *
     * @param bitmap    Bitmap a redimensionar
     * @param maxSize   Tamaño máximo en píxeles
     * @return          Bitmap redimensionado
     */
    private static Bitmap resizeBitmap(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        if (width <= maxSize && height <= maxSize) {
            return bitmap;
        }
        
        float ratio = (float) width / (float) height;
        
        if (ratio > 1) {
            // Imagen horizontal
            width = maxSize;
            height = (int) (width / ratio);
        } else {
            // Imagen vertical o cuadrada
            height = maxSize;
            width = (int) (height * ratio);
        }
        
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    /**
     * Elimina una imagen de Firebase Storage.
     *
     * @param imageUrl URL de la imagen a eliminar
     */
    public static void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }
        
        try {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl(imageUrl);
            storageRef.delete().addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Imagen eliminada correctamente");
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error al eliminar imagen: " + e.getMessage());
            });
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener referencia: " + e.getMessage());
        }
    }

    /**
     * Interfaz para manejar el resultado de la subida de imágenes.
     */
    public interface OnImageUploadListener {
        void onSuccess(String imageUrl);
        void onFailure(String errorMessage);
    }
}
