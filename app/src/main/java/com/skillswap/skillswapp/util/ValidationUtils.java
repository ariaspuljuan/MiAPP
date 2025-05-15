package com.skillswap.skillswapp.util;

import android.text.TextUtils;
import android.util.Patterns;

/**
 * Clase de utilidad para validar entradas de usuario.
 */
public class ValidationUtils {

    /**
     * Valida un correo electrónico.
     * @param email Correo a validar
     * @return true si el correo es válido, false en caso contrario
     */
    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Valida una contraseña.
     * La contraseña debe tener al menos 6 caracteres.
     * @param password Contraseña a validar
     * @return true si la contraseña es válida, false en caso contrario
     */
    public static boolean isValidPassword(String password) {
        return !TextUtils.isEmpty(password) && password.length() >= 6;
    }

    /**
     * Valida un nombre de usuario.
     * El nombre debe tener al menos 3 caracteres.
     * @param name Nombre a validar
     * @return true si el nombre es válido, false en caso contrario
     */
    public static boolean isValidName(String name) {
        return !TextUtils.isEmpty(name) && name.length() >= 3;
    }

    /**
     * Valida que dos contraseñas coincidan.
     * @param password Contraseña
     * @param confirmPassword Confirmación de contraseña
     * @return true si las contraseñas coinciden, false en caso contrario
     */
    public static boolean passwordsMatch(String password, String confirmPassword) {
        return password.equals(confirmPassword);
    }
}
