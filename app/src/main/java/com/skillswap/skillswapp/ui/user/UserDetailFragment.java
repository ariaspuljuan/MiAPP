package com.skillswap.skillswapp.ui.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.skillswap.skillswapp.R;
import com.skillswap.skillswapp.data.model.User;
import com.skillswap.skillswapp.databinding.FragmentUserDetailBinding;
import com.skillswap.skillswapp.ui.adapters.SkillAdapter;
import com.skillswap.skillswapp.util.UiUtils;
import com.skillswap.skillswapp.viewmodel.FavoriteViewModel;
import com.skillswap.skillswapp.viewmodel.UserViewModel;

/**
 * Fragmento para mostrar el detalle de un usuario.
 */
public class UserDetailFragment extends Fragment {

    private FragmentUserDetailBinding binding;
    private UserViewModel userViewModel;
    private FavoriteViewModel favoriteViewModel;
    private SkillAdapter teachSkillsAdapter;
    private String userId;
    private boolean isFavorite = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentUserDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Obtener el ID del usuario de los argumentos
        if (getArguments() != null) {
            userId = getArguments().getString("userId");
        }
        
        if (userId == null || userId.isEmpty()) {
            UiUtils.showSnackbar(binding.getRoot(), getString(R.string.error_user_not_found));
            requireActivity().onBackPressed();
            return;
        }
        
        // Inicializar ViewModels
        userViewModel = new UserViewModel();
        favoriteViewModel = new FavoriteViewModel();
        
        setupRecyclerView();
        setupListeners();
        loadUserData();
        checkIfFavorite();
    }

    private void setupRecyclerView() {
        // Configurar RecyclerView para habilidades que enseña
        teachSkillsAdapter = new SkillAdapter(true);
        binding.rvTeachSkills.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTeachSkills.setAdapter(teachSkillsAdapter);
    }

    private void setupListeners() {
        // Botón para agregar/quitar de favoritos
        binding.fabFavorite.setOnClickListener(v -> {
            if (isFavorite) {
                removeFavorite();
            } else {
                addFavorite();
            }
        });
        
        // Botón para contactar
        binding.btnContact.setOnClickListener(v -> {
            // Implementar lógica para contactar al usuario
            // Por ahora, solo agregamos como contacto reciente
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            if (!currentUserId.equals(userId)) {
                userViewModel.addRecentContact(userId);
                UiUtils.showSnackbar(binding.getRoot(), getString(R.string.contact_added));
            }
        });
    }

    private void loadUserData() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.scrollView.setVisibility(View.GONE);
        
        userViewModel.getUserById(userId).observe(getViewLifecycleOwner(), user -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.scrollView.setVisibility(View.VISIBLE);
            
            if (user != null) {
                updateUI(user);
            } else {
                UiUtils.showSnackbar(binding.getRoot(), getString(R.string.error_user_not_found));
                requireActivity().onBackPressed();
            }
        });
    }

    private void checkIfFavorite() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        // No mostrar botón de favorito si es el propio perfil
        if (currentUserId.equals(userId)) {
            binding.fabFavorite.setVisibility(View.GONE);
            return;
        }
        
        favoriteViewModel.isFavorite(currentUserId, userId).observe(getViewLifecycleOwner(), favorite -> {
            isFavorite = favorite != null && favorite;
            updateFavoriteButton();
        });
    }

    private void updateUI(User user) {
        // Actualizar nombre de usuario
        binding.tvUserName.setText(user.getProfile().getName());
        
        // Actualizar biografía
        String bio = user.getProfile().getBio();
        if (bio != null && !bio.isEmpty()) {
            binding.tvBio.setText(bio);
            binding.tvBio.setVisibility(View.VISIBLE);
        } else {
            binding.tvBio.setVisibility(View.GONE);
        }
        
        // Actualizar foto de perfil si existe
        String photoUrl = user.getProfile().getPhotoUrl();
        if (photoUrl != null && !photoUrl.isEmpty()) {
            Glide.with(this)
                    .load(photoUrl)
                    .circleCrop()
                    .into(binding.ivProfileImage);
        }
        
        // Actualizar habilidades que enseña
        if (user.getSkillsToTeach() != null && !user.getSkillsToTeach().isEmpty()) {
            teachSkillsAdapter.setSkills(user.getSkillsToTeach());
            binding.tvNoSkills.setVisibility(View.GONE);
        } else {
            binding.tvNoSkills.setVisibility(View.VISIBLE);
        }
    }

    private void updateFavoriteButton() {
        binding.fabFavorite.setImageResource(isFavorite ? 
                R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);
    }

    private void addFavorite() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        favoriteViewModel.addFavorite(userId).observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                isFavorite = true;
                updateFavoriteButton();
                UiUtils.showSnackbar(binding.getRoot(), getString(R.string.added_to_favorites));
            } else {
                UiUtils.showSnackbar(binding.getRoot(), getString(R.string.error_adding_favorite));
            }
        });
    }

    private void removeFavorite() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        favoriteViewModel.removeFavorite(userId).observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                isFavorite = false;
                updateFavoriteButton();
                UiUtils.showSnackbar(binding.getRoot(), getString(R.string.removed_from_favorites));
            } else {
                UiUtils.showSnackbar(binding.getRoot(), getString(R.string.error_removing_favorite));
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
