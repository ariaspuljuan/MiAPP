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
     * Obtiene el estado de carga.
     * @return LiveData con el estado de carga
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
}
