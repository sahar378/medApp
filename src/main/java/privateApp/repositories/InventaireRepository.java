package privateApp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import privateApp.models.Inventaire;

public interface InventaireRepository extends JpaRepository<Inventaire, Long> {
}