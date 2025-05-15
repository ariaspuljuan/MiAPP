package com.skillswap.skillswapp.ui.contacts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.material.tabs.TabLayoutMediator;
import com.skillswap.skillswapp.databinding.FragmentContactsBinding;

/**
 * Fragmento para mostrar los contactos del usuario.
 */
public class ContactsFragment extends Fragment {

    private FragmentContactsBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentContactsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupViewPager();
    }

    private void setupViewPager() {
        // Configurar adaptador para ViewPager
        ContactsViewPagerAdapter adapter = new ContactsViewPagerAdapter(this);
        binding.viewPager.setAdapter(adapter);
        
        // Conectar TabLayout con ViewPager
        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Favoritos");
                    break;
                case 1:
                    tab.setText("Recientes");
                    break;
            }
        }).attach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Adaptador para el ViewPager de contactos.
     */
    private static class ContactsViewPagerAdapter extends FragmentStateAdapter {
        
        public ContactsViewPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new FavoritesFragment();
                case 1:
                    return new RecentContactsFragment();
                default:
                    return new FavoritesFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}
