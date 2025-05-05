package privateApp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import privateApp.models.Patient;
import privateApp.models.PatientDebutFin;
import privateApp.models.Produit;
import privateApp.services.PatientService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur REST pour gérer les patients.
 * Fournit des endpoints pour créer, mettre à jour, archiver, activer/désactiver, et rechercher des patients.
 */
@RestController
@RequestMapping("/api/medical/patients")
public class PatientController {

    @Autowired
    private PatientService patientService;

    /**
     * Récupère tous les patients non archivés (simplified data for INTENDANT, full data for MEDECIN).
     * @return Liste des patients non archivés.
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('MEDECIN', 'INTENDANT')")
    public ResponseEntity<List<?>> getAllPatients() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isIntendant = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("INTENDANT"));

        if (isIntendant) {
            // Return simplified patient data for INTENDANT
            List<Patient> patients = patientService.getAllPatients();
            List<SimplifiedPatient> simplifiedPatients = patients.stream()
                    .map(p -> new SimplifiedPatient(p.getIdPatient(), p.getPrenom(), p.getNom()))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(simplifiedPatients);
        } else {
            // Return full patient data for MEDECIN
            return ResponseEntity.ok(patientService.getAllPatients());
        }
    }

    /**
     * Simplified patient data class for INTENDANT
     */
    private static class SimplifiedPatient {
        private Long idPatient;
        private String prenom;
        private String nom;

        public SimplifiedPatient(Long idPatient, String prenom, String nom) {
            this.idPatient = idPatient;
            this.prenom = prenom;
            this.nom = nom;
        }

        // Getters and setters
        public Long getIdPatient() {
            return idPatient;
        }

        public String getPrenom() {
            return prenom;
        }

        public String getNom() {
            return nom;
        }
    }
    
    /**
     * Redirects legacy search requests to search active non-archived patients.
     * @param nom The name or part of the name.
     * @return List of active non-archived patients.
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('PERSONNEL_MEDICAL', 'MEDECIN','INTENDANT')")
    public ResponseEntity<List<Patient>> redirectSearch(@RequestParam String nom) {
        return ResponseEntity.ok(patientService.searchActiveNonArchivedPatientsByNom(nom));
    }

    /**
     * Récupère tous les patients archivés.
     * @return Liste des patients archivés.
     */
    @GetMapping("/archived")
    @PreAuthorize("hasAnyAuthority('MEDECIN', 'INTENDANT')")
    public ResponseEntity<List<Patient>> getArchivedPatients() {
        return ResponseEntity.ok(patientService.getArchivedPatients());
    }

    /**
     * Récupère un patient par son ID.
     * @param id L'ID du patient.
     * @return Le patient correspondant.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MEDECIN')")
    public ResponseEntity<Patient> getPatientById(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    /**
     * Recherche des patients actifs et non archivés par nom.
     * @param nom Le nom ou une partie du nom.
     * @return Liste des patients actifs et non archivés correspondants.
     */
    @GetMapping("/search/actifs")
    @PreAuthorize("hasAnyAuthority('PERSONNEL_MEDICAL', 'MEDECIN', 'INTENDANT')")
    public ResponseEntity<List<Patient>> searchActiveNonArchivedPatientsByNom(@RequestParam String nom) {
        return ResponseEntity.ok(patientService.searchActiveNonArchivedPatientsByNom(nom));
    }

    /**
     * Recherche des patients non actifs et non archivés par nom.
     * @param nom Le nom ou une partie du nom.
     * @return Liste des patients non actifs et non archivés correspondants.
     */
    @GetMapping("/search/inactifs-non-archives")
    @PreAuthorize("hasAnyAuthority('PERSONNEL_MEDICAL', 'MEDECIN' , 'INTENDANT')")
    public ResponseEntity<List<Patient>> searchInactiveNonArchivedPatientsByNom(@RequestParam String nom) {
        return ResponseEntity.ok(patientService.searchInactiveNonArchivedPatientsByNom(nom));
    }

