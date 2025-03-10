package privateApp.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import privateApp.models.Categorie;
import privateApp.models.Produit;
import privateApp.models.ProduitLog;
import privateApp.models.User;
import privateApp.repositories.CategorieRepository;
import privateApp.repositories.ProduitLogRepository;
import privateApp.repositories.ProduitRepository;
import privateApp.repositories.UserRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StockService {

    @Autowired
    private ProduitRepository produitRepository;

    @Autowired
    private CategorieRepository categorieRepository;

    @Autowired
    private ProduitLogRepository produitLogRepository;
    
 // Ajouter un nouveau produit : Crée un produit ou réactive un archivé, bloque les doublons actifs.
    public Produit addProduit(Produit produit) {
        // Vérifier les doublons pour un produit actif
        Optional<Produit> existingProduit = produitRepository.findByNomAndCategorieIdCategorie(
            produit.getNom(), produit.getCategorie().getIdCategorie()
        );

        if (existingProduit.isPresent()) {
            Produit archivedProduit = existingProduit.get();
            if (archivedProduit.isArchive()) {
                // Réactiver un produit archivé
                archivedProduit.setArchive(false);
                archivedProduit.setDescription(produit.getDescription());
                archivedProduit.setQteDisponible(produit.getQteDisponible());
                archivedProduit.setSeuilAlerte(produit.getSeuilAlerte());
                archivedProduit.setDateExpiration(produit.getDateExpiration());
                Produit updatedProduit = produitRepository.save(archivedProduit);
                logAction(updatedProduit, "UNARCHIVE", "Produit désarchivé et mis à jour");
                return updatedProduit;
            } else {
            	throw new IllegalArgumentException("Un produit actif avec ce nom existe déjà. Veuillez modifier le produit existant.");            }
        }

        // Créer un nouveau produit
        Produit savedProduit = produitRepository.save(produit);
        logAction(savedProduit, "CREATE", "Produit créé - Quantité: " + savedProduit.getQteDisponible());
        return savedProduit;
    }

    // Mettre à jour un produit existant : Modifie un produit existant sans se soucier des doublons.
    public Produit updateProduit(Produit produit) {
        Produit existingProduit = produitRepository.findById(produit.getIdProduit())
            .orElseThrow(() -> new RuntimeException("Produit non trouvé pour mise à jour"));

        // Mettre à jour les champs
        existingProduit.setNom(produit.getNom());
        existingProduit.setDescription(produit.getDescription());
        existingProduit.setQteDisponible(produit.getQteDisponible());
        existingProduit.setSeuilAlerte(produit.getSeuilAlerte());
        existingProduit.setDateExpiration(produit.getDateExpiration());
        existingProduit.setCategorie(produit.getCategorie());

        Produit updatedProduit = produitRepository.save(existingProduit);
        logAction(updatedProduit, "UPDATE", "Produit mis à jour - Quantité: " + updatedProduit.getQteDisponible());
        return updatedProduit;
    }
 // Modifier getProduits pour ne retourner que les produits non archivés
    public List<Produit> getProduits() {
        return produitRepository.findByArchiveFalse();
    }

    public List<Produit> getAllProduits() { // pour l’intendant , doit voir tous les produits (archivés ou non)
        return produitRepository.findAll();
    }
    public Produit getProduitById(Long produitId) {
        return produitRepository.findById(produitId)
            .orElseThrow(() -> new RuntimeException("Produit non trouvé"));
    }
    
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

    public void definirSeuilsCategorie(int idCategorie, int nombreMalades) { // Retiré userId
        List<Produit> produits = produitRepository.findByCategorieIdCategorie(idCategorie);
        for (Produit produit : produits) {
            if (idCategorie == 1) { // Matériel
                produit.setSeuilAlerte(calculerSeuilMateriel(nombreMalades));
            } else if (idCategorie == 2) { // Médicament
                produit.setSeuilAlerte(calculerSeuilMedicament());
            }
            produitRepository.save(produit);
        }
    }

 // Méthode pour vérifier les alertes (quantité et expiration)
    public List<Produit> verifierAlertes() {
        List<Produit> produitsActifs = produitRepository.findByArchiveFalse();
        Date today = new Date();

        return produitsActifs.stream()
            .filter(produit -> {
                // Condition pour les matériels (idCategorie = 1)
                if (produit.getCategorie().getIdCategorie() == 1) {
                    return produit.getQteDisponible() <= produit.getSeuilAlerte();
                }
                // Condition pour les médicaments (idCategorie = 2)
                else if (produit.getCategorie().getIdCategorie() == 2) {
                    boolean quantiteBasse = produit.getQteDisponible() <= produit.getSeuilAlerte();
                 // Vérifie si le produit est expiré (date d’expiration avant aujourd’hui)
                    boolean expire = produit.getDateExpiration() != null && produit.getDateExpiration().before(today);
                    return quantiteBasse || expire; // On retire quantiteEgaleUn car inclus dans quantiteBasse si seuil = 1
                }
                return false; // Par défaut, pas d’alerte
            })
            .collect(Collectors.toList());
    }

    public void deleteProduit(Long produitId) {
        Produit produit = produitRepository.findById(produitId)
            .orElseThrow(() -> new RuntimeException("Produit non trouvé"));

        // Marquer le produit comme archivé au lieu de le supprimer
        produit.setArchive(true);
        produitRepository.save(produit);

        // Enregistrer l’action dans produit_log
        logAction(produit, "ARCHIVE", "Produit archivé - Quantité: " + produit.getQteDisponible());
    }
 // Méthode pour enregistrer un log
    private void logAction(Produit produit, String action, String details) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ProduitLog log = new ProduitLog();
        log.setUser(user);
        log.setProduit(produit);
        log.setAction(action);
        log.setDetails(details);
        produitLogRepository.save(log);
    }
    // Méthodes de calcul des seuils inchangées
    public int calculerSeuilMateriel(int nombreMalades) {
        int seancesParMois = 13;
        int moisCommande = 3;
        int consommationMensuelle = nombreMalades * seancesParMois;
        int seuilCommande = consommationMensuelle * moisCommande + nombreMalades;
        return consommationMensuelle / 2; // Seuil alerte
    }

    public int calculerSeuilMedicament() {
        return 1;
    }
}