package privateApp.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import privateApp.models.Technicien;

public interface TechnicienRepository extends JpaRepository<Technicien, Long> {
	// privateApp.repositories/TechnicienRepository.java
	List<Technicien> findByArchivedFalse();
	List<Technicien> findByArchivedTrue();
}