package com.skillswap.skillswapp.ui.explore;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.material.chip.Chip;
import com.google.android.material.tabs.TabLayoutMediator;
import com.skillswap.skillswapp.R;
import com.skillswap.skillswapp.data.model.Category;
import com.skillswap.skillswapp.databinding.FragmentExploreBinding;
import com.skillswap.skillswapp.viewmodel.CategoryViewModel;
import com.skillswap.skillswapp.viewmodel.SkillViewModel;
import com.skillswap.skillswapp.viewmodel.UserViewModel;

import java.util.List;

/**
 * Fragmento para la exploración de usuarios y habilidades.
 */
public class ExploreFragment extends Fragment {

    private FragmentExploreBinding binding;
    private CategoryViewModel categoryViewModel;
    private UserViewModel userViewModel;
    private SkillViewModel skillViewModel;
    private String currentQuery = "";
    private String currentCategory = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentExploreBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Inicializar ViewModels
        categoryViewModel = new CategoryViewModel();
        userViewModel = new UserViewModel();
        skillViewModel = new SkillViewModel();
        
        setupViewPager();
        setupSearchView();
        loadCategories();
    }

    private void setupViewPager() {
        // Configurar adaptador para ViewPager
        ExploreViewPagerAdapter adapter = new ExploreViewPagerAdapter(this);
        binding.viewPager.setAdapter(adapter);
        
        // Conectar TabLayout con ViewPager
        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Personas");
                    break;
                case 1:
                    tab.setText("Habilidades");
                    break;
            }
        }).attach();
    }

    private void setupSearchView() {
        // Configurar listener para búsqueda
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                currentQuery = s.toString().trim();
                performSearch();
            }
        });
        
        // Configurar listener para el chip "Más..."
        binding.chipMore.setOnClickListener(v -> {
            // Abrir diálogo para mostrar todas las categorías
            CategoryDialogFragment dialog = new CategoryDialogFragment();
            dialog.setOnCategorySelectedListener(category -> {
                currentCategory = category.getCategoryId();
                updateCategoryChips(category);
                performSearch();
            });
            dialog.show(getChildFragmentManager(), "CategoryDialog");
        });
        
        // Configurar listener para chip "Todos"
        binding.chipAll.setOnClickListener(v -> {
            currentCategory = "";
            binding.chipAll.setChecked(true);
            performSearch();
        });
    }

    private void loadCategories() {
        categoryViewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null && !categories.isEmpty()) {
                setupCategoryChips(categories);
            }
        });
    }

    private void setupCategoryChips(List<Category> categories) {
        // Mostrar solo las primeras 3 categorías en chips
        int maxChips = Math.min(categories.size(), 3);
        for (int i = 0; i < maxChips; i++) {
            Category category = categories.get(i);
            
            // Configurar chips existentes
            Chip chip;
            switch (i) {
                case 0:
                    chip = binding.chipTechnology;
                    break;
                case 1:
                    chip = binding.chipLanguages;
                    break;
                case 2:
                    chip = binding.chipMusic;
                    break;
                default:
                    continue;
            }
            
            chip.setText(category.getName());
            chip.setTag(category.getCategoryId());
            chip.setOnClickListener(v -> {
                currentCategory = category.getCategoryId();
                performSearch();
            });
        }
    }

    private void updateCategoryChips(Category selectedCategory) {
        // Desmarcar todos los chips
        binding.chipAll.setChecked(false);
        binding.chipTechnology.setChecked(false);
        binding.chipLanguages.setChecked(false);
        binding.chipMusic.setChecked(false);
        
        // Verificar si el chip ya existe
        for (int i = 0; i < binding.chipGroupCategories.getChildCount(); i++) {
            View child = binding.chipGroupCategories.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                if (chip.getTag() != null && chip.getTag().equals(selectedCategory.getCategoryId())) {
                    chip.setChecked(true);
                    return;
                }
            }
        }
        
        // Si no existe, crear un nuevo chip temporal
        Chip newChip = new Chip(requireContext());
        newChip.setText(selectedCategory.getName());
        newChip.setTag(selectedCategory.getCategoryId());
        newChip.setCheckable(true);
        newChip.setChecked(true);
        newChip.setClickable(true);
        newChip.setOnClickListener(v -> {
            currentCategory = selectedCategory.getCategoryId();
            performSearch();
        });
        
        // Insertar antes del chip "Más..."
        binding.chipGroupCategories.addView(newChip, binding.chipGroupCategories.getChildCount() - 1);
    }

    private void performSearch() {
        // Notificar a los fragmentos hijos sobre la búsqueda
        int currentTab = binding.viewPager.getCurrentItem();
        Fragment fragment = getChildFragmentManager().findFragmentByTag("f" + currentTab);
        
        if (fragment instanceof ExploreUsersFragment) {
            ((ExploreUsersFragment) fragment).search(currentQuery, currentCategory);
        } else if (fragment instanceof ExploreSkillsFragment) {
            ((ExploreSkillsFragment) fragment).search(currentQuery, currentCategory);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Adaptador para el ViewPager de exploración.
     */
    private static class ExploreViewPagerAdapter extends FragmentStateAdapter {
        
        public ExploreViewPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new ExploreUsersFragment();
                case 1:
                    return new ExploreSkillsFragment();
                default:
                    return new ExploreUsersFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}
