package privateApp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import privateApp.dtos.ProduitAlerteDTO;
import privateApp.exception.ApiError;
import privateApp.models.Fournisseur;
import privateApp.models.Livraison;
import privateApp.models.Produit;
import privateApp.models.ProduitLog;
import privateApp.repositories.ProduitLogRepository;
import privateApp.services.StockService;

import java.util.List;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    @Autowired
    private StockService stockService;
    @Autowired
    private ProduitLogRepository produitLogRepository;
 // Endpoint pour ajouter un nouveau produit
    @PostMapping("/produit")
    @PreAuthorize("hasAuthority('RESPONSABLE_STOCK')")
    public ResponseEntity<?> addProduit(@RequestBody Produit produit) {
        try {
            Produit savedProduit = stockService.addProduit(produit);
            return ResponseEntity.ok(savedProduit);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError(e.getMessage()));
        }
    }

    // Endpoint pour mettre à jour un produit existant
    @PutMapping("/produit/{produitId}")
    @PreAuthorize("hasAuthority('RESPONSABLE_STOCK','PERSONNEL_MEDICAL')")
    public ResponseEntity<Produit> updateProduit(@PathVariable Long produitId, @RequestBody Produit produit) {
        produit.setIdProduit(produitId); // Assure que l’ID correspond
        return ResponseEntity.ok(stockService.updateProduit(produit));
    }
    @GetMapping("/produit/{produitId}")
    @PreAuthorize("hasAnyAuthority('INTENDANT', 'RESPONSABLE_STOCK')")
    public ResponseEntity<Produit> getProduitById(@PathVariable Long produitId) {
        return ResponseEntity.ok(stockService.getProduitById(produitId));
    }

    @GetMapping("/produits")
    @PreAuthorize("hasAnyAuthority('INTENDANT', 'RESPONSABLE_STOCK')")
    public ResponseEntity<List<Produit>> getProduits(
            @RequestParam(required = false) Long categorie) { // Ajout du paramètre categorie
        if (categorie != null) {
            return ResponseEntity.ok(stockService.getProduitsByCategorie(categorie));
        }
        return ResponseEntity.ok(stockService.getProduits()); // Si pas de catégorie, renvoie tous les produits
    }
    @GetMapping("/produits/archives")
    @PreAuthorize("hasAuthority('INTENDANT')")
    public ResponseEntity<List<Produit>> getArchivedProduits() {
        return ResponseEntity.ok(stockService.getArchivedProduits());
    }
    
    @GetMapping("/produits/active/medicaments")
    @PreAuthorize("hasAuthority('INTENDANT')")
    public ResponseEntity<List<Produit>> getActiveMedicaments() {
        return ResponseEntity.ok(stockService.getActiveMedicaments());
    }

    @GetMapping("/produits/active/materiels")
    @PreAuthorize("hasAuthority('INTENDANT','PERSONNEL_MEDICAL')")
    public ResponseEntity<List<Produit>> getActiveMateriels() {
        return ResponseEntity.ok(stockService.getActiveMateriels());
    }

    @GetMapping("/produits/archived/medicaments")
    @PreAuthorize("hasAuthority('INTENDANT')")
    public ResponseEntity<List<Produit>> getArchivedMedicaments() {
        return ResponseEntity.ok(stockService.getArchivedMedicaments());
    }

    @GetMapping("/produits/archived/materiels")
    @PreAuthorize("hasAuthority('INTENDANT')")
    public ResponseEntity<List<Produit>> getArchivedMateriels() {
        return ResponseEntity.ok(stockService.getArchivedMateriels());
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('INTENDANT')")
    public ResponseEntity<List<Produit>> getAllProduits() {
        return ResponseEntity.ok(stockService.getAllProduits());
    }
    /*@GetMapping("/produits/archives")
    @PreAuthorize("hasAuthority('INTENDANT')")
    public ResponseEntity<List<Produit>> getProduitsArchives() {
        return ResponseEntity.ok(produitRepository.findByArchiveTrue());
    }*/

    @PostMapping("/seuil")
    @PreAuthorize("hasAuthority('RESPONSABLE_STOCK')")
    public ResponseEntity<String> definirSeuilAlerte(@RequestParam Long produitId, @RequestParam int nombreMalades) {
        stockService.definirSeuilAlerte(produitId, nombreMalades);
        return ResponseEntity.ok("Seuil alerte mis à jour avec succès");
    }

    @PostMapping("/seuils/categorie")
    @PreAuthorize("hasAuthority('RESPONSABLE_STOCK')")
    public ResponseEntity<String> definirSeuilsCategorie(
            @RequestParam int idCategorie,
            @RequestParam int nombreMalades) { // Retiré userId
        stockService.definirSeuilsCategorie(idCategorie, nombreMalades);
        return ResponseEntity.ok("Seuils mis à jour avec succès");
    }
    
    
    
    @PostMapping("/seuil/automatique")
    @PreAuthorize("hasAuthority('RESPONSABLE_STOCK')")
    public ResponseEntity<String> definirSeuilAlerteAutomatique(@RequestParam Long produitId) {
        stockService.definirSeuilAlerteAutomatique(produitId);
        return ResponseEntity.ok("Seuil alerte mis à jour automatiquement avec succès");
    }

    @PostMapping("/seuils/categorie/automatique")
    @PreAuthorize("hasAuthority('RESPONSABLE_STOCK')")
    public ResponseEntity<String> definirSeuilsCategorieAutomatique(@RequestParam int idCategorie) {
        stockService.definirSeuilsCategorieAutomatique(idCategorie);
        return ResponseEntity.ok("Seuils mis à jour automatiquement avec succès");
    }

    
    
    @GetMapping("/alertes")
    @PreAuthorize("hasAnyAuthority('INTENDANT', 'RESPONSABLE_STOCK')")
    public ResponseEntity<List<ProduitAlerteDTO>> verifierAlertes() {
        return ResponseEntity.ok(stockService.verifierAlertes());
    }
    
    @GetMapping("/alertes/medicaments")
    @PreAuthorize("hasAnyAuthority('INTENDANT', 'RESPONSABLE_STOCK')")
    public ResponseEntity<List<ProduitAlerteDTO>> verifierAlertesMedicaments() {
        return ResponseEntity.ok(stockService.verifierAlertesMedicaments());
    }
    @GetMapping("/alertes/materiels")
    @PreAuthorize("hasAnyAuthority('INTENDANT', 'RESPONSABLE_STOCK')")
    public ResponseEntity<List<ProduitAlerteDTO>> verifierAlertesMateriels() {
        return ResponseEntity.ok(stockService.verifierAlertesMateriels());
    }

    @DeleteMapping("/produit/{produitId}")
    @PreAuthorize("hasAuthority('RESPONSABLE_STOCK')")
    public ResponseEntity<String> deleteProduit(@PathVariable Long produitId) { // Retiré userId
        try {
            stockService.deleteProduit(produitId);
            return ResponseEntity.ok("Produit supprimé avec succès");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
 // Nouvel endpoint pour l'intendant
    @GetMapping("/logs")
    @PreAuthorize("hasAuthority('INTENDANT')")
    public List<ProduitLog> getProduitLogs(@RequestParam Long produitId) {
        return produitLogRepository.findByProduitIdProduit(produitId);
    }
    
    @GetMapping("/produit/{produitId}/fournisseurs")
    @PreAuthorize("hasAnyAuthority('INTENDANT', 'RESPONSABLE_STOCK')")
    public ResponseEntity<List<Fournisseur>> getFournisseursByProduit(@PathVariable Long produitId) {
        Produit produit = stockService.getProduitById(produitId);
        return ResponseEntity.ok(produit.getFournisseurs());
    }
}