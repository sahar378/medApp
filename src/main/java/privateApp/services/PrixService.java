package privateApp.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import privateApp.models.Prix;
import privateApp.models.Produit;
import privateApp.dtos.PrixDTO;
import privateApp.models.Fournisseur;
import privateApp.repositories.PrixRepository;
import privateApp.repositories.ProduitRepository;
import privateApp.repositories.FournisseurRepository;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PrixService {
	
	private static final Logger logger = LoggerFactory.getLogger(PrixService.class);
    @Autowired
    private PrixRepository prixRepository;

    @Autowired
    private ProduitRepository produitRepository;

    @Autowired
    private FournisseurRepository fournisseurRepository;

    // Récupérer tous les prix
    public List<Prix> getAllPrix() {
        return prixRepository.findAll();
    }

    // Récupérer un prix par ID
    public Prix getPrixById(Long id) {
        return prixRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prix non trouvé"));
    }

    // Créer un nouveau prix
    @Transactional
    public Prix createPrix(Prix prix) {
        Produit produit = produitRepository.findById(prix.getProduit().getIdProduit())
                .orElseThrow(() -> new RuntimeException("Produit non trouvé"));
        Fournisseur fournisseur = fournisseurRepository.findById(prix.getFournisseur().getIdFournisseur())
                .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé"));
        
        
        if (fournisseur.getStatut() == 2) {
            throw new IllegalStateException("Impossible d’ajouter un prix pour un fournisseur erased.");
        }
        
        

        // Vérifier si un prix actif existe déjà pour cette combinaison produit/fournisseur
        Optional<Prix> existingPrix = prixRepository.findByProduitIdProduitAndFournisseurIdFournisseurAndStatut(
                produit.getIdProduit(), fournisseur.getIdFournisseur(), 1);
        if (existingPrix.isPresent()) {
            throw new IllegalArgumentException("Un prix actif existe déjà pour ce produit et ce fournisseur.");
        }

        // Désactiver un ancien prix s’il existe (logique existante inchangée)
        prixRepository.findByProduitIdProduitAndFournisseurIdFournisseurAndStatut(
                produit.getIdProduit(), fournisseur.getIdFournisseur(), 1)
                .ifPresent(oldPrix -> {
                    oldPrix.setStatut(0);
                    prixRepository.save(oldPrix);
                });

        prix.setProduit(produit);
        prix.setFournisseur(fournisseur);
        prix.setDate(new Date());
        prix.setStatut(1);
        return prixRepository.save(prix);
    }

    // Mettre à jour un prix existant
    @Transactional
    public Prix updatePrix(Long id, Prix prixDetails) {
        Prix prix = getPrixById(id);
        prix.setPrixUnitaire(prixDetails.getPrixUnitaire());
        prix.setTauxTva(prixDetails.getTauxTva());
        prix.setStatut(prixDetails.getStatut());
        return prixRepository.save(prix);
    }

    // Supprimer un prix
    public void deletePrix(Long id) {
        Prix prix = getPrixById(id);
        prixRepository.delete(prix);
    }

    // Récupérer les prix actifs avec tri et filtrage
    public List<PrixDTO> getProduitsWithPrixActifs(String categorie, String nom, String sortBy, String sortOrder) {
/*        Long idCategorie = "medicaments".equalsIgnoreCase(categorie) ? 2L : 1L; // 1 = matériels, 2 = médicaments
        System.out.println("Catégorie reçue: " + categorie + " | idCategorie: " + idCategorie);

        List<Prix> prixList = prixRepository.findByStatutAndProduitCategorieIdCategorie(1, idCategorie);
        System.out.println("Nombre de prix trouvés: " + prixList.size());
        prixList.forEach(prix -> 
            System.out.println("Produit: " + prix.getProduit().getNom() + " | Catégorie: " + prix.getProduit().getCategorie().getLibelleCategorie())
        );

        // Filtrer par nom si fourni
        if (nom != null && !nom.isEmpty()) {
            prixList = prixList.stream()
                    .filter(p -> p.getProduit().getNom().toLowerCase().contains(nom.toLowerCase()))
                    .collect(Collectors.toList());
        }*/

    	
    	
    	Long idCategorie = "medicaments".equalsIgnoreCase(categorie) ? 2L : 1L;
        List<Prix> prixList = prixRepository.findByStatutAndProduitCategorieIdCategorie(1, idCategorie)
            .stream()
            .filter(p -> p.getFournisseur().getStatut() != 2) // Exclure les erased
            .collect(Collectors.toList());
        
        
        
        
        // Trier selon sortBy et sortOrder
        Comparator<Prix> comparator;
        if ("prixUnitaire".equals(sortBy)) {
            comparator = Comparator.comparingDouble(Prix::getPrixUnitaire);
        } else {
            comparator = Comparator.comparing(p -> p.getProduit().getNom());
        }
        if ("desc".equals(sortOrder)) {
            comparator = comparator.reversed();
        }
        prixList.sort(comparator);

        // Convertir en DTO
        return prixList.stream().map(p -> new PrixDTO(
                p.getProduit().getIdProduit(),
                p.getProduit().getNom(),
                p.getFournisseur().getIdFournisseur(),
                p.getFournisseur().getNom(),
                p.getPrixUnitaire(),
                p.getTauxTva(),
                p.getDate()
        )).collect(Collectors.toList());
    }
    
    //méthode pour récupérer les prix d’un produit spécifique
 // src/main/java/privateApp/services/PrixService.java
    public List<PrixDTO> getPrixByProduit(Long produitId, String sortBy, String sortOrder) {
        List<Prix> prixList = prixRepository.findByProduitIdProduitAndStatut(produitId, 1)
                .stream()
                .filter(p -> p.getFournisseur().getStatut() != 2) // Exclure les erased
                .collect(Collectors.toList());

        Comparator<Prix> comparator = "prixUnitaire".equals(sortBy) ?
                Comparator.comparingDouble(Prix::getPrixUnitaire) :
                Comparator.comparing(p -> p.getFournisseur().getNom());
        if ("desc".equals(sortOrder)) {
            comparator = comparator.reversed();
        }
        prixList.sort(comparator);

        return prixList.stream().map(p -> new PrixDTO(
                p.getIdPrix(), // Ajout de l'ID du prix
                p.getProduit().getIdProduit(),
                p.getProduit().getNom(),
                p.getFournisseur().getIdFournisseur(),
                p.getFournisseur().getNom(),
                p.getPrixUnitaire(),
                p.getTauxTva(),
                p.getDate()
        )).collect(Collectors.toList());
    }
    
  //retourne tous les produits actifs avec leur prix actif s’il existe, sinon avec des champs nuls.  
    public List<PrixDTO> getAllProduitsWithOptionalPrix(String categorie, String nom, String sortBy, String sortOrder) {
        Long idCategorie = "medicaments".equalsIgnoreCase(categorie) ? 2L : 1L;
        List<Produit> produits = produitRepository.findByArchiveFalseAndCategorieIdCategorie(idCategorie);

        // Filtrer par nom si fourni
        if (nom != null && !nom.isEmpty()) {
            produits = produits.stream()
                .filter(p -> p.getNom().toLowerCase().contains(nom.toLowerCase()))
                .collect(Collectors.toList());
        }

        // Récupérer tous les prix actifs pour chaque produit
       /* return produits.stream()
            .flatMap(p -> {
                List<Prix> prixActifs = prixRepository.findByProduitIdProduitAndStatut(p.getIdProduit(), 1);*/
        
        
        return produits.stream()
                .flatMap(p -> {
                    List<Prix> prixActifs = prixRepository.findByProduitIdProduitAndStatut(p.getIdProduit(), 1)
                        .stream()
                        .filter(pr -> pr.getFournisseur().getStatut() != 2)
                        .collect(Collectors.toList());
                    
                    
                    
                if (prixActifs.isEmpty()) {
                    return Stream.of(new PrixDTO(
                        p.getIdProduit(),
                        p.getNom(),
                        null,
                        null,
                        0.0,
                        0.0,
                        null
                    ));
                }
                return prixActifs.stream().map(prix -> new PrixDTO(
                    p.getIdProduit(),
                    p.getNom(),
                    prix.getFournisseur().getIdFournisseur(),
                    prix.getFournisseur().getNom(),
                    prix.getPrixUnitaire(),
                    prix.getTauxTva(),
                    prix.getDate()
                ));
            })
            .sorted((p1, p2) -> {
                if ("prixUnitaire".equals(sortBy)) {
                    int cmp = Double.compare(p1.getPrixUnitaire(), p2.getPrixUnitaire());
                    return "desc".equals(sortOrder) ? -cmp : cmp;
                } else {
                    int cmp = p1.getNomProduit().compareTo(p2.getNomProduit());
                    return "desc".equals(sortOrder) ? -cmp : cmp;
                }
            })
            .collect(Collectors.toList());
    }
    
    
    
 // filtre les fournisseurs associés à un produit pour ne retourner que ceux ayant un prix actif (statut = 1) et non erased.
    public List<Fournisseur> getFournisseursWithPrixActifByProduit(Long produitId) {
        logger.info("Récupération des fournisseurs pour produitId: {}", produitId);
        Produit produit = produitRepository.findById(produitId)
            .orElseThrow(() -> new RuntimeException("Produit non trouvé"));
        List<Fournisseur> result = produit.getFournisseurs().stream()
            .filter(f -> f.getStatut() != 2)
            .filter(f -> prixRepository.findByProduitIdProduitAndFournisseurIdFournisseurAndStatut(
                produitId, f.getIdFournisseur(), 1).isPresent())
            .collect(Collectors.toList());
        logger.info("Fournisseurs trouvés: {}", result.size());
        return result;
    }
}