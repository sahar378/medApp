package privateApp.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import privateApp.models.DetailMesure;

/**
 * Repository pour gérer les opérations sur l'entité DetailMesure.
 */
public interface DetailMesureRepository extends JpaRepository<DetailMesure, Long> {
	/**
     * Trouve les mesures associées à une séance.
     * @param seanceId L'ID de la séance.
     * @return Liste des mesures.
     */
    List<DetailMesure> findBySeanceIdSeance(Long seanceId);
}