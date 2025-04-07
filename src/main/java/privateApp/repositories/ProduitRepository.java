package privateApp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import privateApp.models.Produit;
import privateApp.models.User;

import java.util.List;
import java.util.Optional;

public interface ProduitRepository extends JpaRepository<Produit, Long> {
	List<Produit> findByCategorieIdCategorie(int idCategorie); // pour filtrer par catégorie
	List<Produit> findByArchiveFalse(); // pour retourner seulement les produits non archivés
	List<Produit> findByArchiveTrue();
	Optional<Produit> findByNomAndCategorieIdCategorie(String nom, Long idCategorie);
	// Nouvelles méthodes pour filtrer par archive et catégorie
    List<Produit> findByArchiveFalseAndCategorieIdCategorie(Long idCategorie);
    List<Produit> findByArchiveTrueAndCategorieIdCategorie(Long idCategorie);
	}