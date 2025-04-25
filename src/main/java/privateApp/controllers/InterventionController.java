// privateApp.controllers/InterventionController.java
package privateApp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import privateApp.models.Intervention;
import privateApp.services.InterventionService;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/interventions")
public class InterventionController {

    @Autowired
    private InterventionService interventionService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('INFIRMIER', 'INTENDANT')")
    public ResponseEntity<List<Intervention>> getAllInterventions() {
        return ResponseEntity.ok(interventionService.getAllInterventions());
    }

    @GetMapping("/open")
    //@PreAuthorize("hasAuthority('PERSONNEL_MEDICAL')")
    @PreAuthorize("hasAnyAuthority('INFIRMIER', 'INTENDANT')")
    public ResponseEntity<List<Intervention>> getOpenInterventions() {
        return ResponseEntity.ok(interventionService.getOpenInterventions());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('INFIRMIER')")
    public ResponseEntity<Intervention> createIntervention(@RequestBody Intervention intervention) {
      Intervention createdIntervention = interventionService.createIntervention(intervention);
      return ResponseEntity.ok(createdIntervention);
    }

    @PutMapping("/{id}/close")
    @PreAuthorize("hasAuthority('INFIRMIER')")
    public ResponseEntity<Intervention> closeIntervention(
            @PathVariable Long id,
            @RequestParam String reparation,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateReparation,
            @RequestParam(required = false) String lieuReparation) { // Champ optionnel
        return ResponseEntity.ok(interventionService.closeIntervention(id, reparation, dateReparation, lieuReparation));
    }
    @PutMapping("/{id}/archive")
    @PreAuthorize("hasAuthority('INFIRMIER')")
    public ResponseEntity<Void> archiveIntervention(@PathVariable Long id) {
      interventionService.archiveIntervention(id);
      return ResponseEntity.ok().build();
    }
   /* @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERSONNEL_MEDICAL')")
    public ResponseEntity<Intervention> getInterventionById(@PathVariable Long id) {
      Intervention intervention = interventionService.getInterventionById(id);
      return ResponseEntity.ok(intervention);
    }*/

   /* @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERSONNEL_MEDICAL')")
    public ResponseEntity<Void> updateIntervention(@PathVariable Long id, @RequestBody Intervention intervention) {
      interventionService.updateIntervention(id, intervention);
      return ResponseEntity.ok().build();
    }*/
}