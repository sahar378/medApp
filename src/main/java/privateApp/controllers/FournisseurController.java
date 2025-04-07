// src/main/java/privateApp/controllers/FournisseurController.java
package privateApp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import privateApp.dtos.AssocierProduitsRequest;
import privateApp.dtos.DissocierProduitsRequest;
import privateApp.models.Fournisseur;
import privateApp.services.FournisseurService;
import privateApp.services.PrixService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fournisseurs")
public class FournisseurController {

    @Autowired
    private FournisseurService fournisseurService;
    
    @Autowired
	private PrixService prixService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('INTENDANT', 'RESPONSABLE_STOCK')")
    public ResponseEntity<List<Fournisseur>> getFournisseursByStatut(@RequestParam(defaultValue = "0") int statut) {
        return ResponseEntity.ok(fournisseurService.getFournisseursByStatut(statut));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('INTENDANT', 'RESPONSABLE_STOCK')")
    public ResponseEntity<Fournisseur> getFournisseurById(@PathVariable Long id) {
        return ResponseEntity.ok(fournisseurService.getFournisseurById(id));
    }

    /*@PostMapping
    @PreAuthorize("hasAuthority('RESPONSABLE_STOCK')")
    public ResponseEntity<Fournisseur> createFournisseur(@RequestBody Fournisseur fournisseur) {
        return ResponseEntity.ok(fournisseurService.createFournisseur(fournisseur));
    }*/
    @PostMapping
    @PreAuthorize("hasAuthority('RESPONSABLE_STOCK')")
    public ResponseEntity<Map<String, Object>> createFournisseur(@RequestBody Fournisseur fournisseur) {
        Fournisseur existingFournisseur = fournisseurService.findExistingFournisseur(fournisseur.getNom(), fournisseur.getEmail());
        Map<String, Object> response = new HashMap<>();
        
        if (existingFournisseur != null) {
            response.put("message", "Fournisseur existe déjà dans le système");
            response.put("fournisseur", existingFournisseur);
            response.put("exists", true);
            return ResponseEntity.status(HttpStatus.OK).body(response); // Retourner 200 avec info
        }

        Fournisseur createdFournisseur = fournisseurService.createFournisseur(fournisseur);
        response.put("message", "Fournisseur créé avec succès");
        response.put("fournisseur", createdFournisseur);
        response.put("exists", false);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('RESPONSABLE_STOCK')")
    public ResponseEntity<Fournisseur> updateFournisseur(@PathVariable Long id, @RequestBody Fournisseur fournisseur) {
        return ResponseEntity.ok(fournisseurService.updateFournisseur(id, fournisseur));
    }

    @PutMapping("/{id}/statut")
    @PreAuthorize("hasAuthority('RESPONSABLE_STOCK')")
    public ResponseEntity<Fournisseur> changerStatut(
            @PathVariable Long id,
            @RequestParam int statut,
            @RequestParam(required = false) String causeSuppression) {
        return ResponseEntity.ok(fournisseurService.changerStatut(id, statut, causeSuppression));
    }

    @PostMapping("/{fournisseurId}/produits/{produitId}")
    @PreAuthorize("hasAuthority('RESPONSABLE_STOCK')")
    public ResponseEntity<Fournisseur> associerProduit(
            @PathVariable Long fournisseurId,
            @PathVariable Long produitId) {
        return ResponseEntity.ok(fournisseurService.associerProduit(fournisseurId, produitId));
    }

    @PostMapping("/{id}/associer-produits")
    @PreAuthorize("hasAuthority('RESPONSABLE_STOCK')")
    public ResponseEntity<Fournisseur> associerProduits(
            @PathVariable Long id,
            @RequestBody AssocierProduitsRequest request) {
        return ResponseEntity.ok(fournisseurService.associerProduits(id, request.getProduitIds()));
    }

    @GetMapping("/produit/{produitId}")
    @PreAuthorize("hasAuthority('RESPONSABLE_STOCK')")
    public ResponseEntity<List<Fournisseur>> getFournisseursByProduit(@PathVariable Long produitId) {
        return ResponseEntity.ok(fournisseurService.getFournisseursByProduit(produitId));
    }

    @DeleteMapping("/{fournisseurId}/produits/{produitId}")
    @PreAuthorize("hasAuthority('RESPONSABLE_STOCK')")
    public ResponseEntity<Fournisseur> dissocierProduit(
            @PathVariable Long fournisseurId,
            @PathVariable Long produitId) {
        return ResponseEntity.ok(fournisseurService.dissocierProduit(fournisseurId, produitId));
    }

    @DeleteMapping("/{id}/dissocier-produits")
    @PreAuthorize("hasAuthority('RESPONSABLE_STOCK')")
    public ResponseEntity<Fournisseur> dissocierProduits(
            @PathVariable Long id,
            @RequestBody DissocierProduitsRequest request) {
        return ResponseEntity.ok(fournisseurService.dissocierProduits(id, request.getProduitIds()));
    }
    @GetMapping("/produit/{produitId}/avec-prix")
    @PreAuthorize("hasAuthority('RESPONSABLE_STOCK')")
    public ResponseEntity<List<Fournisseur>> getFournisseursWithPrixByProduit(@PathVariable Long produitId) {
        return ResponseEntity.ok(prixService.getFournisseursWithPrixActifByProduit(produitId));
    }
}