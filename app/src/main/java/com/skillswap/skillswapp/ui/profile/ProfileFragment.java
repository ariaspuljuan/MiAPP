package com.skillswap.skillswapp.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseUser;
import com.skillswap.skillswapp.R;
import com.skillswap.skillswapp.data.model.User;
import com.skillswap.skillswapp.databinding.FragmentProfileBinding;
import com.skillswap.skillswapp.ui.adapters.SkillAdapter;
import com.skillswap.skillswapp.util.UiUtils;
import com.skillswap.skillswapp.viewmodel.AuthViewModel;
import com.skillswap.skillswapp.viewmodel.UserViewModel;

/**
 * Fragmento para mostrar el perfil del usuario.
 */
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private AuthViewModel authViewModel;
    private UserViewModel userViewModel;
    private SkillAdapter teachSkillsAdapter;
    private SkillAdapter learnSkillsAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Inicializar ViewModels
        authViewModel = new AuthViewModel();
        userViewModel = new UserViewModel();
        
        // Verificar si el usuario está autenticado
        FirebaseUser currentUser = authViewModel.getCurrentUser();
        if (currentUser == null) {
            navigateToLogin();
            return;
        }
        
        setupRecyclerViews();
        setupListeners();
        loadUserData(currentUser.getUid());
    }

    private void setupRecyclerViews() {
        // Configurar RecyclerView para habilidades que enseña
        teachSkillsAdapter = new SkillAdapter(true);
        binding.rvTeachSkills.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTeachSkills.setAdapter(teachSkillsAdapter);
        
        // Configurar RecyclerView para habilidades que quiere aprender
        learnSkillsAdapter = new SkillAdapter(false);
        binding.rvLearnSkills.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvLearnSkills.setAdapter(learnSkillsAdapter);
    }

    private void setupListeners() {
        // Botón para editar perfil
        binding.fabEditProfile.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.action_navigation_profile_to_profileEditFragment);
        });
        
        // Botón para cerrar sesión
        binding.btnLogout.setOnClickListener(v -> {
            showLogoutConfirmationDialog();
        });
    }
    
    /**
     * Muestra un diálogo de confirmación para cerrar sesión.
     */
    private void showLogoutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.logout);
        builder.setMessage(R.string.logout_confirmation);
        builder.setPositiveButton(R.string.logout_yes, (dialog, which) -> {
            logout();
        });
        builder.setNegativeButton(R.string.logout_no, (dialog, which) -> {
            dialog.dismiss();
        });
        builder.create().show();
    }
    
    /**
     * Cierra la sesión del usuario y navega a la pantalla de inicio de sesión.
     */
    private void logout() {
        authViewModel.logout();
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigate(R.id.action_mainFragment_to_loginFragment);
    }

    private void loadUserData(String userId) {
        userViewModel.getUserById(userId).observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                updateUI(user);
            } else {
                UiUtils.showSnackbar(binding.getRoot(), getString(R.string.error_profile_update));
            }
        });
        
        // Observar estado de carga
        userViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // Implementar lógica para mostrar/ocultar indicador de carga
        });
    }

    private void updateUI(User user) {
        // Actualizar nombre de usuario
        binding.tvUserName.setText(user.getProfile().getName());
        
        // Actualizar biografía
        String bio = user.getProfile().getBio();
        if (bio != null && !bio.isEmpty()) {
            binding.tvBio.setText(bio);
        } else {
            binding.tvBio.setText(R.string.no_bio);
        }
        
        // Actualizar foto de perfil si existe
        String photoUrl = user.getProfile().getPhotoUrl();
        if (photoUrl != null && !photoUrl.isEmpty()) {
            // Cargar imagen con Glide o Picasso
            // Glide.with(this).load(photoUrl).into(binding.ivProfileImage);
        }
        
        // Actualizar habilidades que enseña
        if (user.getSkillsToTeach() != null && !user.getSkillsToTeach().isEmpty()) {
            teachSkillsAdapter.setSkills(user.getSkillsToTeach());
        }
        
        // Actualizar habilidades que quiere aprender
        if (user.getSkillsToLearn() != null && !user.getSkillsToLearn().isEmpty()) {
            learnSkillsAdapter.setSkills(user.getSkillsToLearn());
        }
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
