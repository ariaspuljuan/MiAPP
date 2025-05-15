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
import com.skillswap.skillswapp.databinding.FragmentRegisterBinding;
import com.skillswap.skillswapp.util.UiUtils;
import com.skillswap.skillswapp.util.ValidationUtils;
import com.skillswap.skillswapp.viewmodel.AuthViewModel;

/**
 * Fragmento para la pantalla de registro de usuario.
 */
public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private AuthViewModel authViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
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
        // Botón de registro
        binding.btnRegister.setOnClickListener(v -> attemptRegister());
        
        // Texto para ir a inicio de sesión
        binding.tvAlreadyHaveAccount.setOnClickListener(v -> navigateToLogin());
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

    private void attemptRegister() {
        // Obtener datos del formulario
        String name = binding.etName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();
        boolean termsAccepted = binding.cbTerms.isChecked();
        
        // Validar datos
        if (!validateForm(name, email, password, confirmPassword, termsAccepted)) {
            return;
        }
        
        // Intentar registro
        authViewModel.register(name, email, password).observe(getViewLifecycleOwner(), isSuccess -> {
            if (isSuccess) {
                UiUtils.showSnackbar(binding.getRoot(), getString(R.string.success_register));
                navigateToProfileSetup();
            }
        });
    }

    private boolean validateForm(String name, String email, String password, String confirmPassword, boolean termsAccepted) {
        boolean isValid = true;
        
        // Validar nombre
        if (!ValidationUtils.isValidName(name)) {
            binding.tilName.setError(getString(R.string.error_invalid_name));
            isValid = false;
        } else {
            binding.tilName.setError(null);
        }
        
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
        
        // Validar confirmación de contraseña
        if (!ValidationUtils.passwordsMatch(password, confirmPassword)) {
            binding.tilConfirmPassword.setError(getString(R.string.error_passwords_dont_match));
            isValid = false;
        } else {
            binding.tilConfirmPassword.setError(null);
        }
        
        // Validar términos y condiciones
        if (!termsAccepted) {
            UiUtils.showSnackbar(binding.getRoot(), getString(R.string.error_terms_not_accepted));
            isValid = false;
        }
        
        return isValid;
    }

    private void showLoading() {
        binding.btnRegister.setEnabled(false);
        binding.shimmerLayout.setVisibility(View.VISIBLE);
        binding.shimmerLayout.startShimmer();
        binding.tilName.setEnabled(false);
        binding.tilEmail.setEnabled(false);
        binding.tilPassword.setEnabled(false);
        binding.tilConfirmPassword.setEnabled(false);
        binding.cbTerms.setEnabled(false);
        binding.tvAlreadyHaveAccount.setEnabled(false);
    }

    private void hideLoading() {
        binding.btnRegister.setEnabled(true);
        binding.shimmerLayout.stopShimmer();
        binding.shimmerLayout.setVisibility(View.GONE);
        binding.tilName.setEnabled(true);
        binding.tilEmail.setEnabled(true);
        binding.tilPassword.setEnabled(true);
        binding.tilConfirmPassword.setEnabled(true);
        binding.cbTerms.setEnabled(true);
        binding.tvAlreadyHaveAccount.setEnabled(true);
    }

    private void navigateToLogin() {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigate(R.id.action_registerFragment_to_loginFragment);
    }

    private void navigateToProfileSetup() {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigate(R.id.action_registerFragment_to_profileSetupFragment);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
