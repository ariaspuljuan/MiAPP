package com.skillswap.skillswapp.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.skillswap.skillswapp.R;

import java.util.List;

/**
 * Adaptador para mostrar sugerencias de búsqueda.
 */
public class SearchSuggestionAdapter extends RecyclerView.Adapter<SearchSuggestionAdapter.SuggestionViewHolder> {

    private List<String> suggestions;
    private OnSuggestionClickListener listener;

    public SearchSuggestionAdapter(List<String> suggestions) {
        this.suggestions = suggestions;
    }

    public void setOnSuggestionClickListener(OnSuggestionClickListener listener) {
        this.listener = listener;
    }

    public void updateSuggestions(List<String> newSuggestions) {
        this.suggestions = newSuggestions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_suggestion, parent, false);
        return new SuggestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionViewHolder holder, int position) {
        String suggestion = suggestions.get(position);
        holder.bind(suggestion);
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    public class SuggestionViewHolder extends RecyclerView.ViewHolder {
        private TextView tvSuggestion;

        public SuggestionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSuggestion = itemView.findViewById(R.id.tvSuggestion);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onSuggestionClick(suggestions.get(position));
                }
            });
        }

        public void bind(String suggestion) {
            tvSuggestion.setText(suggestion);
        }
    }

    /**
     * Interfaz para manejar clics en sugerencias.
     */
    public interface OnSuggestionClickListener {
        void onSuggestionClick(String suggestion);
    }
}
