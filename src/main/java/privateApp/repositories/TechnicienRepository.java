package privateApp.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import privateApp.models.Technicien;

public interface TechnicienRepository extends JpaRepository<Technicien, Long> {
	// privateApp.repositories/TechnicienRepository.java
	List<Technicien> findByArchivedFalse();
	List<Technicien> findByArchivedTrue();
	Optional<Technicien> findByEmail(String email);
}