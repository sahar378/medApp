package privateApp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import privateApp.models.Produit;
import privateApp.services.StockService;

import java.util.List;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    @Autowired
    private StockService stockService;

    // Ajouter ou mettre à jour un produit (réservé à RESPONSABLE_STOCK)
    @PostMapping("/produit")
    @PreAuthorize("hasAuthority('RESPONSABLE_STOCK')")
    public ResponseEntity<Produit> saveProduit(@RequestBody Produit produit, @RequestParam Long userId) {
        Produit savedProduit = stockService.saveProduit(produit, userId);
        return ResponseEntity.ok(savedProduit);
    }

    // Récupérer les produits d’un responsable (accessible à INTENDANT et RESPONSABLE_STOCK)
    @GetMapping("/produits")
    public ResponseEntity<List<Produit>> getProduits(@RequestParam Long userId) {
        List<Produit> produits = stockService.getProduitsByUser(userId);
        return ResponseEntity.ok(produits);
    }

    // Nouveau endpoint : Récupérer tous les produits (pour INTENDANT)
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('INTENDANT')")
    public ResponseEntity<List<Produit>> getAllProduits() {
        List<Produit> produits = stockService.getAllProduits();
        return ResponseEntity.ok(produits);
    }

    // Définir ou recalculer le seuil alerte (réservé à RESPONSABLE_STOCK)
    @PostMapping("/seuil")
    @PreAuthorize("hasAuthority('RESPONSABLE_STOCK')")
    public ResponseEntity<String> definirSeuilAlerte(@RequestParam Long produitId, @RequestParam int nombreMalades) {
        stockService.definirSeuilAlerte(produitId, nombreMalades);
        return ResponseEntity.ok("Seuil alerte mis à jour avec succès");
    }

    // Vérifier les alertes (accessible à INTENDANT et RESPONSABLE_STOCK)
    @GetMapping("/alertes")
    public ResponseEntity<List<Produit>> verifierAlertes(@RequestParam Long userId) {
        List<Produit> produitsEnAlerte = stockService.verifierAlertes(userId);
        return ResponseEntity.ok(produitsEnAlerte);
    }

    // Supprimer un produit (réservé à RESPONSABLE_STOCK)
    @DeleteMapping("/produit/{produitId}")
    @PreAuthorize("hasAuthority('RESPONSABLE_STOCK')")
    public ResponseEntity<String> deleteProduit(@PathVariable Long produitId, @RequestParam Long userId) {
        try {
            stockService.deleteProduit(produitId, userId);
            return ResponseEntity.ok("Produit supprimé avec succès");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}