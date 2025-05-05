package privateApp.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import privateApp.models.Seance;

/**
 * Repository pour gérer les opérations sur l'entité Seance.
 */
public interface SeanceRepository extends JpaRepository<Seance, Long> {
	 /**
     * Récupère toutes les séances associées à un patient donné.
     * @param patientId L'ID du patient.
     * @return Liste des séances.
     */
    List<Seance> findByPatientIdPatient(Long patientId);

    @Query("SELECT s FROM Seance s WHERE s.date BETWEEN :startDate AND :endDate ORDER BY s.date DESC")
    List<Seance> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                 @Param("endDate") LocalDateTime endDate);
    
    /**
     * Finds sessions for a patient within a date range, ordered by date descending.
     * @param patientId The ID of the patient.
     * @param startDate The start date.
     * @param endDate The end date.
     * @return List of sessions.
     */
    /*@Query("SELECT s FROM Seance s WHERE s.patient.idPatient = :patientId AND s.date BETWEEN :startDate AND :endDate ORDER BY s.date DESC")
    List<Seance> findByPatientIdAndDateRange(@Param("patientId") Long patientId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
*/
	List<Seance> findByPatientIdPatientOrderByDateDesc(Long patientId);
	
	/**
     * Récupère toutes les séances, triées par ID de séance en ordre décroissant.
     * @return Liste des séances.
     */
    @Query("SELECT s FROM Seance s ORDER BY s.idSeance DESC")
    List<Seance> findAllOrderByIdSeanceDesc();
    
    @Query("SELECT s FROM Seance s WHERE s.patient.idPatient = :patientId AND s.date BETWEEN :startDate AND :endDate ORDER BY s.date DESC")
    List<Seance> findByPatientIdAndDateRange(@Param("patientId") Long patientId, 
                                             @Param("startDate") LocalDateTime startDate, 
                                             @Param("endDate") LocalDateTime endDate);
}