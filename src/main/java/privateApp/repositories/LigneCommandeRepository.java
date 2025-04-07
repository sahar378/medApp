package privateApp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import privateApp.models.LigneCommande;

public interface LigneCommandeRepository extends JpaRepository<LigneCommande, Long> {

}
