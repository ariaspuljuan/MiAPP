package com.skillswap.skillswapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.skillswap.skillswapp.data.model.Skill;
import com.skillswap.skillswapp.data.repository.SkillRepository;

import java.util.List;

/**
 * ViewModel para gestionar las habilidades.
 */
public class SkillViewModel extends ViewModel {

    private final SkillRepository skillRepository;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public SkillViewModel() {
        this.skillRepository = SkillRepository.getInstance();
    }

    /**
     * Obtiene todas las habilidades.
     * @return LiveData con la lista de habilidades
     */
    public LiveData<List<Skill>> getAllSkills() {
        isLoading.setValue(true);
        LiveData<List<Skill>> skills = skillRepository.getAllSkills();
        isLoading.setValue(false);
        return skills;
    }

    /**
     * Obtiene una habilidad por su ID.
     * @param skillId ID de la habilidad
     * @return LiveData con la habilidad
     */
    public LiveData<Skill> getSkillById(String skillId) {
        isLoading.setValue(true);
        LiveData<Skill> skill = skillRepository.getSkillById(skillId);
        isLoading.setValue(false);
        return skill;
    }

    /**
     * Busca habilidades por texto y/o categoría.
     * @param query Texto de búsqueda
     * @param categoryId ID de categoría para filtrar
     * @return LiveData con la lista de habilidades que coinciden
     */
    public LiveData<List<Skill>> searchSkills(String query, String categoryId) {
        isLoading.setValue(true);
        LiveData<List<Skill>> skills = skillRepository.searchSkills(query, categoryId);
        isLoading.setValue(false);
        return skills;
    }

    /**
     * Obtiene las habilidades por categoría.
     * @param categoryId ID de la categoría
     * @return LiveData con la lista de habilidades de esa categoría
     */
    public LiveData<List<Skill>> getSkillsByCategory(String categoryId) {
        isLoading.setValue(true);
        LiveData<List<Skill>> skills = skillRepository.getSkillsByCategory(categoryId);
        isLoading.setValue(false);
        return skills;
    }

    /**
     * Crea una nueva habilidad.
     * @param skill Habilidad a crear
     * @return LiveData con el resultado (true si se creó correctamente)
     */
    public LiveData<Boolean> createSkill(Skill skill) {
        isLoading.setValue(true);
        LiveData<Boolean> result = skillRepository.createSkill(skill);
        isLoading.setValue(false);
        return result;
    }

    /**
     * Actualiza una habilidad existente.
     * @param skill Habilidad con los datos actualizados
     * @return LiveData con el resultado (true si se actualizó correctamente)
     */
    public LiveData<Boolean> updateSkill(Skill skill) {
        isLoading.setValue(true);
        LiveData<Boolean> result = skillRepository.updateSkill(skill);
        isLoading.setValue(false);
        return result;
    }

    /**
     * Elimina una habilidad.
     * @param skillId ID de la habilidad a eliminar
     * @return LiveData con el resultado (true si se eliminó correctamente)
     */
    public LiveData<Boolean> deleteSkill(String skillId) {
        isLoading.setValue(true);
        LiveData<Boolean> result = skillRepository.deleteSkill(skillId);
        isLoading.setValue(false);
        return result;
    }
    
    /**
     * Obtiene las habilidades destacadas para mostrar en la pantalla de exploración.
     * @return LiveData con la lista de habilidades destacadas
     */
    public LiveData<List<Skill>> getFeaturedSkills() {
        isLoading.setValue(true);
        LiveData<List<Skill>> skills = skillRepository.getFeaturedSkills();
        isLoading.setValue(false);
        return skills;
    }
    
    /**
     * Busca habilidades con filtros avanzados.
     * @param query Texto de búsqueda
     * @param categoryId ID de categoría para filtrar
     * @param level Nivel de habilidad (0: cualquiera, 1: principiante, 2: intermedio, 3: avanzado)
     * @return LiveData con la lista de habilidades que coinciden
     */
    public LiveData<List<Skill>> searchSkillsAdvanced(String query, String categoryId, int level) {
        isLoading.setValue(true);
        LiveData<List<Skill>> skills = skillRepository.searchSkillsAdvanced(query, categoryId, level);
        isLoading.setValue(false);
        return skills;
    }
    
    /**
     * Obtiene sugerencias de búsqueda basadas en una consulta parcial.
     * @param query Consulta parcial
     * @return LiveData con la lista de sugerencias
     */
    public LiveData<List<String>> getSearchSuggestions(String query) {
        return skillRepository.getSearchSuggestions(query);
    }
    
    /**
     * Añade una habilidad a favoritos.
     * @param skillId ID de la habilidad
     * @return LiveData con el resultado (true si se añadió correctamente)
     */
    public LiveData<Boolean> addFavoriteSkill(String skillId) {
        return skillRepository.addFavoriteSkill(skillId);
    }
    
    /**
     * Elimina una habilidad de favoritos.
     * @param skillId ID de la habilidad
     * @return LiveData con el resultado (true si se eliminó correctamente)
     */
    public LiveData<Boolean> removeFavoriteSkill(String skillId) {
        return skillRepository.removeFavoriteSkill(skillId);
    }
    
    /**
     * Verifica si una habilidad está en favoritos.
     * @param skillId ID de la habilidad
     * @return LiveData con el resultado (true si está en favoritos)
     */
    public LiveData<Boolean> isSkillFavorite(String skillId) {
        return skillRepository.isSkillFavorite(skillId);
    }

    /**
     * Obtiene el estado de carga.
     * @return LiveData con el estado de carga
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
}
