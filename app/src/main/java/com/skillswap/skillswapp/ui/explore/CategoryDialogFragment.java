package com.skillswap.skillswapp.ui.explore;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.skillswap.skillswapp.R;
import com.skillswap.skillswapp.data.model.Category;
import com.skillswap.skillswapp.databinding.DialogCategoriesBinding;
import com.skillswap.skillswapp.viewmodel.CategoryViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Diálogo para seleccionar una categoría.
 */
public class CategoryDialogFragment extends DialogFragment {

    private DialogCategoriesBinding binding;
    private CategoryViewModel categoryViewModel;
    private CategoryAdapter categoryAdapter;
    private OnCategorySelectedListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogCategoriesBinding.inflate(LayoutInflater.from(getContext()));
        
        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.select_category)
                .setView(binding.getRoot())
                .setNegativeButton(R.string.cancel, (dialog, which) -> dismiss())
                .create();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Inicializar ViewModel
        categoryViewModel = new CategoryViewModel();
        
        setupRecyclerView();
        loadCategories();
    }

    private void setupRecyclerView() {
        categoryAdapter = new CategoryAdapter(new ArrayList<>());
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(categoryAdapter);
        
        categoryAdapter.setOnCategoryClickListener(category -> {
            if (listener != null) {
                listener.onCategorySelected(category);
            }
            dismiss();
        });
    }

    private void loadCategories() {
        binding.progressBar.setVisibility(View.VISIBLE);
        
        categoryViewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            binding.progressBar.setVisibility(View.GONE);
            
            if (categories != null && !categories.isEmpty()) {
                categoryAdapter.setCategories(categories);
                binding.tvEmptyState.setVisibility(View.GONE);
            } else {
                binding.tvEmptyState.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Establece el listener para la selección de categorías.
     * @param listener Listener a establecer
     */
    public void setOnCategorySelectedListener(OnCategorySelectedListener listener) {
        this.listener = listener;
    }

    /**
     * Interfaz para manejar la selección de categorías.
     */
    public interface OnCategorySelectedListener {
        void onCategorySelected(Category category);
    }

    /**
     * Adaptador para mostrar categorías en RecyclerView.
     */
    private static class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

        private List<Category> categories;
        private OnCategoryClickListener listener;

        public CategoryAdapter(List<Category> categories) {
            this.categories = categories;
        }

        public void setCategories(List<Category> categories) {
            this.categories = categories;
            notifyDataSetChanged();
        }

        public void setOnCategoryClickListener(OnCategoryClickListener listener) {
            this.listener = listener;
        }

        @NonNull
        @Override
        public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
            return new CategoryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
            Category category = categories.get(position);
            holder.bind(category);
        }

        @Override
        public int getItemCount() {
            return categories.size();
        }

        /**
         * ViewHolder para las categorías.
         */
        class CategoryViewHolder extends RecyclerView.ViewHolder {
            private final TextView tvCategoryName;
            private final TextView tvCategoryDescription;

            public CategoryViewHolder(@NonNull View itemView) {
                super(itemView);
                tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
                tvCategoryDescription = itemView.findViewById(R.id.tvCategoryDescription);
                
                itemView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onCategoryClick(categories.get(position));
                    }
                });
            }

            public void bind(Category category) {
                tvCategoryName.setText(category.getName());
                
                if (category.getDescription() != null && !category.getDescription().isEmpty()) {
                    tvCategoryDescription.setVisibility(View.VISIBLE);
                    tvCategoryDescription.setText(category.getDescription());
                } else {
                    tvCategoryDescription.setVisibility(View.GONE);
                }
            }
        }

        /**
         * Interfaz para manejar clicks en categorías.
         */
        interface OnCategoryClickListener {
            void onCategoryClick(Category category);
        }
    }
}
