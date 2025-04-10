package privateApp.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import privateApp.dtos.AnnulationRequest;
import privateApp.dtos.ApprobationRequest;
import privateApp.dtos.BonCommandeDTO;
import privateApp.dtos.LivraisonRequest;
import privateApp.exception.ApiError;
import privateApp.models.BonCommande;
import privateApp.models.LigneCommande;
import privateApp.models.Prix;
import privateApp.repositories.BonCommandeRepository;
import privateApp.repositories.PrixRepository;
import privateApp.services.CommandeService;
import privateApp.services.LivraisonService;

import java.util.List;

@RestController
@RequestMapping("/api/commande")
public class CommandeController {
    private static final Logger logger = LoggerFactory.getLogger(LivraisonService.class);

    @Autowired
    private CommandeService commandeService;
    @Autowired
	private PrixRepository prixRepository;
	private BonCommandeRepository bonCommandeRepository;

    // Créer un bon de commande (Responsable Stock)
	@PostMapping("/creer")
	@PreAuthorize("hasAuthority('RESPONSABLE_STOCK')")
	public ResponseEntity<?> creerBonCommande(@RequestBody BonCommandeDTO bonCommandeDTO) {
	    try {
	        BonCommandeDTO result = commandeService.creerBonCommande(bonCommandeDTO);
	        return ResponseEntity.ok(result);
	    } catch (IllegalArgumentException e) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                .body(new ApiError(e.getMessage()));
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new ApiError("Erreur inattendue lors de la création du bon de commande : " + e.getMessage()));
	    }
	}

 // Nouveau endpoint : Créer plusieurs bons de commande regroupés par fournisseur
    @PostMapping("/creer-multi")
    @PreAuthorize("hasAuthority('RESPONSABLE_STOCK')")
    public ResponseEntity<?> creerBonsCommandeParFournisseur(@RequestBody List<BonCommandeDTO> bonsCommandeDTO) {
        try {
            List<BonCommandeDTO> result = commandeService.creerBonsCommandeParFournisseur(bonsCommandeDTO);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiError(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiError("Erreur inattendue lors de la création des bons de commande : " + e.getMessage()));
        }
    }
    
    
    // Modifier un bon de commande (Responsable Stock, brouillon uniquement)
    @PutMapping("/{idBonCommande}")
    @PreAuthorize("hasAuthority('RESPONSABLE_STOCK')")
    public ResponseEntity<?> modifierBonCommande(@PathVariable("idBonCommande") Long id, @RequestBody BonCommandeDTO bonCommandeDTO) {
        try {
            BonCommandeDTO updatedBon = commandeService.modifierBonCommande(id, bonCommandeDTO);
            return ResponseEntity.ok(updatedBon);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiError(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiError("Erreur inattendue lors de la modification du bon de commande : " + e.getMessage()));
        }
    }
    
    @GetMapping("/bon/{id}/avec-calculs")
    @PreAuthorize("hasAnyAuthority('INTENDANT', 'RESPONSABLE_STOCK')")
    public ResponseEntity<BonCommandeDTO> getBonCommandeAvecCalculs(@PathVariable Long id) {
        return ResponseEntity.ok(commandeService.getBonCommandeAvecCalculs(id));
    }

    // Approuver ou rejeter un bon de commande (Intendant)
    @PostMapping("/{idBonCommande}/approuver")
    @PreAuthorize("hasAuthority('INTENDANT')")
    public ResponseEntity<BonCommande> approuverBonCommande(
            @PathVariable Long idBonCommande,
            @RequestBody ApprobationRequest request) {
        return ResponseEntity.ok(commandeService.approuverBonCommande(idBonCommande, request));
    }

    @PostMapping("/{idBonCommande}/envoyer")
    @PreAuthorize("hasAuthority('INTENDANT')")
    public ResponseEntity<?> envoyerBonCommande(@PathVariable Long idBonCommande) {
        try {
            BonCommande bonCommande = commandeService.envoyerBonCommande(idBonCommande);
            return ResponseEntity.ok(bonCommande);
        } catch (RuntimeException e) {
            // Retourner un objet ApiError pour les erreurs spécifiques
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiError(e.getMessage()));
        } catch (Exception e) {
            // Retourner un objet ApiError pour les erreurs inattendues
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiError("Erreur inattendue lors de l’envoi du bon de commande : " + e.getMessage()));
        }
    }

    // Annuler un bon de commande (Intendant)
    @PostMapping("/{idBonCommande}/annuler")
    @PreAuthorize("hasAuthority('INTENDANT')")
    public ResponseEntity<BonCommande> annulerBonCommande(
            @PathVariable Long idBonCommande,
            @RequestBody AnnulationRequest request) {
        return ResponseEntity.ok(commandeService.annulerBonCommande(idBonCommande, request));
    }

 // Récupérer tous les bons de commande sauf envoyés (Intendant et Responsable Stock)
    @GetMapping("/bons")
    @PreAuthorize("hasAnyAuthority('INTENDANT', 'RESPONSABLE_STOCK')")
    public ResponseEntity<List<BonCommande>> getAllBonsDeCommande() {
        return ResponseEntity.ok(commandeService.getAllBonsDeCommande());
    }

    // Récupérer les bons de commande par état (Intendant et Responsable Stock)
    @GetMapping("/bons/etat/{etat}")
    @PreAuthorize("hasAnyAuthority('INTENDANT', 'RESPONSABLE_STOCK')")
    public ResponseEntity<List<BonCommande>> getBonsDeCommandeByEtat(@PathVariable String etat) {
        return ResponseEntity.ok(commandeService.getBonsDeCommandeByEtat(etat));
    }
    
    @GetMapping("/bons/etats-multiples")
    @PreAuthorize("hasAnyAuthority('INTENDANT', 'RESPONSABLE_STOCK')")
    public ResponseEntity<List<BonCommande>> getBonsDeCommandeByEtats(@RequestParam List<String> etats) {
        return ResponseEntity.ok(commandeService.getBonsDeCommandeByEtats(etats));
    }
 // Supprimer un bon de commande annulé (Intendant uniquement)
    @DeleteMapping("/{idBonCommande}")
    @PreAuthorize("hasAuthority('INTENDANT')")
    public ResponseEntity<String> supprimerBonCommande(@PathVariable Long idBonCommande) {
        commandeService.supprimerBonCommande(idBonCommande);
        return ResponseEntity.ok("Bon de commande supprimé avec succès");
    }
    //Permet de récupérer le prix actif (statut = 1) pour un produit (produitId) et un fournisseur (fournisseurId).
    @GetMapping("/prix/actif")
    @PreAuthorize("hasAuthority('RESPONSABLE_STOCK')")
    public ResponseEntity<Prix> getPrixActif(
        @RequestParam Long produitId,
        @RequestParam Long fournisseurId) {
      Prix prix = prixRepository.findByProduitIdProduitAndFournisseurIdFournisseurAndStatut(produitId, fournisseurId, 1)
          .orElseThrow(() -> new RuntimeException("Prix actif non trouvé"));
      return ResponseEntity.ok(prix);
    }
    @GetMapping("/bon/{id}")
    @PreAuthorize("hasAnyAuthority('INTENDANT', 'RESPONSABLE_STOCK')")
    public ResponseEntity<BonCommande> getBonCommandeDetails(@PathVariable Long id) {
        BonCommande bonCommande = bonCommandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bon de commande non trouvé"));
        return ResponseEntity.ok(bonCommande);
    }

    // Enregistrer une livraison (Responsable Stock)
   /* @PostMapping("/livraison")
    @PreAuthorize("hasAuthority('RESPONSABLE_STOCK')")
    public ResponseEntity<String> enregistrerLivraison(@RequestBody LivraisonRequest livraisonRequest) {
        commandeService.enregistrerLivraison(livraisonRequest);
        return ResponseEntity.ok("Livraison enregistrée avec succès");
    }*/
    
}