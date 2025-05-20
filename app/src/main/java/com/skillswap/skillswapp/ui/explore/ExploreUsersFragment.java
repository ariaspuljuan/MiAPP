package com.skillswap.skillswapp.ui.explore;

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
        
        try {
            // Inicializar ViewModel
            userViewModel = new UserViewModel();
            // Inicializar el contexto para el almacenamiento local
            userViewModel.initContext(requireContext());
            
            setupRecyclerView();
            loadUsers();
        } catch (Exception e) {
            // Manejar cualquier excepción durante la inicialización
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error al inicializar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            e.printStackTrace();
        }
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
     * @param level Nivel de habilidad (0: cualquiera, 1: principiante, 2: intermedio, 3: avanzado)
     */
    public void search(String query, String categoryId, int level) {
        showLoading(true);
        
        // Mostrar animación de carga
        binding.recyclerView.setVisibility(View.INVISIBLE);
        binding.progressBar.setVisibility(View.VISIBLE);
        
        userViewModel.searchUsersAdvanced(query, categoryId, level).observe(getViewLifecycleOwner(), users -> {
            showLoading(false);
            
            if (users != null && !users.isEmpty()) {
                userList.clear();
                userList.addAll(users);
                userAdapter.notifyDataSetChanged();
                showEmptyState(false);
                
                // Mostrar animación de aparición de resultados
                binding.recyclerView.setVisibility(View.VISIBLE);
                binding.recyclerView.scheduleLayoutAnimation();
            } else {
                userList.clear();
                userAdapter.notifyDataSetChanged();
                showEmptyState(true);
            }
        });
    }
    
    /**
     * Sobrecarga del método search para mantener compatibilidad con código existente
     */
    public void search(String query, String categoryId) {
        search(query, categoryId, 0); // 0 = cualquier nivel
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showEmptyState(boolean show) {
        binding.tvEmptyState.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onUserClick(User user) {
        try {
            // Añadir a contactos recientes
            userViewModel.addRecentContact(user.getUserId()).observe(getViewLifecycleOwner(), success -> {
                if (!success) {
                    Toast.makeText(getContext(), "No se pudo añadir a contactos recientes", Toast.LENGTH_SHORT).show();
                }
            });
            
            // Navegar al detalle del usuario
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            
            Bundle args = new Bundle();
            args.putString("userId", user.getUserId());
            
            navController.navigate(R.id.action_navigation_explore_to_userDetailFragment, args);
        } catch (Exception e) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error al abrir el perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            e.printStackTrace();
        }
    }

    @Override
    public void onFavoriteClick(User user, boolean isFavorite) {
        try {
            if (isFavorite) {
                // Añadir a favoritos
                userViewModel.addFavorite(user.getUserId()).observe(getViewLifecycleOwner(), success -> {
                    if (success) {
                        Toast.makeText(getContext(), "Usuario añadido a favoritos", Toast.LENGTH_SHORT).show();
                        user.setFavorite(true);
                        userAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "No se pudo añadir a favoritos", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // Eliminar de favoritos
                userViewModel.removeFavorite(user.getUserId()).observe(getViewLifecycleOwner(), success -> {
                    if (success) {
                        Toast.makeText(getContext(), "Usuario eliminado de favoritos", Toast.LENGTH_SHORT).show();
                        user.setFavorite(false);
                        userAdapter.notifyDataSetChanged();
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
