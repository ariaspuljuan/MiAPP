package com.skillswap.skillswapp.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseUser;
import com.skillswap.skillswapp.R;
import com.skillswap.skillswapp.data.model.User;
import com.skillswap.skillswapp.databinding.FragmentProfileSetupBinding;
import com.skillswap.skillswapp.util.UiUtils;
import com.skillswap.skillswapp.viewmodel.AuthViewModel;
import com.skillswap.skillswapp.viewmodel.UserViewModel;

import java.util.HashMap;
import java.util.Map;

/**
 * Fragmento para la configuración inicial del perfil de usuario.
 */
public class ProfileSetupFragment extends Fragment {

    private FragmentProfileSetupBinding binding;
    private AuthViewModel authViewModel;
    private UserViewModel userViewModel;
    // Mapas para almacenar las habilidades temporalmente con formato simple
    private Map<String, String> skillsToTeach = new HashMap<>();
    private Map<String, String> skillsToLearn = new HashMap<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileSetupBinding.inflate(inflater, container, false);
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
        
        setupListeners();
        observeViewModel();
    }

    private void setupListeners() {
        // Botón para cambiar foto de perfil
        binding.btnChangePicture.setOnClickListener(v -> {
            // Implementar selección de imagen en futuras versiones
            UiUtils.showSnackbar(binding.getRoot(), "Funcionalidad disponible próximamente");
        });
        
        // Botón para agregar habilidad para enseñar
        binding.btnAddSkillTeach.setOnClickListener(v -> addSkillToTeach());
        
        // Botón para agregar habilidad para aprender
        binding.btnAddSkillLearn.setOnClickListener(v -> addSkillToLearn());
        
        // Botón para guardar perfil
        binding.btnSaveProfile.setOnClickListener(v -> saveProfile());
    }

    private void observeViewModel() {
        // Observar estado de carga
        userViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.btnSaveProfile.setEnabled(!isLoading);
        });
        
        // Observar mensajes de error
        userViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                UiUtils.showSnackbar(binding.getRoot(), errorMessage);
            }
        });
    }

    private void addSkillToTeach() {
        String skill = binding.etSkillTeach.getText().toString().trim();
        if (skill.isEmpty()) {
            UiUtils.showSnackbar(binding.getRoot(), "Por favor ingresa una habilidad");
            return;
        }
        
        // Agregar habilidad al mapa
        skillsToTeach.put(skill, skill);
        
        // Crear y agregar chip
        Chip chip = new Chip(requireContext());
        chip.setText(skill);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> {
            binding.chipGroupTeach.removeView(chip);
            skillsToTeach.remove(skill);
        });
        binding.chipGroupTeach.addView(chip);
        
        // Limpiar campo
        binding.etSkillTeach.setText("");
    }

    private void addSkillToLearn() {
        String skill = binding.etSkillLearn.getText().toString().trim();
        if (skill.isEmpty()) {
            UiUtils.showSnackbar(binding.getRoot(), "Por favor ingresa una habilidad");
            return;
        }
        
        // Agregar habilidad al mapa
        skillsToLearn.put(skill, skill);
        
        // Crear y agregar chip
        Chip chip = new Chip(requireContext());
        chip.setText(skill);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> {
            binding.chipGroupLearn.removeView(chip);
            skillsToLearn.remove(skill);
        });
        binding.chipGroupLearn.addView(chip);
        
        // Limpiar campo
        binding.etSkillLearn.setText("");
    }

    private void saveProfile() {
        FirebaseUser currentUser = authViewModel.getCurrentUser();
        if (currentUser == null) {
            navigateToLogin();
            return;
        }
        
        // Crear objeto de usuario con datos del perfil
        User user = new User(currentUser.getUid(), currentUser.getDisplayName(), currentUser.getEmail());
        
        // Actualizar la bio en el perfil
        user.getProfile().setBio(binding.etBio.getText().toString().trim());
        
        // Convertir los maps de habilidades al formato correcto
        Map<String, User.SkillToTeach> skillsToTeachMap = new HashMap<>();
        for (Map.Entry<String, String> entry : skillsToTeach.entrySet()) {
            String skillId = entry.getKey();
            String skillTitle = entry.getValue();
            // Por defecto, nivel 3 y categoría "General"
            User.SkillToTeach skill = new User.SkillToTeach(skillTitle, 3, "General", "");
            skillsToTeachMap.put(skillId, skill);
        }
        user.setSkillsToTeach(skillsToTeachMap);
        
        // Convertir los maps de habilidades a aprender
        Map<String, User.SkillToLearn> skillsToLearnMap = new HashMap<>();
        for (Map.Entry<String, String> entry : skillsToLearn.entrySet()) {
            String skillId = entry.getKey();
            String skillTitle = entry.getValue();
            // Por defecto, prioridad 2
            User.SkillToLearn skill = new User.SkillToLearn(skillTitle, 2);
            skillsToLearnMap.put(skillId, skill);
        }
        user.setSkillsToLearn(skillsToLearnMap);
        
        // Guardar perfil en la base de datos
        userViewModel.updateUser(user).observe(getViewLifecycleOwner(), isSuccess -> {
            if (isSuccess) {
                UiUtils.showSnackbar(binding.getRoot(), getString(R.string.success_profile_update));
                navigateToMain();
            }
        });
    }

    private void navigateToLogin() {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigate(R.id.action_profileSetupFragment_to_loginFragment);
    }

    private void navigateToMain() {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigate(R.id.action_profileSetupFragment_to_mainFragment);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
