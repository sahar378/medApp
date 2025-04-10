package privateApp.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import privateApp.models.Inventaire;
import privateApp.models.LigneInventaire;
import privateApp.models.Produit;
import privateApp.models.User;
import privateApp.repositories.InventaireRepository;
import privateApp.repositories.LigneInventaireRepository;
import privateApp.repositories.ProduitRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class InventaireService {

    @Autowired
    private InventaireRepository inventaireRepository;

    @Autowired
    private LigneInventaireRepository ligneInventaireRepository;

    @Autowired
    private ProduitRepository produitRepository;

    @Transactional
    public Inventaire verifierEtCreerInventaire(List<LigneInventaire> lignes) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Inventaire inventaire = new Inventaire();
        inventaire.setUser(user);
        inventaire.setDate(new Date());
        
        boolean hasDifferences = false;
        List<LigneInventaire> lignesInventaire = new ArrayList<>();

        for (LigneInventaire ligne : lignes) {
            Produit produit = produitRepository.findById(ligne.getProduit().getIdProduit())
                    .orElseThrow(() -> new RuntimeException("Produit non trouvé"));
            int qteStock = produit.getQteDisponible();
            int qteSaisie = ligne.getQteSaisie();

            if (qteSaisie == qteStock) {
                ligne.setObservationProduit("Quantité correcte");
            } else {
                hasDifferences = true;
                ligne.setObservationProduit(String.format("Différence détectée : Stock = %d, Saisie = %d", qteStock, qteSaisie));
            }
            ligne.setInventaire(inventaire);
            lignesInventaire.add(ligne);
        }

        inventaire.setLignesInventaire(lignesInventaire);
        inventaire.setEtat(!hasDifferences);
        inventaire.setObservationInventaire(
            hasDifferences 
                ? "Attention : Des écarts ont été détectés dans le stock lors de cet inventaire." 
                : "Stock conforme : Aucune différence notable détectée."
        );

        return inventaireRepository.save(inventaire);
    }
    
    public List<Inventaire> getAllInventaires() {
        return inventaireRepository.findAll();
    }

    public Inventaire getInventaireById(Long id) {
        return inventaireRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventaire non trouvé"));
    }
 // Nouvelle méthode pour récupérer les 4 derniers inventaires
    public List<Inventaire> getLastFourInventaires() {
        // Tri par date décroissante, limité à 4 éléments
        return inventaireRepository.findAll(PageRequest.of(0, 4, Sort.by("date").descending()))
                .getContent();
    }
}