package privateApp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import privateApp.models.Machine;

import java.util.List;

public interface MachineRepository extends JpaRepository<Machine, Long> {
    List<Machine> findByDisponibilite(int disponibilite);
 // privateApp.repositories/MachineRepository.java
    List<Machine> findByArchivedFalse();
    List<Machine> findByArchivedTrue();
}