package com.skillswap.skillswapp.ui.contacts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
        
        // Inicializar ViewModel
        favoriteViewModel = new FavoriteViewModel();
        
        setupRecyclerView();
        loadFavorites();
    }

    private void setupRecyclerView() {
        userAdapter = new UserAdapter(favoriteUsers);
        userAdapter.setOnUserClickListener(this);
        
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(userAdapter);
    }

    private void loadFavorites() {
        showLoading(true);
        
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
        // Manejar click en favorito
        if (!isFavorite) {
            // Eliminar de favoritos
            favoriteViewModel.removeFavorite(user.getUserId());
            
            // Eliminar de la lista local
            int position = -1;
            for (int i = 0; i < favoriteUsers.size(); i++) {
                if (favoriteUsers.get(i).getUserId().equals(user.getUserId())) {
                    position = i;
                    break;
                }
            }
            
            if (position != -1) {
                favoriteUsers.remove(position);
                userAdapter.notifyItemRemoved(position);
                
                // Mostrar estado vacío si no hay más favoritos
                if (favoriteUsers.isEmpty()) {
                    showEmptyState(true);
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
