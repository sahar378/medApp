package privateApp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import privateApp.dtos.PrixDTO;
import privateApp.models.Fournisseur;
import privateApp.models.Notification;
import privateApp.models.Prix;
import privateApp.models.User;
import privateApp.repositories.NotificationRepository;
import privateApp.repositories.UserRepository;
import privateApp.services.PrixService;

import java.util.List;

@RestController
@RequestMapping("/api/prix")
public class PrixController {

    @Autowired
    private PrixService prixService;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private UserRepository userRepository;

    // Récupérer tous les prix
    @GetMapping
    @PreAuthorize("hasAuthority('RESPONSABLE_STOCK')")
    public ResponseEntity<List<Prix>> getAllPrix() {
        return ResponseEntity.ok(prixService.getAllPrix());
    }

    // Récupérer un prix par ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('RESPONSABLE_STOCK')")
    public ResponseEntity<Prix> getPrixById(@PathVariable Long id) {
        return ResponseEntity.ok(prixService.getPrixById(id));
    }

    // Créer un nouveau prix
    @PostMapping
    @PreAuthorize("hasAuthority('RESPONSABLE_STOCK')")
    public ResponseEntity<Prix> createPrix(@RequestBody Prix prix) {
        return ResponseEntity.ok(prixService.createPrix(prix));
    }

    // Mettre à jour un prix
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('RESPONSABLE_STOCK')")
    public ResponseEntity<Prix> updatePrix(@PathVariable Long id, @RequestBody Prix prix) {
        return ResponseEntity.ok(prixService.updatePrix(id, prix));
    }

    // Supprimer un prix
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('RESPONSABLE_STOCK')")
    public ResponseEntity<Void> deletePrix(@PathVariable Long id) {
        prixService.deletePrix(id);
        return ResponseEntity.ok().build();
    }

    // Nouveau endpoint pour la liste des produits avec prix actifs
    @GetMapping("/produits")
    @PreAuthorize("hasAnyAuthority('INTENDANT', 'RESPONSABLE_STOCK')")
    public ResponseEntity<List<PrixDTO>> getProduitsWithPrixActifs(
            @RequestParam(required = false) String categorie,
            @RequestParam(required = false) String nom,
            @RequestParam(defaultValue = "prixUnitaire") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder) {
        return ResponseEntity.ok(prixService.getProduitsWithPrixActifs(categorie, nom, sortBy, sortOrder));
    }

    
    @GetMapping("/produit/{produitId}")
    @PreAuthorize("hasAnyAuthority('INTENDANT', 'RESPONSABLE_STOCK')")
    public ResponseEntity<List<PrixDTO>> getPrixByProduit(
            @PathVariable Long produitId,
            @RequestParam(defaultValue = "prixUnitaire") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder) {
        return ResponseEntity.ok(prixService.getPrixByProduit(produitId, sortBy, sortOrder));
    }
    
    @GetMapping("/produits/all")
    @PreAuthorize("hasAnyAuthority('INTENDANT', 'RESPONSABLE_STOCK')")
    public ResponseEntity<List<PrixDTO>> getAllProduitsWithOptionalPrix(
            @RequestParam(required = false) String categorie,
            @RequestParam(required = false) String nom,
            @RequestParam(defaultValue = "prixUnitaire") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder) {
        return ResponseEntity.ok(prixService.getAllProduitsWithOptionalPrix(categorie, nom, sortBy, sortOrder));
    }



 // Dans FournisseurController.java
    @GetMapping("/produit/{produitId}/avec-prix")
    @PreAuthorize("hasAuthority('RESPONSABLE_STOCK')")
    public ResponseEntity<List<Fournisseur>> getFournisseursWithPrixByProduit(@PathVariable Long produitId) {
        return ResponseEntity.ok(prixService.getFournisseursWithPrixActifByProduit(produitId));
    }
 }

