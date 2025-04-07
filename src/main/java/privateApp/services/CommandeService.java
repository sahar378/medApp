package privateApp.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSendException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import privateApp.dtos.*;
import privateApp.models.*;
import privateApp.repositories.*;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommandeService {
    private static final Logger logger = LoggerFactory.getLogger(CommandeService.class);

    @Autowired
    private BonCommandeRepository bonCommandeRepository;

    @Autowired
    private LigneCommandeRepository ligneCommandeRepository;

    @Autowired
    private ProduitRepository produitRepository;

    @Autowired
    private FournisseurRepository fournisseurRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PrixRepository prixRepository;

    
    // Méthode utilitaire pour interpréter le prix unitaire
    private double interpreterPrixUnitaire(double prixUnitaireSaisi) {
        // Si le prix est un entier (ex: 12), on le traite comme 12,000 TND
        if (prixUnitaireSaisi >= 1 && prixUnitaireSaisi == Math.floor(prixUnitaireSaisi)) {
            return prixUnitaireSaisi; // 12 devient 12,000 (interprété comme 12 dinars)
        }
        // Sinon, on conserve la valeur saisie telle quelle (ex: 0,655 ou 320,100)
        return prixUnitaireSaisi;
    }

    // Méthode pour calculer les montants et renvoyer un DTO
    private LigneCommandeAvecCalculsDTO calculerMontantsLigneCommande(LigneCommande ligne) {
        LigneCommandeAvecCalculsDTO dto = new LigneCommandeAvecCalculsDTO(ligne);
        double prixUnitaire = interpreterPrixUnitaire(ligne.getPrix().getPrixUnitaire());
        int quantite = ligne.getQuantite();
        double tauxTva = ligne.getPrix().getTauxTva(); // En pourcentage (ex: 20)

        double sousTotal = prixUnitaire * quantite;
        double montantTva = (sousTotal * tauxTva) / 100;
        double total = sousTotal + montantTva;

        dto.setSousTotal(sousTotal);
        dto.setMontantTva(montantTva);
        dto.setTotal(total);

        return dto;
    }

    // Créer un bon de commande (brouillon)
/*Vérifie que le fournisseur est actif (statut = 0) avant de créer un bon de commande.
Vérifie qu'un prix actif existe pour chaque produit et fournisseur sélectionné, sinon une exception est levée avec un message clair (ex. : "Prix actif non trouvé pour le produit X et le fournisseur Y").
Crée un bon de commande uniquement avec des fournisseurs actifs et des prix valides.*/
    @Transactional
    public BonCommandeDTO creerBonCommande(BonCommandeDTO bonCommandeDTO) {
        Fournisseur fournisseur = fournisseurRepository.findById(bonCommandeDTO.getIdFournisseur())
                .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé"));
        
        if (fournisseur.getStatut() != 0) {
            throw new IllegalArgumentException("Le fournisseur '" + fournisseur.getNom() + "' est inactif ou supprimé et ne peut pas être utilisé dans un bon de commande.");
        }

        User createdBy = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        BonCommande bonCommande = new BonCommande();
        Date creationDate = new Date();
        bonCommande.setDate(creationDate);
        bonCommande.setEtat("brouillon");
        bonCommande.setFournisseur(fournisseur);
        bonCommande.setCreatedBy(createdBy);

        List<LigneCommande> lignesCommande = bonCommandeDTO.getLignesCommande().stream().map(dto -> {
            Produit produit = produitRepository.findById(dto.getIdProduit())
                    .orElseThrow(() -> new RuntimeException("Produit non trouvé"));
            
            // Vérifier qu'un prix actif existe pour ce produit et fournisseur
            Prix prix = prixRepository.findByProduitIdProduitAndFournisseurIdFournisseurAndStatut(
                    dto.getIdProduit(), fournisseur.getIdFournisseur(), 1)
                    .orElseThrow(() -> new RuntimeException(
                        "Prix actif non trouvé pour le produit '" + produit.getNom() + 
                        "' et le fournisseur '" + fournisseur.getNom() + "'"));
                        
            LigneCommande ligne = new LigneCommande();
            ligne.setProduit(produit);
            ligne.setQuantite(dto.getQuantite());
            ligne.setBonCommande(bonCommande);
            ligne.setPrix(prix);
            ligne.setFournisseur(fournisseur);
            return ligne;
        }).collect(Collectors.toList());

        bonCommande.setLignesCommande(lignesCommande);
        BonCommande savedBon = bonCommandeRepository.save(bonCommande);

        // Préparer la réponse avec les calculs (inchangé)
        BonCommandeDTO responseDTO = new BonCommandeDTO();
        responseDTO.setIdFournisseur(savedBon.getFournisseur().getIdFournisseur());
        List<LigneCommandeDTO> lignesDTO = savedBon.getLignesCommande().stream()
                .map(ligne -> {
                    LigneCommandeAvecCalculsDTO calculsDTO = calculerMontantsLigneCommande(ligne);
                    LigneCommandeDTO ligneDTO = new LigneCommandeDTO();
                    ligneDTO.setIdProduit(calculsDTO.getIdProduit());
                    ligneDTO.setQuantite(calculsDTO.getQuantite());
                    return ligneDTO;
                })
                .collect(Collectors.toList());
        responseDTO.setLignesCommande(lignesDTO);
        responseDTO.setLignesAvecCalculs(savedBon.getLignesCommande().stream()
                .map(this::calculerMontantsLigneCommande)
                .collect(Collectors.toList()));

        return responseDTO;
    }
    
    
    // Créer plusieurs bons de commande regroupés par fournisseur
    @Transactional
    public List<BonCommandeDTO> creerBonsCommandeParFournisseur(List<BonCommandeDTO> bonsCommandeDTO) {
        User createdBy = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<BonCommandeDTO> responseDTOs = new ArrayList<>();

        for (BonCommandeDTO dto : bonsCommandeDTO) {
            Fournisseur fournisseur = fournisseurRepository.findById(dto.getIdFournisseur())
                    .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé"));
            
            
            if (fournisseur.getStatut() != 0) {
            	throw new IllegalArgumentException("Le fournisseur '" + fournisseur.getNom() + "' est inactif ou supprimé et ne peut pas être utilisé dans un bon de commande.");
            }
            
            

            BonCommande bonCommande = new BonCommande();
            Date creationDate = new Date();
            bonCommande.setDate(creationDate);
            bonCommande.setEtat("brouillon");
            bonCommande.setFournisseur(fournisseur);
            bonCommande.setCreatedBy(createdBy);

            List<LigneCommande> lignesCommande = dto.getLignesCommande().stream().map(ligneDTO -> {
                Produit produit = produitRepository.findById(ligneDTO.getIdProduit())
                        .orElseThrow(() -> new RuntimeException("Produit non trouvé"));
                Prix prix = prixRepository.findByProduitIdProduitAndFournisseurIdFournisseurAndStatut(ligneDTO.getIdProduit(), fournisseur.getIdFournisseur(), 1)
                        .orElseThrow(() -> new RuntimeException("Prix actif non trouvé pour ce produit et fournisseur"));
                LigneCommande ligne = new LigneCommande();
                ligne.setProduit(produit);
                ligne.setQuantite(ligneDTO.getQuantite());
                ligne.setBonCommande(bonCommande);
                ligne.setPrix(prix);
                ligne.setFournisseur(fournisseur);
                return ligne;
            }).collect(Collectors.toList());

            bonCommande.setLignesCommande(lignesCommande);
            BonCommande savedBon = bonCommandeRepository.save(bonCommande);

            // Préparer la réponse avec les calculs
            BonCommandeDTO responseDTO = new BonCommandeDTO();
            responseDTO.setIdFournisseur(savedBon.getFournisseur().getIdFournisseur());
            List<LigneCommandeDTO> lignesDTO = savedBon.getLignesCommande().stream()
                    .map(ligne -> {
                        LigneCommandeDTO ligneDTO = new LigneCommandeDTO();
                        ligneDTO.setIdProduit(ligne.getProduit().getIdProduit());
                        ligneDTO.setQuantite(ligne.getQuantite());
                        return ligneDTO;
                    })
                    .collect(Collectors.toList());
            responseDTO.setLignesCommande(lignesDTO);
            responseDTO.setLignesAvecCalculs(savedBon.getLignesCommande().stream()
                    .map(this::calculerMontantsLigneCommande)
                    .collect(Collectors.toList()));
            responseDTOs.add(responseDTO);
        }

        return responseDTOs;
    }

    // Mettre à jour un bon de commande (uniquement si brouillon)
    @Transactional
    public BonCommandeDTO modifierBonCommande(Long idBonCommande, BonCommandeDTO bonCommandeDTO) {
        BonCommande bonCommande = bonCommandeRepository.findById(idBonCommande)
                .orElseThrow(() -> new RuntimeException("Bon de commande non trouvé"));

        if (!"brouillon".equals(bonCommande.getEtat())) {
            throw new IllegalStateException("Seuls les bons de commande en état 'brouillon' peuvent être modifiés.");
        }

        Fournisseur fournisseur = fournisseurRepository.findById(bonCommandeDTO.getIdFournisseur())
                .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé"));
        
        
        if (fournisseur.getStatut() != 0) {
        	throw new IllegalArgumentException("Le fournisseur '" + fournisseur.getNom() + "' est inactif ou supprimé et ne peut pas être utilisé dans un bon de commande.");
        }
        
        

        User modifiedBy = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        ligneCommandeRepository.deleteAll(bonCommande.getLignesCommande());
        bonCommande.getLignesCommande().clear();

        List<LigneCommande> nouvellesLignes = bonCommandeDTO.getLignesCommande().stream().map(dto -> {
            Produit produit = produitRepository.findById(dto.getIdProduit())
                    .orElseThrow(() -> new RuntimeException("Produit non trouvé"));
            Prix prix = prixRepository.findByProduitIdProduitAndFournisseurIdFournisseurAndStatut(dto.getIdProduit(), fournisseur.getIdFournisseur(), 1)
                    .orElseThrow(() -> new RuntimeException("Prix actif non trouvé pour ce produit et fournisseur"));
            LigneCommande ligne = new LigneCommande();
            ligne.setProduit(produit);
            ligne.setQuantite(dto.getQuantite());
            ligne.setBonCommande(bonCommande);
            ligne.setPrix(prix);
            ligne.setFournisseur(fournisseur);
            return ligne;
        }).collect(Collectors.toList());

        bonCommande.setFournisseur(fournisseur);
        bonCommande.setLignesCommande(nouvellesLignes);
        bonCommande.setDateModification(new Date());
        bonCommande.setModifiedBy(modifiedBy);
        BonCommande savedBon = bonCommandeRepository.save(bonCommande);

        // Préparer la réponse avec les calculs
        BonCommandeDTO responseDTO = new BonCommandeDTO();
        responseDTO.setIdFournisseur(savedBon.getFournisseur().getIdFournisseur());
        List<LigneCommandeDTO> lignesDTO = savedBon.getLignesCommande().stream()
                .map(ligne -> {
                    LigneCommandeDTO ligneDTO = new LigneCommandeDTO();
                    ligneDTO.setIdProduit(ligne.getProduit().getIdProduit());
                    ligneDTO.setQuantite(ligne.getQuantite());
                    return ligneDTO;
                })
                .collect(Collectors.toList());
        responseDTO.setLignesCommande(lignesDTO);
        responseDTO.setLignesAvecCalculs(savedBon.getLignesCommande().stream()
                .map(this::calculerMontantsLigneCommande)
                .collect(Collectors.toList()));
        logger.info("Modification du bon de commande {} avec données : {}", idBonCommande, bonCommandeDTO);
        return responseDTO;
    }

    // Méthode pour récupérer un bon de commande avec calculs (pour affichage)
    public BonCommandeDTO getBonCommandeAvecCalculs(Long idBonCommande) {
        BonCommande bonCommande = bonCommandeRepository.findById(idBonCommande)
                .orElseThrow(() -> new RuntimeException("Bon de commande non trouvé"));

        BonCommandeDTO responseDTO = new BonCommandeDTO();
        responseDTO.setIdFournisseur(bonCommande.getFournisseur().getIdFournisseur());
        List<LigneCommandeDTO> lignesDTO = bonCommande.getLignesCommande().stream()
                .map(ligne -> {
                    LigneCommandeDTO ligneDTO = new LigneCommandeDTO();
                    ligneDTO.setIdProduit(ligne.getProduit().getIdProduit());
                    ligneDTO.setQuantite(ligne.getQuantite());
                    return ligneDTO;
                })
                .collect(Collectors.toList());
        responseDTO.setLignesCommande(lignesDTO);
        responseDTO.setLignesAvecCalculs(bonCommande.getLignesCommande().stream()
                .map(this::calculerMontantsLigneCommande)
                .collect(Collectors.toList()));

        return responseDTO;
    }

    // Les autres méthodes restent inchangées (approuverBonCommande, envoyerBonCommande, etc.)
    @Transactional
    public BonCommande approuverBonCommande(Long idBonCommande, ApprobationRequest request) {
        BonCommande bonCommande = bonCommandeRepository.findById(idBonCommande)
                .orElseThrow(() -> new RuntimeException("Bon de commande non trouvé"));

        if (!"brouillon".equals(bonCommande.getEtat())) {
            throw new IllegalStateException("Seuls les bons de commande en état 'brouillon' peuvent être approuvés ou rejetés.");
        }

        if (request.isApprouve()) {
            bonCommande.setEtat("approuvé");
            bonCommande.setDateRejet(null);
        } else {
            if (request.getCommentaireRejet() == null || request.getCommentaireRejet().trim().isEmpty()) {
                throw new IllegalArgumentException("Un commentaire de rejet est requis.");
            }
            bonCommande.setEtat("brouillon");
            bonCommande.setCommentaireRejet(request.getCommentaireRejet());
            bonCommande.setDateRejet(new Date());
        }

        return bonCommandeRepository.save(bonCommande);
    }

    @Transactional
    public BonCommande envoyerBonCommande(Long idBonCommande) throws Exception {
        BonCommande bonCommande = bonCommandeRepository.findById(idBonCommande)
                .orElseThrow(() -> new RuntimeException("Bon de commande non trouvé"));

        if (!"approuvé".equals(bonCommande.getEtat())) {
            throw new IllegalStateException("Seuls les bons de commande approuvés peuvent être envoyés.");
        }

        if (bonCommande.getFournisseur().getEmail() == null || bonCommande.getFournisseur().getEmail().isEmpty()) {
            throw new IllegalStateException("L’email du fournisseur est manquant.");
        }

        bonCommande.setEtat("envoyé");
        BonCommande updatedBon = bonCommandeRepository.save(bonCommande);

        try {
            emailService.sendBonCommandeEmail(updatedBon);
        } catch (MailSendException e) {
            logger.error("Erreur lors de l’envoi de l’email : {}", e.getMessage());
            Throwable cause = e;
            while (cause != null) {
                logger.info("Vérification de la cause : {}", cause.getClass().getName());
                if (cause instanceof UnknownHostException || cause instanceof ConnectException || e.getMessage().contains("Connection refused")) {
                    throw new RuntimeException("Vous êtes hors ligne. Vérifiez votre connexion Internet et réessayez plus tard.");
                }
                cause = cause.getCause();
            }
            throw new RuntimeException("Erreur lors de l’envoi de l’email au fournisseur : " + e.getMessage());
        }

        return updatedBon;
    }

    @Transactional
    public BonCommande annulerBonCommande(Long idBonCommande, AnnulationRequest request) {
        BonCommande bonCommande = bonCommandeRepository.findById(idBonCommande)
                .orElseThrow(() -> new RuntimeException("Bon de commande non trouvé"));

        if (!"approuvé".equals(bonCommande.getEtat()) && !"envoyé".equals(bonCommande.getEtat())) {
            throw new IllegalStateException("Seuls les bons de commande approuvés ou envoyés peuvent être annulés.");
        }

        if (request.getMotifAnnulation() == null || request.getMotifAnnulation().trim().isEmpty()) {
            throw new IllegalArgumentException("Un motif d'annulation est requis.");
        }

        bonCommande.setEtat("annulé");
        bonCommande.setMotifAnnulation(request.getMotifAnnulation());
        return bonCommandeRepository.save(bonCommande);
    }

    public List<BonCommande> getAllBonsDeCommande() {
        return bonCommandeRepository.findByEtatNot("envoyé");
    }

    public List<BonCommande> getBonsDeCommandeByEtat(String etat) {
        return bonCommandeRepository.findByEtat(etat);
    }
    
    public List<BonCommande> getBonsDeCommandeByEtats(List<String> etats) {
        return bonCommandeRepository.findByEtatIn(etats);
    }

    @Transactional
    public void supprimerBonCommande(Long idBonCommande) {
        BonCommande bonCommande = bonCommandeRepository.findById(idBonCommande)
                .orElseThrow(() -> new RuntimeException("Bon de commande non trouvé"));

        if (!"annulé".equals(bonCommande.getEtat())) {
            throw new IllegalStateException("Seuls les bons de commande annulés peuvent être supprimés.");
        }

        bonCommandeRepository.delete(bonCommande);
    }
}