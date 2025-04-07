// src/main/java/privateApp/services/FournisseurService.java
package privateApp.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import privateApp.models.Fournisseur;
import privateApp.models.Produit;
import privateApp.repositories.FournisseurRepository;
import privateApp.repositories.ProduitRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FournisseurService {

    @Autowired
    private FournisseurRepository fournisseurRepository;

    @Autowired
    private ProduitRepository produitRepository;

    // Récupérer tous les fournisseurs selon leur statut
    public List<Fournisseur> getFournisseursByStatut(int statut) {
        return fournisseurRepository.findByStatut(statut);
    }

    // Récupérer un fournisseur par ID
    public Fournisseur getFournisseurById(Long id) {
        return fournisseurRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé"));
    }
/*
    // Récupérer les fournisseurs associés à un produit (actif ou inactif uniquement)
    public List<Fournisseur> getFournisseursByProduit(Long produitId) {
        Produit produit = produitRepository.findById(produitId)
            .orElseThrow(() -> new RuntimeException("Produit non trouvé"));
        return produit.getFournisseurs().stream()
            .filter(f -> f.getStatut() != 2) // Exclure les fournisseurs erased
            .collect(Collectors.toList());
    }
    */
    
 //Récupère tous les fournisseurs associés à un produit, sauf les erased (statut = 2).
    public List<Fournisseur> getFournisseursByProduit(Long produitId) {
        Produit produit = produitRepository.findById(produitId)
            .orElseThrow(() -> new RuntimeException("Produit non trouvé"));
        return produit.getFournisseurs().stream()
            .filter(f -> f.getStatut() != 2) // Exclure uniquement les fournisseurs erased
            .collect(Collectors.toList());
    }
    
   /* // Créer un nouveau fournisseur
    public Fournisseur createFournisseur(Fournisseur fournisseur) {
        fournisseur.setStatut(0); // Actif par défaut
        return fournisseurRepository.save(fournisseur);
    }*/
 // Méthode pour vérifier si un fournisseur existe déjà (par nom ou email, par exemple)
    public Fournisseur findExistingFournisseur(String nom, String email) {
        return fournisseurRepository.findByNomOrEmail(nom, email).orElse(null);
    }

    // Créer un nouveau fournisseur avec vérification d’existence
    public Fournisseur createFournisseur(Fournisseur fournisseur) {
        Fournisseur existingFournisseur = findExistingFournisseur(fournisseur.getNom(), fournisseur.getEmail());
        if (existingFournisseur != null) {
            // Retourner le fournisseur existant avec son statut pour indiquer qu’il existe déjà
            return existingFournisseur;
        }
        fournisseur.setStatut(0); // Actif par défaut
        return fournisseurRepository.save(fournisseur);
    }

    // Mettre à jour un fournisseur
    public Fournisseur updateFournisseur(Long id, Fournisseur fournisseurDetails) {
        Fournisseur fournisseur = getFournisseurById(id);
        fournisseur.setNom(fournisseurDetails.getNom());
        fournisseur.setEmail(fournisseurDetails.getEmail());
        fournisseur.setAdresse(fournisseurDetails.getAdresse());
        fournisseur.setTelephone(fournisseurDetails.getTelephone());
        fournisseur.setFax(fournisseurDetails.getFax());
        fournisseur.setMatriculeFiscale(fournisseurDetails.getMatriculeFiscale());
        fournisseur.setRib(fournisseurDetails.getRib());
        fournisseur.setRc(fournisseurDetails.getRc());
        fournisseur.setCodeTva(fournisseurDetails.getCodeTva());
        // Ne pas modifier le statut ou causeSuppression ici, utiliser des méthodes dédiées
        return fournisseurRepository.save(fournisseur);
    }

    // Changer le statut d’un fournisseur
    @Transactional
    public Fournisseur changerStatut(Long id, int nouveauStatut, String causeSuppression) {
        Fournisseur fournisseur = getFournisseurById(id);
        if (nouveauStatut < 0 || nouveauStatut > 2) {
            throw new IllegalArgumentException("Statut invalide. Utilisez 0 (actif), 1 (inactif) ou 2 (erased).");
        }
        if (nouveauStatut == 2 && (causeSuppression == null || causeSuppression.trim().isEmpty())) {
            throw new IllegalArgumentException("Une cause de suppression est requise pour passer au statut erased.");
        }
        if (fournisseur.getStatut() == 2 && nouveauStatut != 2) {
            throw new IllegalStateException("Un fournisseur erased ne peut pas être réactivé.");
        }
        fournisseur.setStatut(nouveauStatut);
        if (nouveauStatut == 2) {
            fournisseur.setCauseSuppression(causeSuppression);
        } else {
            fournisseur.setCauseSuppression(null); // Réinitialiser si pas erased
        }
        return fournisseurRepository.save(fournisseur);
    }

    // Associer un produit à un fournisseur
    @Transactional
    public Fournisseur associerProduit(Long fournisseurId, Long produitId) {
        Fournisseur fournisseur = getFournisseurById(fournisseurId);
        if (fournisseur.getStatut() == 2) {
            throw new IllegalStateException("Impossible d’associer un produit à un fournisseur erased.");
        }
        Produit produit = produitRepository.findById(produitId)
            .orElseThrow(() -> new RuntimeException("Produit non trouvé"));

        List<Produit> produits = fournisseur.getProduits();
        if (produits == null) {
            produits = new ArrayList<>();
            fournisseur.setProduits(produits);
        }
        if (!produits.contains(produit)) {
            produits.add(produit);
            fournisseur = fournisseurRepository.save(fournisseur);
        }
        return fournisseur;
    }

    // Associer plusieurs produits à un fournisseur
    @Transactional
    public Fournisseur associerProduits(Long fournisseurId, List<Long> produitIds) {
        Fournisseur fournisseur = getFournisseurById(fournisseurId);
        if (fournisseur.getStatut() == 2) {
            throw new IllegalStateException("Impossible d’associer des produits à un fournisseur erased.");
        }
        List<Produit> produitsToAdd = produitRepository.findAllById(produitIds);
        if (produitsToAdd.size() != produitIds.size()) {
            throw new RuntimeException("Certains produits n’ont pas été trouvés.");
        }

        List<Produit> produits = fournisseur.getProduits();
        if (produits == null) {
            produits = new ArrayList<>();
            fournisseur.setProduits(produits);
        }
        for (Produit produit : produitsToAdd) {
            if (!produits.contains(produit)) {
                produits.add(produit);
            }
        }
        return fournisseurRepository.save(fournisseur);
    }

    // Dissocier un produit d’un fournisseur
    @Transactional
    public Fournisseur dissocierProduit(Long fournisseurId, Long produitId) {
        Fournisseur fournisseur = getFournisseurById(fournisseurId);
        Produit produit = produitRepository.findById(produitId)
            .orElseThrow(() -> new RuntimeException("Produit non trouvé"));

        List<Produit> produits = fournisseur.getProduits();
        if (produits != null && produits.contains(produit)) {
            produits.remove(produit);
            fournisseur.setProduits(produits);
            return fournisseurRepository.save(fournisseur);
        }
        return fournisseur;
    }

    // Dissocier plusieurs produits
    @Transactional
    public Fournisseur dissocierProduits(Long fournisseurId, List<Long> produitIds) {
        Fournisseur fournisseur = getFournisseurById(fournisseurId);
        List<Produit> produitsToRemove = produitRepository.findAllById(produitIds);
        if (produitsToRemove.size() != produitIds.size()) {
            throw new RuntimeException("Certains produits n’ont pas été trouvés.");
        }

        List<Produit> produits = fournisseur.getProduits();
        if (produits != null && !produits.isEmpty()) {
            produits.removeAll(produitsToRemove);
            fournisseur.setProduits(produits);
            return fournisseurRepository.save(fournisseur);
        }
        return fournisseur;
    }
}