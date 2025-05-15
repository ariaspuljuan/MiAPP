package com.skillswap.skillswapp.ui.main;

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
import com.skillswap.skillswapp.databinding.FragmentMainBinding;
import com.skillswap.skillswapp.util.UiUtils;
import com.skillswap.skillswapp.viewmodel.AuthViewModel;
import com.skillswap.skillswapp.viewmodel.UserViewModel;

/**
 * Fragmento principal para la pantalla principal después de la autenticación.
 */
public class MainFragment extends Fragment {

    private FragmentMainBinding binding;
    private AuthViewModel authViewModel;
    private UserViewModel userViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMainBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Inicializar ViewModels
        authViewModel = new AuthViewModel();
        userViewModel = new UserViewModel();
        
        // Verificar si el usuario está autenticado
        if (authViewModel.getCurrentUser() == null) {
            navigateToLogin();
            return;
        }
        
        setupUI();
        setupListeners();
    }

    private void setupUI() {
        // Cargar datos del usuario
        String userId = authViewModel.getCurrentUser().getUid();
        userViewModel.getUserById(userId).observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                binding.tvWelcome.setText("¡Bienvenido, " + user.getProfile().getName() + "!");
            }
        });
    }

    private void setupListeners() {
        // Botón para cerrar sesión
        binding.btnLogout.setOnClickListener(v -> logout());
    }

    private void logout() {
        authViewModel.logout();
        UiUtils.showSnackbar(binding.getRoot(), "Sesión cerrada correctamente");
        navigateToLogin();
    }

    private void navigateToLogin() {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigate(R.id.action_mainFragment_to_loginFragment);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
