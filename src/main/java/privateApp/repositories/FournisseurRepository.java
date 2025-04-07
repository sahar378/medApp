package privateApp.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import privateApp.models.Fournisseur;

public interface FournisseurRepository extends JpaRepository<Fournisseur, Long> {
	List<Fournisseur> findByStatut(int statut);
	Optional<Fournisseur> findByNomOrEmail(String nom, String email);

}
