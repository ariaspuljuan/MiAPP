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
import com.skillswap.skillswapp.databinding.FragmentRecentContactsBinding;
import com.skillswap.skillswapp.ui.adapters.UserAdapter;
import com.skillswap.skillswapp.viewmodel.ContactViewModel;
import com.skillswap.skillswapp.viewmodel.FavoriteViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragmento para mostrar los contactos recientes.
 */
public class RecentContactsFragment extends Fragment implements UserAdapter.OnUserClickListener {

    private FragmentRecentContactsBinding binding;
    private ContactViewModel contactViewModel;
    private FavoriteViewModel favoriteViewModel;
    private UserAdapter userAdapter;
    private List<User> recentContacts = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRecentContactsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Inicializar ViewModels
        contactViewModel = new ContactViewModel();
        favoriteViewModel = new FavoriteViewModel();
        
        setupRecyclerView();
        loadRecentContacts();
    }

    private void setupRecyclerView() {
        userAdapter = new UserAdapter(recentContacts);
        userAdapter.setOnUserClickListener(this);
        
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(userAdapter);
    }

    private void loadRecentContacts() {
        showLoading(true);
        
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        contactViewModel.getRecentContacts(currentUserId).observe(getViewLifecycleOwner(), users -> {
            showLoading(false);
            
            if (users != null && !users.isEmpty()) {
                recentContacts.clear();
                recentContacts.addAll(users);
                
                // Verificar cuáles son favoritos
                checkFavorites();
                
                userAdapter.notifyDataSetChanged();
                showEmptyState(false);
            } else {
                showEmptyState(true);
            }
        });
    }

    private void checkFavorites() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        favoriteViewModel.getFavoriteUserIds(currentUserId).observe(getViewLifecycleOwner(), favoriteIds -> {
            if (favoriteIds != null && !favoriteIds.isEmpty()) {
                // Marcar usuarios favoritos
                for (User user : recentContacts) {
                    user.setFavorite(favoriteIds.contains(user.getUserId()));
                }
                userAdapter.notifyDataSetChanged();
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
        if (isFavorite) {
            favoriteViewModel.addFavorite(user.getUserId());
        } else {
            favoriteViewModel.removeFavorite(user.getUserId());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
