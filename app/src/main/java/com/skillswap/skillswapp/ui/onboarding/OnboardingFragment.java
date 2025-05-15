package com.skillswap.skillswapp.ui.onboarding;

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
import com.skillswap.skillswapp.databinding.FragmentOnboardingBinding;
import com.skillswap.skillswapp.viewmodel.AuthViewModel;

/**
 * Fragmento para la pantalla de bienvenida (onboarding).
 */
public class OnboardingFragment extends Fragment {

    private FragmentOnboardingBinding binding;
    private AuthViewModel authViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOnboardingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Inicializar ViewModel
        authViewModel = new AuthViewModel();
        
        // Verificar si el usuario ya tiene sesión activa
        if (authViewModel.isUserLoggedIn()) {
            navigateToMain();
            return;
        }
        
        setupListeners();
    }

    private void setupListeners() {
        // Botón para comenzar el registro
        binding.btnGetStarted.setOnClickListener(v -> navigateToRegister());
        
        // Texto para ir a inicio de sesión
        binding.tvAlreadyHaveAccount.setOnClickListener(v -> navigateToLogin());
    }

    private void navigateToRegister() {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigate(R.id.action_onboardingFragment_to_registerFragment);
    }

    private void navigateToLogin() {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigate(R.id.action_onboardingFragment_to_loginFragment);
    }

    private void navigateToMain() {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigate(R.id.action_onboardingFragment_to_mainFragment);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
