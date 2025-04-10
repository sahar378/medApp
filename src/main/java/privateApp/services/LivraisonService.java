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

import java.util.Date;
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
    
 // Nouvelle méthode pour les 7 dernières livraisons
    public List<Livraison> getLastSevenLivraisons() {
        List<Livraison> allLivraisons = livraisonRepository.findAllByOrderByIdLivraisonDesc();
        logger.info("Récupération des 7 dernières livraisons parmi {}", allLivraisons.size());
        return allLivraisons.stream()
                .limit(7)
                .collect(Collectors.toList());
    }
 // Nouvelle méthode : Livraisons par fournisseur
    public List<Livraison> getLivraisonsByFournisseur(Long idFournisseur) {
        List<Livraison> livraisons = livraisonRepository.findByFournisseurIdFournisseurOrderByIdLivraisonDesc(idFournisseur);
        logger.info("Récupération de {} livraisons pour le fournisseur ID {}", livraisons.size(), idFournisseur);
        return livraisons;
    }

    // Nouvelle méthode : Livraisons par date
    public List<Livraison> getLivraisonsByDate(Date date) {
        List<Livraison> livraisons = livraisonRepository.findByDateOrderByIdLivraisonDesc(date);
        logger.info("Récupération de {} livraisons pour la date {}", livraisons.size(), date);
        return livraisons;
    }

    // Nouvelle méthode : Livraisons par fournisseur et date
    public List<Livraison> getLivraisonsByFournisseurAndDate(Long idFournisseur, Date date) {
        List<Livraison> livraisons = livraisonRepository.findByFournisseurIdFournisseurAndDateOrderByIdLivraisonDesc(idFournisseur, date);
        logger.info("Récupération de {} livraisons pour le fournisseur ID {} et la date {}", livraisons.size(), idFournisseur, date);
        return livraisons;
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