package com.skillswap.skillswapp.ui.skills;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.skillswap.skillswapp.R;
import com.skillswap.skillswapp.data.model.Category;
import com.skillswap.skillswapp.data.model.User;
import com.skillswap.skillswapp.databinding.DialogAddSkillBinding;
import com.skillswap.skillswapp.util.UiUtils;
import com.skillswap.skillswapp.viewmodel.CategoryViewModel;
import com.skillswap.skillswapp.viewmodel.UserViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Diálogo para editar o eliminar una habilidad existente.
 */
public class EditSkillDialogFragment extends DialogFragment {

    private DialogAddSkillBinding binding;
    private UserViewModel userViewModel;
    private CategoryViewModel categoryViewModel;
    private boolean isTeachSkill;
    private String skillId;
    private List<Category> categories = new ArrayList<>();
    private User.SkillToTeach teachSkill;
    private User.SkillToLearn learnSkill;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogAddSkillBinding.inflate(LayoutInflater.from(getContext()));
        
        // Obtener argumentos
        if (getArguments() != null) {
            isTeachSkill = getArguments().getBoolean("isTeachSkill", true);
            skillId = getArguments().getString("skillId", "");
        }
        
        // Inicializar ViewModels
        userViewModel = new UserViewModel();
        categoryViewModel = new CategoryViewModel();
        
        // Configurar UI según el tipo de habilidad
        setupUI();
        
        // Cargar categorías
        loadCategories();
        
