package privateApp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import privateApp.models.Intervention;

import java.util.List;

public interface InterventionRepository extends JpaRepository<Intervention, Long> {
    List<Intervention> findByMachineIdMachine(Long idMachine);
    List<Intervention> findByEstFermee(boolean estFermee);
    List<Intervention> findByArchivedFalse(); // Interventions non archivées

}