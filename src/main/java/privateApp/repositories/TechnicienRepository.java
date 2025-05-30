package privateApp.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import privateApp.models.Technicien;

public interface TechnicienRepository extends JpaRepository<Technicien, Long> {
    List<Technicien> findByArchivedFalse();
    List<Technicien> findByArchivedTrue();
    Optional<Technicien> findByEmail(String email);

    // Nouvelle méthode pour la recherche
    @Query("SELECT t FROM Technicien t WHERE t.archived = :archived AND (LOWER(t.nom) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
    	       "OR LOWER(t.prenom) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
    	       "OR LOWER(t.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    	List<Technicien> searchTechniciens(@Param("searchTerm") String searchTerm, @Param("archived") boolean archived);
}