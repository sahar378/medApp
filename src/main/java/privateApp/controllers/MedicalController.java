package privateApp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import privateApp.models.Produit;
import privateApp.services.StockService;

import java.util.List;

@RestController
@RequestMapping("/api/medical")
public class MedicalController {

    @Autowired
    private StockService stockService;

    @GetMapping("/produits")
    @PreAuthorize("hasAuthority('PERSONNEL_MEDICAL')")
    public ResponseEntity<List<Produit>> getProduitsForInventaire() {
        return ResponseEntity.ok(stockService.getProduitsForInventaire());
    }
}