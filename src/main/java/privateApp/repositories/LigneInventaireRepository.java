package privateApp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import privateApp.models.LigneInventaire;

public interface LigneInventaireRepository extends JpaRepository<LigneInventaire, Long> {
}