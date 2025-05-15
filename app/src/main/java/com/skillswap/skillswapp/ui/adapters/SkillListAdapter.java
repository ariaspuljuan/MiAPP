package com.skillswap.skillswapp.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.skillswap.skillswapp.R;
import com.skillswap.skillswapp.data.model.Skill;

import java.util.List;

/**
 * Adaptador para mostrar lista de habilidades en RecyclerView.
 */
public class SkillListAdapter extends RecyclerView.Adapter<SkillListAdapter.SkillViewHolder> {

    private final List<Skill> skills;
    private OnSkillClickListener listener;

    public SkillListAdapter(List<Skill> skills) {
        this.skills = skills;
    }

    public void setOnSkillClickListener(OnSkillClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public SkillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_skill_list, parent, false);
        return new SkillViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SkillViewHolder holder, int position) {
        Skill skill = skills.get(position);
        holder.bind(skill);
    }

    @Override
    public int getItemCount() {
        return skills.size();
    }

    /**
     * ViewHolder para las habilidades.
     */
    class SkillViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvSkillTitle;
        private final Chip chipCategory;
        private final TextView tvTeachersCount;

        public SkillViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSkillTitle = itemView.findViewById(R.id.tvSkillTitle);
            chipCategory = itemView.findViewById(R.id.chipCategory);
            tvTeachersCount = itemView.findViewById(R.id.tvTeachersCount);
            
            // Configurar listener para click en el item
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onSkillClick(skills.get(position));
                }
            });
        }

        public void bind(Skill skill) {
            // Establecer título de la habilidad
            tvSkillTitle.setText(skill.getTitle());
            
            // Establecer categoría
            if (skill.getCategory() != null && !skill.getCategory().isEmpty()) {
                chipCategory.setVisibility(View.VISIBLE);
                chipCategory.setText(skill.getCategory());
            } else {
                chipCategory.setVisibility(View.GONE);
            }
            
            // Establecer contador de profesores
            int teachersCount = 0;
            if (skill.getUsersTeaching() != null) {
                teachersCount = skill.getUsersTeaching().size();
            }
            
            tvTeachersCount.setText(itemView.getContext().getString(
                    R.string.teachers_count, teachersCount));
        }
    }

    /**
     * Interfaz para manejar clicks en habilidades.
     */
    public interface OnSkillClickListener {
        void onSkillClick(Skill skill);
    }
}