        // Cargar datos de la habilidad
        loadSkillData();
        
        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(isTeachSkill ? R.string.edit_skill_to_teach : R.string.edit_skill_to_learn)
                .setView(binding.getRoot())
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    saveSkill();
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dismiss())
                .setNeutralButton(R.string.delete, (dialog, which) -> {
                    deleteSkill();
                })
                .create();
    }

    private void setupUI() {
        // Configurar campos según el tipo de habilidad
        if (isTeachSkill) {
            // Para habilidades que enseña
            binding.layoutLevel.setVisibility(View.VISIBLE);
            binding.layoutPriority.setVisibility(View.GONE);
            binding.layoutCategory.setVisibility(View.VISIBLE);
            binding.layoutDescription.setVisibility(View.VISIBLE);
            
            // Configurar SeekBar de nivel
            binding.seekBarLevel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    binding.tvLevelValue.setText(String.valueOf(progress + 1));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
        } else {
            // Para habilidades que quiere aprender
            binding.layoutLevel.setVisibility(View.GONE);
            binding.layoutPriority.setVisibility(View.VISIBLE);
            binding.layoutCategory.setVisibility(View.GONE);
            binding.layoutDescription.setVisibility(View.GONE);
            
            // Configurar SeekBar de prioridad
            binding.seekBarPriority.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    String[] priorities = {"Baja", "Media", "Alta"};
                    binding.tvPriorityValue.setText(priorities[progress]);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
        }
    }

    private void loadCategories() {
        categoryViewModel.getAllCategories().observe(this, categoriesList -> {
            if (categoriesList != null && !categoriesList.isEmpty()) {
                categories = categoriesList;
                
                // Crear adaptador para el spinner de categorías
                List<String> categoryNames = new ArrayList<>();
                for (Category category : categories) {
                    categoryNames.add(category.getName());
                }
                
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        categoryNames
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.spinnerCategory.setAdapter(adapter);
                
                // Si estamos editando una habilidad para enseñar, seleccionar la categoría
                if (isTeachSkill && teachSkill != null) {
                    for (int i = 0; i < categories.size(); i++) {
                        if (categories.get(i).getName().equals(teachSkill.getCategory())) {
                            binding.spinnerCategory.setSelection(i);
                            break;
                        }
                    }
                }
            }
        });
    }

    private void loadSkillData() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        userViewModel.getUserById(userId).observe(this, user -> {
            if (user != null) {
                if (isTeachSkill && user.getSkillsToTeach() != null) {
                    // Obtener la habilidad para enseñar
                    teachSkill = user.getSkillsToTeach().get(skillId);
                    if (teachSkill != null) {
                        binding.etTitle.setText(teachSkill.getTitle());
                        binding.etDescription.setText(teachSkill.getDescription());
                        binding.seekBarLevel.setProgress(teachSkill.getLevel() - 1); // 0-4 para UI, 1-5 para datos
                    }
                } else if (!isTeachSkill && user.getSkillsToLearn() != null) {
                    // Obtener la habilidad para aprender
                    learnSkill = user.getSkillsToLearn().get(skillId);
                    if (learnSkill != null) {
                        binding.etTitle.setText(learnSkill.getTitle());
                        binding.seekBarPriority.setProgress(learnSkill.getPriority() - 1); // 0-2 para UI, 1-3 para datos
                    }
                }
            }
        });
    }

    private void saveSkill() {
        final String title = binding.etTitle.getText().toString().trim();
        
        // Validar título
        if (title.isEmpty()) {
            UiUtils.showSnackbar(binding.getRoot(), getString(R.string.error_empty_title));
            return;
        }
        
        // Obtener ID del usuario actual
        final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        if (isTeachSkill) {
            // Guardar habilidad para enseñar
            final String categoryId;
            String categoryName = "";
            if (binding.spinnerCategory.getSelectedItemPosition() >= 0 && binding.spinnerCategory.getSelectedItemPosition() < categories.size()) {
                categoryId = categories.get(binding.spinnerCategory.getSelectedItemPosition()).getCategoryId();
                categoryName = categories.get(binding.spinnerCategory.getSelectedItemPosition()).getName();
            } else {
                categoryId = "";
            }
            
            String description = binding.etDescription.getText().toString().trim();
            int level = binding.seekBarLevel.getProgress() + 1; // 1-5
            
            // Guardar en Firebase
            Map<String, Object> skillMap = new HashMap<>();
            skillMap.put("title", title);
            skillMap.put("category", categoryName);
            skillMap.put("description", description);
            skillMap.put("level", level);
            
            userViewModel.updateUserField(userId, "skills_to_teach/" + skillId, skillMap).observe(this, success -> {
                if (success) {
                    // Notificar al fragmento padre para actualizar la lista
                    if (getParentFragment() instanceof TeachSkillsFragment) {
                        ((TeachSkillsFragment) getParentFragment()).refreshSkills();
                    }
                    
                    // Actualizar la habilidad en la colección global de habilidades
                    userViewModel.updateSkillInGlobal(skillId, title, categoryId);
                    
                    dismiss();
                } else {
                    UiUtils.showSnackbar(binding.getRoot(), getString(R.string.error_saving_skill));
                }
            });
        } else {
            // Guardar habilidad para aprender
            int priority = binding.seekBarPriority.getProgress() + 1; // 1-3
            
            // Guardar en Firebase
            Map<String, Object> skillMap = new HashMap<>();
            skillMap.put("title", title);
            skillMap.put("priority", priority);
            
            userViewModel.updateUserField(userId, "skills_to_learn/" + skillId, skillMap).observe(this, success -> {
                if (success) {
                    // Notificar al fragmento padre para actualizar la lista
                    if (getParentFragment() instanceof LearnSkillsFragment) {
                        ((LearnSkillsFragment) getParentFragment()).refreshSkills();
                    }
                    
                    dismiss();
                } else {
                    UiUtils.showSnackbar(binding.getRoot(), getString(R.string.error_saving_skill));
                }
            });
        }
    }

    private void deleteSkill() {
        // Obtener ID del usuario actual
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        if (isTeachSkill) {
            // Eliminar habilidad para enseñar
            userViewModel.updateUserField(userId, "skills_to_teach/" + skillId, null).observe(this, success -> {
                if (success) {
                    // Notificar al fragmento padre para actualizar la lista
                    if (getParentFragment() instanceof TeachSkillsFragment) {
                        ((TeachSkillsFragment) getParentFragment()).refreshSkills();
                    }
                    
                    // Eliminar al usuario de la lista de usuarios que enseñan esta habilidad
                    userViewModel.removeUserFromSkill(skillId, userId);
                    
                    dismiss();
                } else {
                    UiUtils.showSnackbar(binding.getRoot(), getString(R.string.error_deleting_skill));
                }
            });
        } else {
            // Eliminar habilidad para aprender
            userViewModel.updateUserField(userId, "skills_to_learn/" + skillId, null).observe(this, success -> {
                if (success) {
                    // Notificar al fragmento padre para actualizar la lista
                    if (getParentFragment() instanceof LearnSkillsFragment) {
                        ((LearnSkillsFragment) getParentFragment()).refreshSkills();
                    }
                    
                    dismiss();
                } else {
                    UiUtils.showSnackbar(binding.getRoot(), getString(R.string.error_deleting_skill));
                }
            });
        }
    }
}
