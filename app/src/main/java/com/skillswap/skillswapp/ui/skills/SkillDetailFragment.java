package com.skillswap.skillswapp.ui.skills;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.skillswap.skillswapp.R;
import com.skillswap.skillswapp.data.model.Skill;
import com.skillswap.skillswapp.data.model.User;
import com.skillswap.skillswapp.databinding.FragmentSkillDetailBinding;
import com.skillswap.skillswapp.ui.adapters.UserAdapter;
import com.skillswap.skillswapp.ui.user.UserDetailFragment;
import com.skillswap.skillswapp.util.UiUtils;
import com.skillswap.skillswapp.viewmodel.SkillViewModel;
import com.skillswap.skillswapp.viewmodel.UserViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragmento para mostrar el detalle de una habilidad.
 */
public class SkillDetailFragment extends Fragment implements UserAdapter.OnUserClickListener {

    private FragmentSkillDetailBinding binding;
    private SkillViewModel skillViewModel;
    private UserViewModel userViewModel;
    private UserAdapter userAdapter;
    private String skillId;
    private List<User> teachersList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSkillDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Obtener el ID de la habilidad de los argumentos
        if (getArguments() != null) {
            skillId = getArguments().getString("skillId");
        }
        
        if (skillId == null || skillId.isEmpty()) {
            UiUtils.showSnackbar(binding.getRoot(), getString(R.string.error_skill_not_found));
            requireActivity().onBackPressed();
            return;
        }
        
        // Inicializar ViewModels
        skillViewModel = new SkillViewModel();
        userViewModel = new UserViewModel();
        
        setupRecyclerView();
        setupListeners();
        loadSkillData();
    }

    private void setupRecyclerView() {
        userAdapter = new UserAdapter(teachersList);
        userAdapter.setOnUserClickListener(this);
        
        binding.rvTeachers.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTeachers.setAdapter(userAdapter);
    }

    private void setupListeners() {
        // Botón para volver
        binding.toolbar.setNavigationOnClickListener(v -> {
            requireActivity().onBackPressed();
        });
    }

    private void loadSkillData() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.scrollView.setVisibility(View.GONE);
        
        skillViewModel.getSkillById(skillId).observe(getViewLifecycleOwner(), skill -> {
            if (skill != null) {
                updateUI(skill);
                loadTeachers(skill);
            } else {
                binding.progressBar.setVisibility(View.GONE);
                UiUtils.showSnackbar(binding.getRoot(), getString(R.string.error_skill_not_found));
                requireActivity().onBackPressed();
            }
        });
    }

    private void updateUI(Skill skill) {
        // Actualizar título
        binding.tvSkillTitle.setText(skill.getTitle());
        binding.toolbar.setTitle(skill.getTitle());
        
        // Actualizar categoría
        if (skill.getCategory() != null && !skill.getCategory().isEmpty()) {
            binding.chipCategory.setText(skill.getCategory());
            binding.chipCategory.setVisibility(View.VISIBLE);
        } else {
            binding.chipCategory.setVisibility(View.GONE);
        }
        
        // Actualizar contador de profesores
        int teachersCount = skill.getUsersTeaching() != null ? skill.getUsersTeaching().size() : 0;
        binding.tvTeachersCount.setText(getString(R.string.teachers_count, teachersCount));
    }

    private void loadTeachers(Skill skill) {
        if (skill.getUsersTeaching() == null || skill.getUsersTeaching().isEmpty()) {
            binding.progressBar.setVisibility(View.GONE);
            binding.scrollView.setVisibility(View.VISIBLE);
            binding.tvNoTeachers.setVisibility(View.VISIBLE);
            binding.rvTeachers.setVisibility(View.GONE);
            return;
        }
        
        // Cargar datos de los usuarios que enseñan esta habilidad
        final int[] remaining = {skill.getUsersTeaching().size()};
        teachersList.clear();
        
        for (String userId : skill.getUsersTeaching()) {
            userViewModel.getUserById(userId).observe(getViewLifecycleOwner(), user -> {
                if (user != null) {
                    teachersList.add(user);
                }
                
                remaining[0]--;
                if (remaining[0] == 0) {
                    // Todos los usuarios han sido cargados
                    binding.progressBar.setVisibility(View.GONE);
                    binding.scrollView.setVisibility(View.VISIBLE);
                    
                    if (teachersList.isEmpty()) {
                        binding.tvNoTeachers.setVisibility(View.VISIBLE);
                        binding.rvTeachers.setVisibility(View.GONE);
                    } else {
                        binding.tvNoTeachers.setVisibility(View.GONE);
                        binding.rvTeachers.setVisibility(View.VISIBLE);
                        userAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    @Override
    public void onUserClick(User user) {
        // Navegar al detalle del usuario
        Bundle args = new Bundle();
        args.putString("userId", user.getUserId());
        
        // Usar el NavController para navegar
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment, UserDetailFragment.class, args)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onFavoriteClick(User user, boolean isFavorite) {
        // Implementar lógica para agregar/quitar de favoritos
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
