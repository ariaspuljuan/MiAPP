package com.skillswap.skillswapp.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.skillswap.skillswapp.R;
import com.skillswap.skillswapp.data.model.User;

import java.util.List;

/**
 * Adaptador para mostrar usuarios en RecyclerView.
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final List<User> users;
    private OnUserClickListener listener;

    public UserAdapter(List<User> users) {
        this.users = users;
    }

    public void setOnUserClickListener(OnUserClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    /**
     * ViewHolder para los usuarios.
     */
    class UserViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivUserPhoto;
        private final TextView tvUserName;
        private final TextView tvUserBio;
        private final ImageView ivFavorite;
        private final TextView tvSkillsCount;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserPhoto = itemView.findViewById(R.id.ivUserPhoto);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserBio = itemView.findViewById(R.id.tvUserBio);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
            tvSkillsCount = itemView.findViewById(R.id.tvSkillsCount);
            
            // Configurar listener para click en el item
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onUserClick(users.get(position));
                }
            });
            
            // Configurar listener para click en favorito
            ivFavorite.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    User user = users.get(position);
                    boolean isFavorite = !user.isFavorite(); // Cambiar estado
                    user.setFavorite(isFavorite);
                    notifyItemChanged(position);
                    listener.onFavoriteClick(user, isFavorite);
                }
            });
        }

        public void bind(User user) {
            // Establecer nombre del usuario
            tvUserName.setText(user.getProfile().getName());
            
            // Establecer biografía si existe
            String bio = user.getProfile().getBio();
            if (bio != null && !bio.isEmpty()) {
                tvUserBio.setVisibility(View.VISIBLE);
                tvUserBio.setText(bio);
            } else {
                tvUserBio.setVisibility(View.GONE);
            }
            
            // Cargar foto de perfil si existe
            String photoUrl = user.getProfile().getPhotoUrl();
            if (photoUrl != null && !photoUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(photoUrl)
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .error(R.drawable.ic_profile_placeholder)
                        .circleCrop()
                        .into(ivUserPhoto);
            } else {
                ivUserPhoto.setImageResource(R.drawable.ic_profile_placeholder);
            }
            
            // Establecer icono de favorito
            ivFavorite.setImageResource(user.isFavorite() ? 
                    R.drawable.ic_favorite_filled : R.drawable.ic_favorite_outline);
            
            // Establecer contador de habilidades
            int skillsCount = 0;
            if (user.getSkillsToTeach() != null) {
                skillsCount += user.getSkillsToTeach().size();
            }
            tvSkillsCount.setText(itemView.getContext().getString(
                    R.string.skills_count, skillsCount));
        }
    }

    /**
     * Interfaz para manejar clicks en usuarios.
     */
    public interface OnUserClickListener {
        void onUserClick(User user);
        void onFavoriteClick(User user, boolean isFavorite);
    }
}
