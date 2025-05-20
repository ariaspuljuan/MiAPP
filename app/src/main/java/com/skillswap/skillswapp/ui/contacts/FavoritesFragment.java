package com.skillswap.skillswapp.ui.contacts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.skillswap.skillswapp.R;
import com.skillswap.skillswapp.data.model.User;
import com.skillswap.skillswapp.databinding.FragmentFavoritesBinding;
import com.skillswap.skillswapp.ui.adapters.UserAdapter;
import com.skillswap.skillswapp.viewmodel.FavoriteViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragmento para mostrar los usuarios favoritos.
 */
public class FavoritesFragment extends Fragment implements UserAdapter.OnUserClickListener {

    private FragmentFavoritesBinding binding;
    private FavoriteViewModel favoriteViewModel;
    private UserAdapter userAdapter;
    private List<User> favoriteUsers = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        try {
            // Inicializar ViewModel
            favoriteViewModel = new FavoriteViewModel();
            // Inicializar el contexto para el almacenamiento local
            favoriteViewModel.initContext(requireContext());
            
            setupRecyclerView();
            loadFavorites();
        } catch (Exception e) {
            // Manejar cualquier excepción durante la inicialización
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error al cargar favoritos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            e.printStackTrace();
            showEmptyState(true);
        }
    }

    private void setupRecyclerView() {
        userAdapter = new UserAdapter(favoriteUsers);
        userAdapter.setOnUserClickListener(this);
        
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(userAdapter);
    }

    private void loadFavorites() {
        showLoading(true);
        
        try {
            // Verificar si el usuario está autenticado
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                showEmptyState(true);
                showLoading(false);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Debes iniciar sesión para ver tus favoritos", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            
            // Usar el almacenamiento local para cargar favoritos
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            favoriteViewModel.getFavoriteUsers(currentUserId).observe(getViewLifecycleOwner(), users -> {
                showLoading(false);
                
                if (users != null && !users.isEmpty()) {
                    favoriteUsers.clear();
                    favoriteUsers.addAll(users);
                    userAdapter.notifyDataSetChanged();
                    showEmptyState(false);
                } else {
                    showEmptyState(true);
                }
            });
            
            // Observar mensajes de error
            favoriteViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMsg -> {
                if (errorMsg != null && !errorMsg.isEmpty() && getContext() != null) {
                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            showLoading(false);
            showEmptyState(true);
            e.printStackTrace();
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error al cargar favoritos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showEmptyState(boolean show) {
        binding.tvEmptyState.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onUserClick(User user) {
        // Navegar al detalle del usuario
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        
        Bundle args = new Bundle();
        args.putString("userId", user.getUserId());
        
        navController.navigate(R.id.action_navigation_contacts_to_userDetailFragment, args);
    }

    @Override
    public void onFavoriteClick(User user, boolean isFavorite) {
        try {
            if (isFavorite) {
                // Ya es favorito, eliminarlo
                favoriteViewModel.removeFavorite(user.getUserId()).observe(getViewLifecycleOwner(), success -> {
                    if (success) {
                        user.setFavorite(false);
                        Toast.makeText(getContext(), "Usuario eliminado de favoritos", Toast.LENGTH_SHORT).show();
                        
                        // Eliminar de la lista
                        favoriteUsers.remove(user);
                        userAdapter.notifyDataSetChanged();
                        
                        if (favoriteUsers.isEmpty()) {
                            showEmptyState(true);
                        }
                    } else {
                        Toast.makeText(getContext(), "No se pudo eliminar de favoritos", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
