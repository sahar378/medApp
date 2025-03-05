package privateApp.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import privateApp.models.Categorie;
import privateApp.models.Produit;
import privateApp.models.User;
import privateApp.repositories.CategorieRepository;
import privateApp.repositories.ProduitRepository;
import privateApp.repositories.UserRepository;

import java.util.List;

@Service
public class StockService {

    @Autowired
    private ProduitRepository produitRepository;

    @Autowired
    private CategorieRepository categorieRepository;

    @Autowired
    private UserRepository userRepository;

    // Ajouter ou mettre à jour un produit
    public Produit saveProduit(Produit produit, Long userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        produit.setUser(user);
        return produitRepository.save(produit);
    }

    // Récupérer les produits d’un responsable
    public List<Produit> getProduitsByUser(Long userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return produitRepository.findByUser(user);
    }

    // Récupérer tous les produits (pour l’intendant)
    public List<Produit> getAllProduits() {
        return produitRepository.findAll();
    }

    // Calculer dynamiquement le seuil alerte pour un matériel
    public int calculerSeuilMateriel(int nombreMalades) {
        int seancesParMois = 13;
        int moisCommande = 3;
        int consommationMensuelle = nombreMalades * seancesParMois;
        int seuilCommande = consommationMensuelle * moisCommande + nombreMalades;
        int seuilAlerte = consommationMensuelle / 2;
        return seuilAlerte;
    }

    // Seuil statique pour les médicaments
    public int calculerSeuilMedicament() {
        return 1;
    }

    // Définir ou recalculer le seuil alerte pour un produit
    public void definirSeuilAlerte(Long produitId, int nombreMalades) {
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé"));
        if ("medicament".equalsIgnoreCase(produit.getCategorie().getLibelleCategorie())) {
            produit.setSeuilAlerte(calculerSeuilMedicament());
        } else if ("materiel".equalsIgnoreCase(produit.getCategorie().getLibelleCategorie())) {
            produit.setSeuilAlerte(calculerSeuilMateriel(nombreMalades));
        }
        produitRepository.save(produit);
    }

    // Vérifier les alertes pour un responsable
    public List<Produit> verifierAlertes(Long userId) {
        List<Produit> produits = getProduitsByUser(userId);
        return produits.stream()
                .filter(produit -> produit.getQteDisponible() <= produit.getSeuilAlerte())
                .toList();
    }

    // Supprimer un produit
    public void deleteProduit(Long produitId, Long userId) {
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé"));
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        if (!produit.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Vous n’êtes pas autorisé à supprimer ce produit");
        }
        produitRepository.delete(produit);
    }
}