    /**
     * Recherche des patients archivés par nom.
     * @param nom Le nom ou une partie du nom.
     * @return Liste des patients archivés correspondants.
     */
    @GetMapping("/search/archived")
    @PreAuthorize("hasAnyAuthority('MEDECIN' , 'INTENDANT')")
    public ResponseEntity<List<Patient>> searchArchivedPatientsByNom(@RequestParam String nom) {
        return ResponseEntity.ok(patientService.searchArchivedPatientsByNom(nom));
    }

    /**
     * Récupère les patients actifs et non archivés.
     * @return Liste des patients actifs et non archivés.
     */
    @GetMapping("/actifs")
    @PreAuthorize("hasAnyAuthority('PERSONNEL_MEDICAL', 'MEDECIN' , 'INTENDANT')")
    public ResponseEntity<List<Patient>> getActivePatients() {
        return ResponseEntity.ok(patientService.getActivePatients());
    }

    /**
     * Récupère les patients non actifs et non archivés.
     * @return Liste des patients non actifs et non archivés.
     */
    @GetMapping("/inactifs-non-archives")
    @PreAuthorize("hasAnyAuthority('PERSONNEL_MEDICAL', 'MEDECIN' , 'INTENDANT')")
    public ResponseEntity<List<Patient>> getInactiveNonArchivedPatients() {
        return ResponseEntity.ok(patientService.getInactiveNonArchivedPatients());
    }

    /**
     * Crée un nouveau patient.
     * @param patient Les données du patient.
     * @return Le patient créé.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('MEDECIN')")
    public ResponseEntity<Patient> createPatient(@RequestBody Patient patient) {
        return ResponseEntity.ok(patientService.createPatient(patient));
    }

    /**
     * Met à jour un patient.
     * @param id L'ID du patient.
     * @param patient Les nouvelles données.
     * @return Le patient mis à jour.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MEDECIN')")
    public ResponseEntity<Patient> updatePatient(@PathVariable Long id, @RequestBody Patient patient) {
        return ResponseEntity.ok(patientService.updatePatient(id, patient));
    }

    /**
     * Archive un patient.
     * @param id L'ID du patient.
     * @return Réponse vide.
     */
    @PutMapping("/{id}/archive")
    @PreAuthorize("hasAuthority('MEDECIN')")
    public ResponseEntity<Void> archivePatient(@PathVariable Long id) {
        patientService.archivePatient(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Active ou désactive un patient.
     * @param id L'ID du patient.
     * @return Le patient mis à jour.
     */
    @PutMapping("/{id}/toggle-actif")
    @PreAuthorize("hasAuthority('MEDECIN')")
    public ResponseEntity<Patient> togglePatientActif(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.togglePatientActif(id));
    }

    /**
     * Récupère les produits standards associés à un patient.
     * @param patientId L'ID du patient.
     * @return Liste des produits standards.
     */
    @GetMapping("/{patientId}/produits-standards")
    @PreAuthorize("hasAnyAuthority('PERSONNEL_MEDICAL', 'MEDECIN', 'RESPONSABLE_STOCK','INTENDANT')")
    public ResponseEntity<List<Produit>> getProduitsStandards(@PathVariable Long patientId) {
        Patient patient = patientService.getPatientById(patientId);
        return ResponseEntity.ok(patient.getProduitsStandards());
    }

    /**
     * Récupère l'historique de dialyse d'un patient.
     * @param patientId L'ID du patient.
     * @return Liste des périodes de dialyse.
     */
    @GetMapping("/{patientId}/dialysis-history")
    @PreAuthorize("hasAnyAuthority 'MEDECIN', 'INTENDANT')")
    public ResponseEntity<List<PatientDebutFin>> getDialysisHistory(@PathVariable Long patientId) {
        Patient patient = patientService.getPatientById(patientId);
        return ResponseEntity.ok(patient.getDebutFinRecords());
    }

    /**
     * Restaure un patient archivé en le rendant non archivé et actif.
     * @param id L'ID du patient.
     * @return Réponse vide.
     * @throws RuntimeException si le patient n'est pas archivé ou n'existe pas.
     */
    @PutMapping("/{id}/unarchive")
    @PreAuthorize("hasAuthority('MEDECIN')")
    public ResponseEntity<Void> unarchivePatient(@PathVariable Long id) {
        patientService.unarchivePatient(id);
        return ResponseEntity.ok().build();
    }
}