package privateApp.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import privateApp.models.DetailSeanceProduitSpecialStandard;

/**
 * Repository pour gérer les opérations sur l'entité DetailSeanceProduitSpecialStandard.
 */
public interface DetailSeanceProduitSpecialStandardRepository extends JpaRepository<DetailSeanceProduitSpecialStandard, Long> {
	/**
     * Trouve les produits associés à une séance.
     * @param seanceId L'ID de la séance.
     * @return Liste des détails des produits.
     */
    List<DetailSeanceProduitSpecialStandard> findBySeanceIdSeance(Long seanceId);
    @Query("SELECT d FROM DetailSeanceProduitSpecialStandard d JOIN FETCH d.seance s JOIN FETCH s.patient p")
    List<DetailSeanceProduitSpecialStandard> findAllWithSeanceAndPatient();
}