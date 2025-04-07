package privateApp.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import privateApp.models.BonCommande;
import privateApp.models.Fournisseur;
import privateApp.models.Livraison;
import privateApp.models.Produit;
import privateApp.models.ProduitLog;
import privateApp.models.User;
import privateApp.repositories.BonCommandeRepository;
import privateApp.repositories.LivraisonRepository;
import privateApp.repositories.ProduitLogRepository;
import privateApp.repositories.ProduitRepository;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class LivraisonService {

    private static final Logger logger = LoggerFactory.getLogger(LivraisonService.class);

    @Autowired
    private LivraisonRepository livraisonRepository;

    @Autowired
    private ProduitRepository produitRepository;
    
    @Autowired
	private BonCommandeRepository bonCommandeRepository;
    
    @Autowired
	private ProduitLogRepository produitLogRepository;
    
 
    @Transactional
    public Livraison addLivraison(Livraison livraison) {
        Produit produit = livraison.getProduit();
        if (produit == null || produit.getIdProduit() == null) {
            throw new IllegalArgumentException("Produit non spécifié ou invalide.");
        }
        produit = produitRepository.findById(produit.getIdProduit())
                .orElseThrow(() -> new RuntimeException("Produit non trouvé pour la livraison"));

        Fournisseur fournisseur = livraison.getFournisseur();
        if (fournisseur == null || fournisseur.getIdFournisseur() == null) {
            throw new IllegalArgumentException("Fournisseur non spécifié ou invalide.");
        }

        // Vérification basée sur l'ID du fournisseur
        boolean fournisseurAssocie = produit.getFournisseurs().stream()
                .anyMatch(f -> f.getIdFournisseur().equals(fournisseur.getIdFournisseur()));
        if (!fournisseurAssocie) {
            throw new IllegalArgumentException("Ce fournisseur n'est pas associé à ce produit.");
        }

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        livraison.setUser(user);
        logger.info("Ajout d'une livraison par l'utilisateur : {}", user.getUsername());

        Livraison savedLivraison = livraisonRepository.save(livraison);

        int nouvelleQuantite = produit.getQteDisponible() + livraison.getQuantiteLivree();
        produit.setQteDisponible(nouvelleQuantite);
        produitRepository.save(produit);

        logger.info("Livraison enregistrée - ID: {}, Quantité ajoutée: {}", savedLivraison.getIdLivraison(), livraison.getQuantiteLivree());
        // Ajouter un log avec vérification
        String fournisseurNom = (fournisseur.getNom() != null) ? fournisseur.getNom() : "Fournisseur inconnu";
        logAction(produit, "LIVRAISON", "Quantité livrée: " + livraison.getQuantiteLivree() + " par " + fournisseurNom);
        return savedLivraison;
    }
    
    public List<Livraison> getAllLivraisons() {
        List<Livraison> livraisons = livraisonRepository.findAll();
        logger.info("Récupération de {} livraisons", livraisons.size());
        return livraisons;
    }

    public List<Livraison> getLivraisonsByProduit(Long idProduit) {
        return livraisonRepository.findByProduitIdProduit(idProduit);
    }
    private void logAction(Produit produit, String action, String details) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ProduitLog log = new ProduitLog();
        log.setUser(user);
        log.setProduit(produit);
        log.setAction(action);
        log.setDetails(details);
        produitLogRepository.save(log);
    }
}