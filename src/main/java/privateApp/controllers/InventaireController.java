package privateApp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import privateApp.models.Inventaire;
import privateApp.models.LigneInventaire;
import privateApp.services.InventaireService;

import java.util.List;

@RestController
@RequestMapping("/api/medical/inventaire")
public class InventaireController {

    @Autowired
    private InventaireService inventaireService;

    @PostMapping("/verifier")
    @PreAuthorize("hasAuthority('PERSONNEL_MEDICAL')")
    public ResponseEntity<Inventaire> verifierEtCreerInventaire(@RequestBody List<LigneInventaire> lignes) {
        Inventaire inventaire = inventaireService.verifierEtCreerInventaire(lignes);
        return ResponseEntity.ok(inventaire);
    }

    @GetMapping("/historique")
    @PreAuthorize("hasAnyAuthority('INTENDANT', 'RESPONSABLE_STOCK')")
    public ResponseEntity<List<Inventaire>> getAllInventaires() {
        return ResponseEntity.ok(inventaireService.getAllInventaires());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('INTENDANT', 'RESPONSABLE_STOCK')")
    public ResponseEntity<Inventaire> getInventaireById(@PathVariable Long id) {
        return ResponseEntity.ok(inventaireService.getInventaireById(id));
    }
}