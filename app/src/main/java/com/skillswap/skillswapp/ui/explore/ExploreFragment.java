package com.skillswap.skillswapp.ui.explore;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.TextInputEditText;
import com.skillswap.skillswapp.R;
import com.skillswap.skillswapp.data.model.Category;
import com.skillswap.skillswapp.data.model.Skill;
import com.skillswap.skillswapp.databinding.FragmentExploreBinding;
import com.skillswap.skillswapp.ui.adapters.SearchSuggestionAdapter;
import com.skillswap.skillswapp.ui.adapters.SkillAdapter;
import com.skillswap.skillswapp.util.UiUtils;
import com.skillswap.skillswapp.viewmodel.CategoryViewModel;
import com.skillswap.skillswapp.viewmodel.SkillViewModel;
import com.skillswap.skillswapp.viewmodel.UserViewModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Fragmento para la exploración de usuarios y habilidades.
 */
public class ExploreFragment extends Fragment implements SearchSuggestionAdapter.OnSuggestionClickListener, SkillAdapter.OnSkillClickListener {

    private FragmentExploreBinding binding;
    private CategoryViewModel categoryViewModel;
    private UserViewModel userViewModel;
    private SkillViewModel skillViewModel;
    private String currentQuery = "";
    private String currentCategory = "";
    private int currentLevel = 0; // 0: Cualquiera, 1: Principiante, 2: Intermedio, 3: Avanzado
    
    private SearchSuggestionAdapter suggestionAdapter;
    private SkillAdapter featuredSkillsAdapter;
    private List<String> recentSearches = new ArrayList<>();
    private List<Skill> featuredSkills = new ArrayList<>();
    
