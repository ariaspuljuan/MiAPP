package com.skillswap.skillswapp.ui.profile;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.skillswap.skillswapp.R;
import com.skillswap.skillswapp.data.model.User;
import com.skillswap.skillswapp.databinding.FragmentProfileEditBinding;
import com.skillswap.skillswapp.util.UiUtils;
import com.skillswap.skillswapp.viewmodel.UserViewModel;

/**
 * Fragmento para editar el perfil del usuario.
 */
public class ProfileEditFragment extends Fragment {

    private FragmentProfileEditBinding binding;
    private UserViewModel userViewModel;
    private Uri selectedImageUri;
    private ActivityResultLauncher<String> getContent;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Registrar el launcher para seleccionar imágenes
        getContent = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                selectedImageUri = uri;
                Glide.with(this)
                        .load(uri)
                        .circleCrop()
                        .into(binding.ivProfileImage);
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileEditBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Inicializar ViewModel
        userViewModel = new UserViewModel();
        
        // Verificar si el usuario está autenticado
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            navigateToLogin();
            return;
        }
        
        setupListeners();
        loadUserData(currentUser.getUid());
    }

    private void setupListeners() {
        // Botón para seleccionar imagen de perfil
        binding.btnChangeImage.setOnClickListener(v -> {
            getContent.launch("image/*");
        });
        
        // Botón para guardar cambios
        binding.btnSave.setOnClickListener(v -> {
            saveUserProfile();
        });
        
        // Botón para cancelar
        binding.btnCancel.setOnClickListener(v -> {
            navigateBack();
        });
    }

    private void loadUserData(String userId) {
        userViewModel.getUserById(userId).observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                updateUI(user);
            } else {
                UiUtils.showSnackbar(binding.getRoot(), getString(R.string.error_loading_profile));
            }
        });
        
        // Observar estado de carga
        userViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.scrollView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        });
    }

    private void updateUI(User user) {
        // Establecer nombre
        binding.etName.setText(user.getProfile().getName());
        
        // Establecer biografía si existe
        String bio = user.getProfile().getBio();
        if (bio != null && !bio.isEmpty()) {
            binding.etBio.setText(bio);
        }
        
        // Cargar foto de perfil si existe
        String photoUrl = user.getProfile().getPhotoUrl();
        if (photoUrl != null && !photoUrl.isEmpty()) {
            Glide.with(this)
                    .load(photoUrl)
                    .circleCrop()
                    .into(binding.ivProfileImage);
        }
    }

    private void saveUserProfile() {
        String name = binding.etName.getText().toString().trim();
        String bio = binding.etBio.getText().toString().trim();
        
        // Validar campos
        if (name.isEmpty()) {
            binding.tilName.setError(getString(R.string.error_empty_name));
            return;
        }
        
        // Mostrar progreso
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.scrollView.setVisibility(View.GONE);
        
        // Obtener el ID del usuario actual
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        // Actualizar nombre
        userViewModel.updateUserField(userId, "profile/name", name).observe(getViewLifecycleOwner(), success -> {
            if (success) {
                // Actualizar biografía
                userViewModel.updateUserField(userId, "profile/bio", bio).observe(getViewLifecycleOwner(), bioSuccess -> {
                    // Si hay una imagen seleccionada, subirla
                    if (selectedImageUri != null) {
                        uploadProfileImage(userId);
                    } else {
                        // Ocultar progreso y navegar de vuelta
                        binding.progressBar.setVisibility(View.GONE);
                        UiUtils.showSnackbar(binding.getRoot(), getString(R.string.profile_updated));
                        navigateBack();
                    }
                });
            } else {
                // Ocultar progreso y mostrar error
                binding.progressBar.setVisibility(View.GONE);
                binding.scrollView.setVisibility(View.VISIBLE);
                UiUtils.showSnackbar(binding.getRoot(), getString(R.string.error_updating_profile));
            }
        });
    }

    private void uploadProfileImage(String userId) {
        userViewModel.uploadProfileImage(userId, selectedImageUri).observe(getViewLifecycleOwner(), downloadUrl -> {
            // Ocultar progreso
            binding.progressBar.setVisibility(View.GONE);
            
            if (downloadUrl != null && !downloadUrl.isEmpty()) {
                // Actualizar la URL de la foto en el perfil del usuario
                userViewModel.updateUserField(userId, "profile/photoUrl", downloadUrl).observe(getViewLifecycleOwner(), success -> {
                    if (success) {
                        UiUtils.showSnackbar(binding.getRoot(), getString(R.string.profile_updated));
                        navigateBack();
                    } else {
                        binding.scrollView.setVisibility(View.VISIBLE);
                        UiUtils.showSnackbar(binding.getRoot(), getString(R.string.error_updating_profile));
                    }
                });
            } else {
                binding.scrollView.setVisibility(View.VISIBLE);
                UiUtils.showSnackbar(binding.getRoot(), getString(R.string.error_uploading_image));
            }
        });
    }

    private void navigateToLogin() {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigate(R.id.action_mainFragment_to_loginFragment);
    }

    private void navigateBack() {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigate(R.id.action_profileEditFragment_to_navigation_profile);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
