package com.skillswap.skillswapp.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.skillswap.skillswapp.R;
import com.skillswap.skillswapp.databinding.FragmentLoginBinding;
import com.skillswap.skillswapp.util.UiUtils;
import com.skillswap.skillswapp.util.ValidationUtils;
import com.skillswap.skillswapp.viewmodel.AuthViewModel;

/**
 * Fragmento para la pantalla de inicio de sesión.
 */
public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private AuthViewModel authViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
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
        // Botón de inicio de sesión
        binding.btnLogin.setOnClickListener(v -> attemptLogin());
        
        // Texto para ir a registro
        binding.tvDontHaveAccount.setOnClickListener(v -> navigateToRegister());
        
        // Texto para recuperar contraseña
        binding.tvForgotPassword.setOnClickListener(v -> navigateToResetPassword());
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

    private void attemptLogin() {
        // Obtener datos del formulario
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        boolean rememberMe = binding.cbRememberMe.isChecked();
        
        // Validar datos
        if (!validateForm(email, password)) {
            return;
        }
        
        // Intentar inicio de sesión
        authViewModel.login(email, password).observe(getViewLifecycleOwner(), isSuccess -> {
            if (isSuccess) {
                UiUtils.showSnackbar(binding.getRoot(), getString(R.string.success_login));
                navigateToMain();
            }
        });
    }

    private boolean validateForm(String email, String password) {
        boolean isValid = true;
        
        // Validar email
        if (!ValidationUtils.isValidEmail(email)) {
            binding.tilEmail.setError(getString(R.string.error_invalid_email));
            isValid = false;
        } else {
            binding.tilEmail.setError(null);
        }
        
        // Validar contraseña
        if (!ValidationUtils.isValidPassword(password)) {
            binding.tilPassword.setError(getString(R.string.error_invalid_password));
            isValid = false;
        } else {
            binding.tilPassword.setError(null);
        }
        
        return isValid;
    }

    private void showLoading() {
        binding.btnLogin.setEnabled(false);
        binding.shimmerLayout.setVisibility(View.VISIBLE);
        binding.shimmerLayout.startShimmer();
        binding.tilEmail.setEnabled(false);
        binding.tilPassword.setEnabled(false);
        binding.cbRememberMe.setEnabled(false);
        binding.tvForgotPassword.setEnabled(false);
        binding.tvDontHaveAccount.setEnabled(false);
    }

    private void hideLoading() {
        binding.btnLogin.setEnabled(true);
        binding.shimmerLayout.stopShimmer();
        binding.shimmerLayout.setVisibility(View.GONE);
        binding.tilEmail.setEnabled(true);
        binding.tilPassword.setEnabled(true);
        binding.cbRememberMe.setEnabled(true);
        binding.tvForgotPassword.setEnabled(true);
        binding.tvDontHaveAccount.setEnabled(true);
    }

    private void navigateToRegister() {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigate(R.id.action_loginFragment_to_registerFragment);
    }

    private void navigateToResetPassword() {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigate(R.id.action_loginFragment_to_resetPasswordFragment);
    }

    private void navigateToMain() {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigate(R.id.action_loginFragment_to_mainFragment);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
