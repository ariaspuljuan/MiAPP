package com.skillswap.skillswapp.ui.skills;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.skillswap.skillswapp.R;
import com.skillswap.skillswapp.data.model.User;
import com.skillswap.skillswapp.databinding.FragmentLearnSkillsBinding;
import com.skillswap.skillswapp.ui.adapters.SkillAdapter;
import com.skillswap.skillswapp.util.UiUtils;
import com.skillswap.skillswapp.viewmodel.UserViewModel;

/**
 * Fragmento para gestionar las habilidades que el usuario quiere aprender.
 */
public class LearnSkillsFragment extends Fragment implements SkillAdapter.OnSkillClickListener {

    private FragmentLearnSkillsBinding binding;
    private UserViewModel userViewModel;
    private SkillAdapter skillAdapter;
    private String userId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLearnSkillsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Obtener el ID del usuario actual
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        // Inicializar ViewModel
        userViewModel = new UserViewModel();
        
        setupRecyclerView();
        loadUserSkills();
    }

    private void setupRecyclerView() {
        skillAdapter = new SkillAdapter(false);
        skillAdapter.setOnSkillClickListener(this);
        
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(skillAdapter);
    }

    private void loadUserSkills() {
        binding.progressBar.setVisibility(View.VISIBLE);
        
        userViewModel.getUserById(userId).observe(getViewLifecycleOwner(), user -> {
            binding.progressBar.setVisibility(View.GONE);
            
            if (user != null && user.getSkillsToLearn() != null && !user.getSkillsToLearn().isEmpty()) {
                skillAdapter.setSkills(user.getSkillsToLearn());
                binding.tvEmptyState.setVisibility(View.GONE);
            } else {
                binding.tvEmptyState.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onSkillClick(SkillAdapter.SkillItem skill) {
        // Mostrar diálogo para editar o eliminar la habilidad
        EditSkillDialogFragment dialog = new EditSkillDialogFragment();
        Bundle args = new Bundle();
        args.putString("skillId", skill.getId());
        args.putBoolean("isTeachSkill", false);
        dialog.setArguments(args);
        dialog.show(getChildFragmentManager(), "EditSkillDialog");
    }

    /**
     * Método para actualizar la lista de habilidades después de agregar o editar una.
     */
    public void refreshSkills() {
        loadUserSkills();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
