package com.skillswap.skillswapp.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.skillswap.skillswapp.R;
import com.skillswap.skillswapp.databinding.FragmentResetPasswordBinding;
import com.skillswap.skillswapp.util.UiUtils;
import com.skillswap.skillswapp.util.ValidationUtils;
import com.skillswap.skillswapp.viewmodel.AuthViewModel;

/**
 * Fragmento para la pantalla de recuperación de contraseña.
 */
public class ResetPasswordFragment extends Fragment {

    private FragmentResetPasswordBinding binding;
    private AuthViewModel authViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentResetPasswordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Inicializar ViewModel
        authViewModel = new AuthViewModel();
        
        setupListeners();
        observeViewModel();
    }

    private void setupListeners() {
        // Botón para enviar enlace de recuperación
        binding.btnReset.setOnClickListener(v -> attemptResetPassword());
        
        // Botón para volver a inicio de sesión
        binding.tvBackToLogin.setOnClickListener(v -> navigateToLogin());
        
        // Icono para volver atrás
        binding.ivBack.setOnClickListener(v -> navigateToLogin());
    }

    private void observeViewModel() {
        // Observar estado de carga
        authViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                showLoading();
            } else {
                hideLoading();
            }
        });
        
        // Observar mensajes de error
        authViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                UiUtils.showSnackbar(binding.getRoot(), errorMessage);
            }
        });
    }

    private void attemptResetPassword() {
        // Obtener email del formulario
        String email = binding.etEmail.getText().toString().trim();
        
        // Validar email
        if (!validateEmail(email)) {
            return;
        }
        
        // Intentar enviar enlace de recuperación
        authViewModel.resetPassword(email).observe(getViewLifecycleOwner(), isSuccess -> {
            if (isSuccess) {
                UiUtils.showSnackbar(binding.getRoot(), getString(R.string.success_reset_password));
                navigateToLogin();
            }
        });
    }

    private boolean validateEmail(String email) {
        if (!ValidationUtils.isValidEmail(email)) {
            binding.tilEmail.setError(getString(R.string.error_invalid_email));
            return false;
        } else {
            binding.tilEmail.setError(null);
            return true;
        }
    }

    private void showLoading() {
        binding.btnReset.setEnabled(false);
        binding.shimmerLayout.setVisibility(View.VISIBLE);
        binding.shimmerLayout.startShimmer();
        binding.tilEmail.setEnabled(false);
        binding.tvBackToLogin.setEnabled(false);
        binding.ivBack.setEnabled(false);
    }

    private void hideLoading() {
        binding.btnReset.setEnabled(true);
        binding.shimmerLayout.stopShimmer();
        binding.shimmerLayout.setVisibility(View.GONE);
        binding.tilEmail.setEnabled(true);
        binding.tvBackToLogin.setEnabled(true);
        binding.ivBack.setEnabled(true);
    }

    private void navigateToLogin() {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigate(R.id.action_resetPasswordFragment_to_loginFragment);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
