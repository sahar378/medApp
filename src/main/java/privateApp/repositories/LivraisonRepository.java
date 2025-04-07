package privateApp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import privateApp.models.BonCommande;
import privateApp.models.Livraison;
import privateApp.models.Produit;

import java.util.List;
import java.util.Optional;

public interface LivraisonRepository extends JpaRepository<Livraison, Long> {
    // Récupérer toutes les livraisons pour un produit donné

	List<Livraison> findByProduitIdProduit(Long idProduit);

	}