package privateApp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import privateApp.models.EnvoiBonCommande;

public interface EnvoiBonCommandeRepository extends JpaRepository<EnvoiBonCommande, Long> {
}