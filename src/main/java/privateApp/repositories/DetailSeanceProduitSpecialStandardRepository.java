package privateApp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import privateApp.models.DetailSeanceProduitSpecialStandard;

import java.util.List;

@Repository
public interface DetailSeanceProduitSpecialStandardRepository extends JpaRepository<DetailSeanceProduitSpecialStandard, Long> {
    List<DetailSeanceProduitSpecialStandard> findBySeanceIdSeance(Long seanceId);

    @Query("SELECT d FROM DetailSeanceProduitSpecialStandard d JOIN FETCH d.seance s JOIN FETCH s.patient WHERE d.seance.idSeance = :seanceId")
    List<DetailSeanceProduitSpecialStandard> findBySeanceIdSeanceWithPatient(Long seanceId);

    @Query("SELECT d FROM DetailSeanceProduitSpecialStandard d JOIN FETCH d.seance s JOIN FETCH s.patient")
    List<DetailSeanceProduitSpecialStandard> findAllWithSeanceAndPatient();

    @Modifying
    @Query("DELETE FROM DetailSeanceProduitSpecialStandard d WHERE d.seance.idSeance = :seanceId AND d.standard = false")
    void deleteBySeanceIdSeanceAndStandardFalse(Long seanceId);
}