package privateApp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import privateApp.models.Prix;

import java.util.List;
import java.util.Optional;

public interface PrixRepository extends JpaRepository<Prix, Long> {
    Optional<Prix> findByProduitIdProduitAndFournisseurIdFournisseurAndStatut(Long idProduit, Long idFournisseur, int statut);
    List<Prix> findByStatutAndProduitCategorieIdCategorie(int statut, Long idCategorie);
	//Optional<Prix> findByProduitIdProduitAndStatut(Long idProduit, int i);
	List<Prix> findByProduitIdProduitAndStatut(Long idProduit, int statut);
}