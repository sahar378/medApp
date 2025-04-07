// privateApp.controllers/LivraisonController.java
package privateApp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import privateApp.exception.ApiError;
import privateApp.models.Fournisseur;
import privateApp.models.Livraison;
import privateApp.models.Produit;
import privateApp.dtos.LivraisonRequest;
import privateApp.services.LivraisonService;
import privateApp.repositories.FournisseurRepository;
import privateApp.repositories.ProduitRepository;

import java.util.List;

@RestController
@RequestMapping("/api/livraisons")
public class LivraisonController {

    @Autowired
    private LivraisonService livraisonService;

    @Autowired
    private ProduitRepository produitRepository;

    @Autowired
    private FournisseurRepository fournisseurRepository;

    @PostMapping
    @PreAuthorize("hasAuthority('RESPONSABLE_STOCK')")
    public ResponseEntity<?> addLivraison(@RequestBody LivraisonRequest livraisonDTO) {
        try {
            Produit produit = produitRepository.findById(livraisonDTO.getIdProduit())
                    .orElseThrow(() -> new RuntimeException("Produit non trouvé"));
            Fournisseur fournisseur = fournisseurRepository.findById(livraisonDTO.getIdFournisseur())
                    .orElseThrow(() -> new RuntimeException("Fournisseur non trouvé"));

            Livraison livraison = new Livraison();
            livraison.setProduit(produit);
            livraison.setFournisseur(fournisseur);
            livraison.setQuantiteLivree(livraisonDTO.getQuantiteLivree());
            livraison.setDate(livraisonDTO.getDate());
            livraison.setObservation(livraisonDTO.getObservation());
            livraison.setLivreur(livraisonDTO.getLivreur());

            Livraison savedLivraison = livraisonService.addLivraison(livraison);
            return ResponseEntity.ok(savedLivraison);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiError(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiError(e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('INTENDANT', 'RESPONSABLE_STOCK')")
    public ResponseEntity<List<Livraison>> getAllLivraisons() {
        return ResponseEntity.ok(livraisonService.getAllLivraisons());
    }

    @GetMapping("/{produitId}")
    @PreAuthorize("hasAnyAuthority('INTENDANT', 'RESPONSABLE_STOCK')")
    public ResponseEntity<List<Livraison>> getLivraisonsByProduit(@PathVariable Long produitId) {
        return ResponseEntity.ok(livraisonService.getLivraisonsByProduit(produitId));
    }

    @GetMapping("/last-id")
    @PreAuthorize("hasAuthority('RESPONSABLE_STOCK')")
    public ResponseEntity<Long> getLastLivraisonId() {
        List<Livraison> livraisons = livraisonService.getAllLivraisons();
        if (livraisons.isEmpty()) {
            return ResponseEntity.ok(1L); // Si aucune livraison, commencer à 1
        }
        Long lastId = livraisons.stream()
                .mapToLong(Livraison::getIdLivraison)
                .max()
                .getAsLong();
        return ResponseEntity.ok(lastId);
    }
}