package com.skillswap.skillswapp.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Clase de utilidad para manejar elementos de la interfaz de usuario.
 */
public class UiUtils {

    /**
     * Muestra un mensaje de error en un TextInputLayout.
     * @param textInputLayout El TextInputLayout donde mostrar el error
     * @param errorMessage El mensaje de error a mostrar
     */
    public static void showError(TextInputLayout textInputLayout, String errorMessage) {
        textInputLayout.setError(errorMessage);
    }

    /**
     * Limpia el error de un TextInputLayout.
     * @param textInputLayout El TextInputLayout donde limpiar el error
     */
    public static void clearError(TextInputLayout textInputLayout) {
        textInputLayout.setError(null);
    }

    /**
     * Muestra un Snackbar con un mensaje.
     * @param view La vista donde mostrar el Snackbar
     * @param message El mensaje a mostrar
     */
    public static void showSnackbar(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
    }

    /**
     * Muestra un Snackbar con un mensaje y una acción.
     * @param view La vista donde mostrar el Snackbar
     * @param message El mensaje a mostrar
     * @param actionText El texto del botón de acción
     * @param action La acción a realizar al hacer clic en el botón
     */
    public static void showSnackbarWithAction(View view, String message, String actionText, View.OnClickListener action) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                .setAction(actionText, action)
                .show();
    }

    /**
     * Anima la visibilidad de una vista.
     * @param view La vista a animar
     * @param toVisibility La visibilidad final (View.VISIBLE o View.GONE)
     * @param duration La duración de la animación en milisegundos
     */
    public static void animateViewVisibility(final View view, final int toVisibility, int duration) {
        if (toVisibility == View.VISIBLE) {
            view.setAlpha(0f);
            view.setVisibility(View.VISIBLE);
            view.animate()
                    .alpha(1f)
                    .setDuration(duration)
                    .setListener(null);
        } else {
            view.animate()
                    .alpha(0f)
                    .setDuration(duration)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            view.setVisibility(toVisibility);
                        }
                    });
        }
    }

    /**
     * Aplica una animación a una vista.
     * @param context El contexto
     * @param view La vista a animar
     * @param animationResId El ID del recurso de animación
     */
    public static void applyAnimation(Context context, View view, int animationResId) {
        Animation animation = AnimationUtils.loadAnimation(context, animationResId);
        view.startAnimation(animation);
    }
}
