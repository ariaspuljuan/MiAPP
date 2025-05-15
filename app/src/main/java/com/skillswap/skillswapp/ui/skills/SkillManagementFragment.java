package com.skillswap.skillswapp.ui.skills;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.skillswap.skillswapp.R;
import com.skillswap.skillswapp.databinding.FragmentSkillManagementBinding;

/**
 * Fragmento para gestionar las habilidades del usuario.
 */
public class SkillManagementFragment extends Fragment {

    private FragmentSkillManagementBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSkillManagementBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Verificar si el usuario está autenticado
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            navigateToLogin();
            return;
        }
        
        setupViewPager();
        setupListeners();
    }

    private void setupViewPager() {
        // Configurar adaptador para ViewPager
        SkillsViewPagerAdapter adapter = new SkillsViewPagerAdapter(this);
        binding.viewPager.setAdapter(adapter);
        
        // Conectar TabLayout con ViewPager
        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(R.string.skills_to_teach);
                    break;
                case 1:
                    tab.setText(R.string.skills_to_learn);
                    break;
            }
        }).attach();
    }

    private void setupListeners() {
        // Botón para volver
        binding.toolbar.setNavigationOnClickListener(v -> {
            requireActivity().onBackPressed();
        });
        
        // Botón para agregar habilidad
        binding.fabAddSkill.setOnClickListener(v -> {
            // Determinar qué tipo de habilidad agregar según la pestaña actual
            boolean isTeachSkill = binding.viewPager.getCurrentItem() == 0;
            
            // Mostrar diálogo para agregar habilidad
            AddSkillDialogFragment dialog = new AddSkillDialogFragment();
            Bundle args = new Bundle();
            args.putBoolean("isTeachSkill", isTeachSkill);
            dialog.setArguments(args);
            dialog.show(getChildFragmentManager(), "AddSkillDialog");
        });
    }

    private void navigateToLogin() {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigate(R.id.action_mainFragment_to_loginFragment);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Adaptador para el ViewPager de habilidades.
     */
    private static class SkillsViewPagerAdapter extends FragmentStateAdapter {
        
        public SkillsViewPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new TeachSkillsFragment();
                case 1:
                    return new LearnSkillsFragment();
                default:
                    return new TeachSkillsFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}
