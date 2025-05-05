package privateApp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import privateApp.models.Machine;
import privateApp.models.Seance;

import java.time.LocalDateTime;
import java.util.List;

public interface MachineRepository extends JpaRepository<Machine, Long> {
    List<Machine> findByDisponibilite(int disponibilite);// Machines par disponibilité
    List<Machine> findByArchivedFalse();// Machines non archivées
    List<Machine> findByArchivedTrue();// Machines archivées
    /**
     * Vérifie si une machine est utilisée dans une séance active à un moment donné.
     * Une séance est active si debutDialyse <= now <= finDialyse.
     */
    @Query("SELECT s FROM Seance s WHERE s.machine.idMachine = :machineId " +
           "AND s.debutDialyse <= :now AND (s.finDialyse IS NULL OR s.finDialyse >= :now)")
    List<Seance> findActiveSeanceByMachine(Long machineId, LocalDateTime now);
    
    /**
     * Trouve les machines avec disponibilité donnée et non archivées.
     * @param disponibilite Statut de disponibilité (0 = disponible).
     * @return Liste des machines correspondantes.
     */
    List<Machine> findByDisponibiliteAndArchivedFalse(int disponibilite);
}