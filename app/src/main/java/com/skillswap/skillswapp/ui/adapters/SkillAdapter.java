package com.skillswap.skillswapp.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.skillswap.skillswapp.R;
import com.skillswap.skillswapp.data.model.Skill;
import com.skillswap.skillswapp.data.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Adaptador para mostrar habilidades en RecyclerView.
 */
public class SkillAdapter extends RecyclerView.Adapter<SkillAdapter.SkillViewHolder> {

    private List<SkillItem> skills;
    private final boolean isTeachSkill;
    private OnSkillClickListener listener;

    /**
     * Constructor del adaptador.
     * @param isTeachSkill true si son habilidades para enseñar, false si son para aprender
     */
    public SkillAdapter(boolean isTeachSkill) {
        this.skills = new ArrayList<>();
        this.isTeachSkill = isTeachSkill;
    }
    
    /**
     * Constructor del adaptador con lista de habilidades.
     * @param skills Lista de habilidades a mostrar
     * @param isTeachSkill true si son habilidades para enseñar, false si son para aprender
     */
    public SkillAdapter(List<Skill> skills, boolean isTeachSkill) {
        this.skills = new ArrayList<>();
        this.isTeachSkill = isTeachSkill;
        
        // Convertir las habilidades al formato interno
        if (skills != null) {
            for (Skill skill : skills) {
                this.skills.add(new SkillItem(
                    skill.getSkillId(),
                    skill.getTitle(),
                    skill.getCategory(),
                    skill.getDescription(),
                    skill.getLevel(),
                    0 // Prioridad por defecto
                ));
            }
        }
    }

    /**
     * Establece las habilidades a mostrar.
     */
    public void setSkills(Map<String, ?> skillsMap) {
        skills.clear();
        
        if (isTeachSkill) {
            Map<String, User.SkillToTeach> teachMap = (Map<String, User.SkillToTeach>) skillsMap;
            for (Map.Entry<String, User.SkillToTeach> entry : teachMap.entrySet()) {
                User.SkillToTeach skill = entry.getValue();
                skills.add(new SkillItem(
                        entry.getKey(),
                        skill.getTitle(),
                        skill.getCategory(),
                        skill.getDescription(),
                        skill.getLevel(),
                        0
                ));
            }
        } else {
            Map<String, User.SkillToLearn> learnMap = (Map<String, User.SkillToLearn>) skillsMap;
            for (Map.Entry<String, User.SkillToLearn> entry : learnMap.entrySet()) {
                User.SkillToLearn skill = entry.getValue();
                skills.add(new SkillItem(
                        entry.getKey(),
                        skill.getTitle(),
                        "",
                        "",
                        0,
                        skill.getPriority()
                ));
            }
        }
        
        notifyDataSetChanged();
    }

    /**
     * Establece el listener para clicks en habilidades.
     */
    public void setOnSkillClickListener(OnSkillClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public SkillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_skill, parent, false);
        return new SkillViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SkillViewHolder holder, int position) {
        SkillItem skill = skills.get(position);
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
        private final TextView tvTitle;
        private final TextView tvCategory;
        private final TextView tvDescription;
        private final TextView tvPriority;
        private final View layoutLevel;
        private final ImageView[] levelStars;

        public SkillViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvSkillTitle);
            tvCategory = itemView.findViewById(R.id.tvSkillCategory);
            tvDescription = itemView.findViewById(R.id.tvSkillDescription);
            tvPriority = itemView.findViewById(R.id.tvPriority);
            layoutLevel = itemView.findViewById(R.id.layoutLevel);
            
            // Inicializar array de estrellas para nivel
            levelStars = new ImageView[5];
            levelStars[0] = itemView.findViewById(R.id.ivLevel1);
            levelStars[1] = itemView.findViewById(R.id.ivLevel2);
            levelStars[2] = itemView.findViewById(R.id.ivLevel3);
            levelStars[3] = itemView.findViewById(R.id.ivLevel4);
            levelStars[4] = itemView.findViewById(R.id.ivLevel5);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onSkillClick(skills.get(position));
                }
            });
        }

        public void bind(SkillItem skill) {
            tvTitle.setText(skill.getTitle());
            
            if (isTeachSkill) {
                // Configurar para habilidades que enseña
                tvPriority.setVisibility(View.GONE);
                layoutLevel.setVisibility(View.VISIBLE);
                
                if (skill.getCategory() != null && !skill.getCategory().isEmpty()) {
                    tvCategory.setVisibility(View.VISIBLE);
                    tvCategory.setText(skill.getCategory());
                } else {
                    tvCategory.setVisibility(View.GONE);
                }
                
                if (skill.getDescription() != null && !skill.getDescription().isEmpty()) {
                    tvDescription.setVisibility(View.VISIBLE);
                    tvDescription.setText(skill.getDescription());
                } else {
                    tvDescription.setVisibility(View.GONE);
                }
                
                // Configurar estrellas según nivel
                int level = skill.getLevel();
                for (int i = 0; i < 5; i++) {
                    levelStars[i].setImageResource(i < level ? 
                            R.drawable.ic_star_filled : R.drawable.ic_star_outline);
                }
            } else {
                // Configurar para habilidades que quiere aprender
                tvCategory.setVisibility(View.GONE);
                tvDescription.setVisibility(View.GONE);
                layoutLevel.setVisibility(View.GONE);
                tvPriority.setVisibility(View.VISIBLE);
                
                // Establecer texto de prioridad
                String priorityText;
                int priorityValue = skill.getPriority();
                switch (priorityValue) {
                    case 1:
                        priorityText = "Baja";
                        break;
                    case 2:
                        priorityText = "Media";
                        break;
                    case 3:
                        priorityText = "Alta";
                        break;
                    default:
                        priorityText = "Media";
                }
                tvPriority.setText(priorityText);
            }
        }
    }

    /**
     * Clase para representar un item de habilidad en el adaptador.
     */
    public static class SkillItem {
        private final String id;
        private final String title;
        private final String category;
        private final String description;
        private final int level;
        private final int priority;

        public SkillItem(String id, String title, String category, String description, int level, int priority) {
            this.id = id;
            this.title = title;
            this.category = category;
            this.description = description;
            this.level = level;
            this.priority = priority;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getCategory() {
            return category;
        }

        public String getDescription() {
            return description;
        }

        public int getLevel() {
            return level;
        }

        public int getPriority() {
            return priority;
        }
    }

    /**
     * Interfaz para manejar clicks en habilidades.
     */
    public interface OnSkillClickListener {
        void onSkillClick(SkillItem skill);
    }
}
