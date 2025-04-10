// privateApp.controllers/TechnicienController.java
package privateApp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import privateApp.models.Technicien;
import privateApp.services.TechnicienService;

import java.util.List;

@RestController
@RequestMapping("/api/techniciens")
public class TechnicienController {

    @Autowired
    private TechnicienService technicienService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('PERSONNEL_MEDICAL', 'INTENDANT')")
    public ResponseEntity<List<Technicien>> getAllTechniciens() {
        return ResponseEntity.ok(technicienService.getAllTechniciens());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERSONNEL_MEDICAL')")
    public ResponseEntity<Technicien> addTechnicien(@RequestBody Technicien technicien) {
        return ResponseEntity.ok(technicienService.addTechnicien(technicien));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERSONNEL_MEDICAL')")
    public ResponseEntity<Technicien> getTechnicienById(@PathVariable Long id) {
      Technicien technicien = technicienService.getTechnicienById(id);
      return ResponseEntity.ok(technicien);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERSONNEL_MEDICAL')")
    public ResponseEntity<Void> updateTechnicien(@PathVariable Long id, @RequestBody Technicien technicien) {
      technicienService.updateTechnicien(id, technicien);
      return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERSONNEL_MEDICAL')")
    public ResponseEntity<Void> deleteTechnicien(@PathVariable Long id) {
        technicienService.deleteTechnicien(id);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{id}/archive")
    @PreAuthorize("hasAuthority('PERSONNEL_MEDICAL')")
    public ResponseEntity<Void> archiveTechnicien(@PathVariable Long id) {
      technicienService.archiveTechnicien(id);
      return ResponseEntity.ok().build();
    }
 // privateApp.controllers/TechnicienController.java
    @GetMapping("/non-archived")
    @PreAuthorize("hasAnyAuthority('INTENDANT', 'PERSONNEL_MEDICAL')")
    public ResponseEntity<List<Technicien>> getNonArchivedTechniciens() {
      List<Technicien> techniciens = technicienService.getNonArchivedTechniciens();
      return ResponseEntity.ok(techniciens);
    }

    @GetMapping("/archived")
    @PreAuthorize("hasAuthority('INTENDANT')")
    public ResponseEntity<List<Technicien>> getArchivedTechniciens() {
      List<Technicien> techniciens = technicienService.getArchivedTechniciens();
      return ResponseEntity.ok(techniciens);
    }
}