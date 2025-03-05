package privateApp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import privateApp.models.Produit;
import privateApp.models.User;

import java.util.List;

public interface ProduitRepository extends JpaRepository<Produit, Long> {
    List<Produit> findByUser(User user); // Récupérer les produits gérés par un responsable
}