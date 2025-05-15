package com.skillswap.skillswapp.ui.explore;

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
import androidx.recyclerview.widget.RecyclerView;

import com.skillswap.skillswapp.R;
import com.skillswap.skillswapp.data.model.User;
import com.skillswap.skillswapp.databinding.FragmentExploreUsersBinding;
import com.skillswap.skillswapp.ui.adapters.UserAdapter;
import com.skillswap.skillswapp.viewmodel.UserViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragmento para la exploración de usuarios.
 */
public class ExploreUsersFragment extends Fragment implements UserAdapter.OnUserClickListener {

    private FragmentExploreUsersBinding binding;
    private UserViewModel userViewModel;
    private UserAdapter userAdapter;
    private List<User> userList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentExploreUsersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Inicializar ViewModel
        userViewModel = new UserViewModel();
        
        setupRecyclerView();
        loadUsers();
    }

    private void setupRecyclerView() {
        userAdapter = new UserAdapter(userList);
        userAdapter.setOnUserClickListener(this);
        
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(userAdapter);
        
        // Agregar listener para cargar más usuarios al hacer scroll
        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                
                // Cargar más usuarios cuando se acerque al final de la lista
                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5
                        && firstVisibleItemPosition >= 0) {
                    loadMoreUsers();
                }
            }
        });
    }

    private void loadUsers() {
        showLoading(true);
        
        userViewModel.getAllUsers().observe(getViewLifecycleOwner(), users -> {
            showLoading(false);
            
            if (users != null && !users.isEmpty()) {
                userList.clear();
                userList.addAll(users);
                userAdapter.notifyDataSetChanged();
                showEmptyState(false);
            } else {
                showEmptyState(true);
            }
        });
    }

    private void loadMoreUsers() {
        // Implementar paginación si es necesario
    }

    /**
     * Realiza una búsqueda de usuarios.
     * @param query Texto de búsqueda
     * @param categoryId ID de categoría para filtrar
     */
    public void search(String query, String categoryId) {
        showLoading(true);
        
        userViewModel.searchUsers(query, categoryId).observe(getViewLifecycleOwner(), users -> {
            showLoading(false);
            
            if (users != null && !users.isEmpty()) {
                userList.clear();
                userList.addAll(users);
                userAdapter.notifyDataSetChanged();
                showEmptyState(false);
            } else {
                userList.clear();
                userAdapter.notifyDataSetChanged();
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
        
        navController.navigate(R.id.action_navigation_explore_to_userDetailFragment, args);
    }

    @Override
    public void onFavoriteClick(User user, boolean isFavorite) {
        // Manejar click en favorito
        if (isFavorite) {
            userViewModel.addFavorite(user.getUserId());
        } else {
            userViewModel.removeFavorite(user.getUserId());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