    private static final String PREF_RECENT_SEARCHES = "recent_searches";
    private static final String PREF_SAVED_SEARCHES = "saved_searches";
    private static final int MAX_RECENT_SEARCHES = 5;
    
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentExploreBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        try {
            // Inicializar ViewModels con el contexto adecuado
            categoryViewModel = new CategoryViewModel();
            userViewModel = new UserViewModel();
            skillViewModel = new SkillViewModel();
            
            // Cargar búsquedas recientes
            loadRecentSearches();
            
            // Configurar animaciones
            setupAnimations();
            
            // Configurar componentes de UI
            setupViewPager();
            setupSearchView();
            setupSuggestions();
            setupFeaturedSkills();
            setupFilterButtons();
            loadCategories();
            loadFeaturedSkills();
        } catch (Exception e) {
            // Manejar cualquier excepción durante la inicialización
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error al inicializar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            e.printStackTrace();
        }
    }
    

    
    private void setupAnimations() {
        try {
            // Configurar animaciones para cambios de layout
            ViewGroup searchCard = binding.cardSearch.findViewById(android.R.id.content);
            if (searchCard instanceof ViewGroup) {
                LayoutTransition transition = new LayoutTransition();
                transition.enableTransitionType(LayoutTransition.CHANGING);
                ((ViewGroup) searchCard).setLayoutTransition(transition);
            }
            
            // Usar una animación simple en lugar de loadLayoutAnimation que puede causar problemas
            binding.recyclerViewSuggestions.setAnimation(AnimationUtils.loadAnimation(requireContext(), android.R.anim.fade_in));
        } catch (Exception e) {
            // Ignorar errores de animación, no son críticos para la funcionalidad
            e.printStackTrace();
        }
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
        // Configurar listener para búsqueda con delay para evitar muchas consultas
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                // Mostrar sugerencias si hay texto
                if (!query.isEmpty()) {
                    showSuggestions(query);
                } else {
                    hideSuggestions();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Cancelar búsqueda anterior si existe
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                
                // Crear nueva búsqueda con delay
                searchRunnable = () -> {
                    currentQuery = s.toString().trim();
                    if (!currentQuery.isEmpty()) {
                        performSearch();
                        addToRecentSearches(currentQuery);
                    }
                };
                
                // Ejecutar búsqueda después de 500ms para evitar muchas consultas
                searchHandler.postDelayed(searchRunnable, 500);
            }
        });
        
        // Configurar acción de búsqueda en teclado
        binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                currentQuery = binding.etSearch.getText().toString().trim();
                if (!currentQuery.isEmpty()) {
                    performSearch();
                    hideSuggestions();
                    addToRecentSearches(currentQuery);
                    UiUtils.hideKeyboard(requireActivity());
                }
                return true;
            }
            return false;
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
        
        // Configurar botones de filtros avanzados
        binding.btnShowFilters.setOnClickListener(v -> {
            if (binding.filterSection.getVisibility() == View.VISIBLE) {
                binding.filterSection.setVisibility(View.GONE);
                binding.btnShowFilters.setText(R.string.advanced_filters);
            } else {
                binding.filterSection.setVisibility(View.VISIBLE);
                binding.filterSection.startAnimation(AnimationUtils.loadAnimation(requireContext(), android.R.anim.fade_in));
                binding.btnShowFilters.setText(R.string.hide_filters);
            }
        });
        
        // Configurar botón de búsquedas guardadas
        binding.btnSavedSearches.setOnClickListener(v -> {
            showSavedSearchesDialog();
        });
    }
    
    private void setupSuggestions() {
        // Inicializar adaptador de sugerencias
        suggestionAdapter = new SearchSuggestionAdapter(new ArrayList<>());
        suggestionAdapter.setOnSuggestionClickListener(this);
        
        // Configurar RecyclerView
        binding.recyclerViewSuggestions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewSuggestions.setAdapter(suggestionAdapter);
    }
    
    private void setupFeaturedSkills() {
        // Inicializar adaptador de habilidades destacadas
        featuredSkillsAdapter = new SkillAdapter(featuredSkills, true);
        featuredSkillsAdapter.setOnSkillClickListener(this);
        
        // Configurar RecyclerView horizontal
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        binding.recyclerViewFeatured.setLayoutManager(layoutManager);
        binding.recyclerViewFeatured.setAdapter(featuredSkillsAdapter);
    }
    
    private void setupFilterButtons() {
        // Configurar botón para mostrar/ocultar filtros
        binding.btnShowFilters.setOnClickListener(v -> {
            boolean isVisible = binding.filterSection.getVisibility() == View.VISIBLE;
            binding.filterSection.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            binding.btnShowFilters.setText(isVisible ? R.string.show_filters : R.string.hide_filters);
            binding.btnShowFilters.setIcon(getResources().getDrawable(
                    isVisible ? R.drawable.ic_filter : R.drawable.ic_close, 
                    requireContext().getTheme()));
        });
        
        // Configurar grupo de botones de nivel usando MaterialButtonToggleGroup
        binding.toggleLevel.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnAnyLevel) {
                    currentLevel = 0; // Cualquier nivel
                } else if (checkedId == R.id.btnBeginner) {
                    currentLevel = 1; // Principiante
                } else if (checkedId == R.id.btnIntermediate) {
                    currentLevel = 2; // Intermedio
                } else if (checkedId == R.id.btnAdvanced) {
                    currentLevel = 3; // Avanzado
                }
            }
        });
        
        // Seleccionar el botón "Cualquier nivel" por defecto
        binding.toggleLevel.check(R.id.btnAnyLevel);
        
        // Botón para aplicar filtros
        binding.btnApplyFilters.setOnClickListener(v -> {
            performSearch();
            binding.filterSection.setVisibility(View.GONE);
            binding.btnShowFilters.setText(R.string.show_filters);
            UiUtils.showSnackbar(binding.getRoot(), "Filtros aplicados");
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

    private void loadFeaturedSkills() {
        skillViewModel.getFeaturedSkills().observe(getViewLifecycleOwner(), skills -> {
            if (skills != null && !skills.isEmpty()) {
                featuredSkills.clear();
                featuredSkills.addAll(skills);
                featuredSkillsAdapter.notifyDataSetChanged();
                binding.cardFeatured.setVisibility(View.VISIBLE);
            } else {
                binding.cardFeatured.setVisibility(View.GONE);
            }
        });
    }
    
    private void showSuggestions(String query) {
        if (query.length() < 2) {
            hideSuggestions();
            return;
        }
        
        // Obtener sugerencias basadas en la consulta
        skillViewModel.getSearchSuggestions(query).observe(getViewLifecycleOwner(), suggestions -> {
            if (suggestions != null && !suggestions.isEmpty()) {
                suggestionAdapter.updateSuggestions(suggestions);
                binding.recyclerViewSuggestions.setVisibility(View.VISIBLE);
                binding.recyclerViewSuggestions.scheduleLayoutAnimation();
            } else {
                hideSuggestions();
            }
        });
    }
    
    private void hideSuggestions() {
        binding.recyclerViewSuggestions.setVisibility(View.GONE);
    }
    
    private void loadRecentSearches() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("search_prefs", Context.MODE_PRIVATE);
        Set<String> savedSearches = prefs.getStringSet(PREF_RECENT_SEARCHES, new HashSet<>());
        recentSearches = new ArrayList<>(savedSearches);
    }
    
    private void addToRecentSearches(String query) {
        // Evitar duplicados
        if (recentSearches.contains(query)) {
            recentSearches.remove(query);
        }
        
        // Añadir al principio
        recentSearches.add(0, query);
        
        // Limitar a MAX_RECENT_SEARCHES
        if (recentSearches.size() > MAX_RECENT_SEARCHES) {
            recentSearches = recentSearches.subList(0, MAX_RECENT_SEARCHES);
        }
        
        // Guardar en SharedPreferences
        SharedPreferences prefs = requireActivity().getSharedPreferences("search_prefs", Context.MODE_PRIVATE);
        prefs.edit().putStringSet(PREF_RECENT_SEARCHES, new HashSet<>(recentSearches)).apply();
    }
    
    private void saveSearch(String query, String name) {
        SharedPreferences prefs = requireActivity().getSharedPreferences("search_prefs", Context.MODE_PRIVATE);
        Set<String> savedSearches = prefs.getStringSet(PREF_SAVED_SEARCHES, new HashSet<>());
        
        // Formato: nombre:consulta:categoría:nivel
        String searchData = name + ":" + query + ":" + currentCategory + ":" + currentLevel;
        
        // Guardar búsqueda
        Set<String> updatedSearches = new HashSet<>(savedSearches);
        updatedSearches.add(searchData);
        
        prefs.edit().putStringSet(PREF_SAVED_SEARCHES, updatedSearches).apply();
        
        UiUtils.showSnackbar(binding.getRoot(), getString(R.string.search_saved));
    }
    
    private void showSavedSearchesDialog() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("search_prefs", Context.MODE_PRIVATE);
        Set<String> savedSearches = prefs.getStringSet(PREF_SAVED_SEARCHES, new HashSet<>());
        
        if (savedSearches.isEmpty()) {
            // Mostrar diálogo para guardar la búsqueda actual
            showSaveSearchDialog();
            return;
        }
        
        // Convertir a lista para mostrar en diálogo
        List<String> searchNames = new ArrayList<>();
        List<String> searchData = new ArrayList<>(savedSearches);
        
        for (String data : searchData) {
            String[] parts = data.split(":");
            if (parts.length > 0) {
                searchNames.add(parts[0]);
            }
        }
        
        // Mostrar diálogo con búsquedas guardadas
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.saved_searches)
                .setItems(searchNames.toArray(new String[0]), (dialog, which) -> {
                    // Aplicar búsqueda guardada
                    String[] parts = searchData.get(which).split(":");
                    if (parts.length >= 4) {
                        String name = parts[0];
                        String query = parts[1];
                        String category = parts[2];
                        int level = Integer.parseInt(parts[3]);
                        
                        // Aplicar filtros
                        binding.etSearch.setText(query);
                        currentQuery = query;
                        currentCategory = category;
                        currentLevel = level;
                        
                        // Actualizar UI
                        updateCategoryFromId(category);
                        performSearch();
                    }
                })
                .setNeutralButton(R.string.cancel, null)
                .setPositiveButton(R.string.save_search, (dialog, which) -> showSaveSearchDialog())
                .show();
    }
    
    private void showSaveSearchDialog() {
        // Crear EditText para el nombre de la búsqueda
        TextInputEditText editText = new TextInputEditText(requireContext());
        editText.setHint(R.string.search_name);
        
        // Mostrar diálogo para guardar búsqueda
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.save_search)
                .setView(editText)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String name = editText.getText().toString().trim();
                    if (!name.isEmpty() && !currentQuery.isEmpty()) {
                        saveSearch(currentQuery, name);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
    
    private void updateCategoryFromId(String categoryId) {
        if (categoryId.isEmpty()) {
            binding.chipAll.setChecked(true);
            return;
        }
        
        // Buscar categoría por ID
        categoryViewModel.getCategoryById(categoryId).observe(getViewLifecycleOwner(), category -> {
            if (category != null) {
                updateCategoryChips(category);
            } else {
                binding.chipAll.setChecked(true);
            }
        });
    }
    
    private void performSearch() {
        // Ocultar sugerencias
        hideSuggestions();
        
        // Notificar a los fragmentos hijos sobre la búsqueda
        int currentTab = binding.viewPager.getCurrentItem();
        Fragment fragment = getChildFragmentManager().findFragmentByTag("f" + currentTab);
        
        if (fragment instanceof ExploreUsersFragment) {
            ((ExploreUsersFragment) fragment).search(currentQuery, currentCategory, currentLevel);
        } else if (fragment instanceof ExploreSkillsFragment) {
            ((ExploreSkillsFragment) fragment).search(currentQuery, currentCategory, currentLevel);
        }
    }

    @Override
    public void onSuggestionClick(String suggestion) {
        // Establecer la sugerencia como consulta
        binding.etSearch.setText(suggestion);
        binding.etSearch.setSelection(suggestion.length());
        
        // Ocultar sugerencias
        hideSuggestions();
        
        // Realizar búsqueda
        currentQuery = suggestion;
        performSearch();
        
        // Añadir a búsquedas recientes
        addToRecentSearches(suggestion);
    }
    
    @Override
    public void onSkillClick(SkillAdapter.SkillItem skill) {
        // Navegar al detalle de la habilidad
        Bundle args = new Bundle();
        args.putString("skillId", skill.getId());
        
        // Usar NavController para navegar
        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                .navigate(R.id.action_navigation_explore_to_skillDetailFragment, args);
    }
    
    // Método para manejar clicks en habilidades del tipo Skill (mantener compatibilidad)
    public void onSkillClick(Skill skill) {
        // Navegar al detalle de la habilidad
        Bundle args = new Bundle();
        args.putString("skillId", skill.getSkillId());
        
        // Usar NavController para navegar
        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                .navigate(R.id.action_navigation_explore_to_skillDetailFragment, args);
    }
    
    // Método para manejar clicks en el botón de favorito
    public void onFavoriteClick(Skill skill, boolean isFavorite) {
        // Manejar click en favorito
        if (isFavorite) {
            skillViewModel.addFavoriteSkill(skill.getSkillId());
            UiUtils.showSnackbar(binding.getRoot(), getString(R.string.added_to_favorites));
        } else {
            skillViewModel.removeFavoriteSkill(skill.getSkillId());
            UiUtils.showSnackbar(binding.getRoot(), getString(R.string.removed_from_favorites));
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        
        // Cancelar búsquedas pendientes
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
        
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
