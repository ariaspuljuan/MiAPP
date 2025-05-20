package com.skillswap.skillswapp.ui.explore;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.skillswap.skillswapp.R;
import com.skillswap.skillswapp.data.model.Skill;
import com.skillswap.skillswapp.databinding.FragmentExploreSkillsBinding;
import com.skillswap.skillswapp.ui.adapters.SkillListAdapter;
import com.skillswap.skillswapp.viewmodel.SkillViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragmento para la exploración de habilidades.
 */
public class ExploreSkillsFragment extends Fragment implements SkillListAdapter.OnSkillClickListener {

    private FragmentExploreSkillsBinding binding;
    private SkillViewModel skillViewModel;
    private SkillListAdapter skillAdapter;
    private List<Skill> skillList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentExploreSkillsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        try {
            // Inicializar ViewModel
            skillViewModel = new SkillViewModel();
            
            setupRecyclerView();
            loadSkills();
        } catch (Exception e) {
            // Manejar cualquier excepción durante la inicialización
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error al inicializar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            e.printStackTrace();
        }
    }

    private void setupRecyclerView() {
        skillAdapter = new SkillListAdapter(skillList);
        skillAdapter.setOnSkillClickListener(this);
        
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(skillAdapter);
        
        // Agregar listener para cargar más habilidades al hacer scroll
        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                
                // Cargar más habilidades cuando se acerque al final de la lista
                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5
                        && firstVisibleItemPosition >= 0) {
                    loadMoreSkills();
                }
            }
        });
    }

    private void loadSkills() {
        showLoading(true);
        
        skillViewModel.getAllSkills().observe(getViewLifecycleOwner(), skills -> {
            showLoading(false);
            
            if (skills != null && !skills.isEmpty()) {
                skillList.clear();
                skillList.addAll(skills);
                skillAdapter.notifyDataSetChanged();
                showEmptyState(false);
            } else {
                showEmptyState(true);
            }
        });
    }

    private void loadMoreSkills() {
        // Implementar paginación si es necesario
    }

    /**
     * Realiza una búsqueda de habilidades.
     * @param query Texto de búsqueda
     * @param categoryId ID de categoría para filtrar
     */
    public void search(String query, String categoryId) {
        // Llamar al método de búsqueda avanzada con nivel 0 (cualquier nivel)
        search(query, categoryId, 0);
    }
    
    /**
     * Realiza una búsqueda avanzada de habilidades con filtrado por nivel.
     * @param query Texto de búsqueda
     * @param categoryId ID de categoría para filtrar
     * @param level Nivel de habilidad (0: cualquiera, 1: principiante, 2: intermedio, 3: avanzado)
     */
    public void search(String query, String categoryId, int level) {
        showLoading(true);
        
        // Mostrar animación de carga
        binding.recyclerView.setVisibility(View.INVISIBLE);
        binding.progressBar.setVisibility(View.VISIBLE);
        
        // Usar el método de búsqueda avanzada si se especifica un nivel, o el método estándar si no
        LiveData<List<Skill>> searchResult;
        if (level > 0) {
            searchResult = skillViewModel.searchSkillsAdvanced(query, categoryId, level);
        } else {
            searchResult = skillViewModel.searchSkills(query, categoryId);
        }
        
        searchResult.observe(getViewLifecycleOwner(), skills -> {
            showLoading(false);
            
            if (skills != null && !skills.isEmpty()) {
                skillList.clear();
                skillList.addAll(skills);
                skillAdapter.notifyDataSetChanged();
                showEmptyState(false);
                
                // Mostrar animación de aparición de resultados
                binding.recyclerView.setVisibility(View.VISIBLE);
                binding.recyclerView.scheduleLayoutAnimation();
            } else {
                skillList.clear();
                skillAdapter.notifyDataSetChanged();
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
    public void onSkillClick(Skill skill) {
        // Navegar al detalle de la habilidad
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        
        Bundle args = new Bundle();
        args.putString("skillId", skill.getSkillId());
        
        navController.navigate(R.id.action_navigation_explore_to_skillDetailFragment, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
