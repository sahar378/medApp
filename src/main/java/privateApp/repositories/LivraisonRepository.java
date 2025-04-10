package privateApp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import privateApp.models.BonCommande;
import privateApp.models.Livraison;
import privateApp.models.Produit;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface LivraisonRepository extends JpaRepository<Livraison, Long> {
    // Récupérer toutes les livraisons pour un produit donné

	List<Livraison> findByProduitIdProduit(Long idProduit);

	// Toutes les livraisons triées par idLivraison décroissant
    List<Livraison> findAllByOrderByIdLivraisonDesc();

    // Livraisons par fournisseur triées par idLivraison décroissant
    List<Livraison> findByFournisseurIdFournisseurOrderByIdLivraisonDesc(Long idFournisseur);

    // Livraisons par date triées par idLivraison décroissant
    List<Livraison> findByDateOrderByIdLivraisonDesc(Date date);

    // Livraisons par fournisseur et date triées par idLivraison décroissant
    List<Livraison> findByFournisseurIdFournisseurAndDateOrderByIdLivraisonDesc(Long idFournisseur, Date date);

	}