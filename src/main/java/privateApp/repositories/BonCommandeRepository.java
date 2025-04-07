package privateApp.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import privateApp.models.BonCommande;

public interface BonCommandeRepository extends JpaRepository<BonCommande, Long> {
	List<BonCommande> findByEtatNot(String etat);
    List<BonCommande> findByEtat(String etat);
    List<BonCommande> findByEtatIn(List<String> etats);

}
