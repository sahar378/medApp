package privateApp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import privateApp.models.DetailMesure;
import privateApp.models.DetailSeanceProduitSpecialStandard;
import privateApp.models.Produit;
import privateApp.models.Seance;
import privateApp.repositories.DetailMesureRepository;
import privateApp.repositories.DetailSeanceProduitSpecialStandardRepository;
import privateApp.repositories.ProduitRepository;
import privateApp.repositories.SeanceRepository;
import privateApp.services.SeanceService;
import privateApp.services.StockService;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Contrôleur REST pour gérer les séances, mesures et produits utilisés.
 */
@RestController
@RequestMapping("/api/medical")
public class MedicalController {

    @Autowired
    private SeanceService seanceService;

    @Autowired
    private StockService stockService;

    @Autowired
    private SeanceRepository seanceRepository;

    @Autowired
    private DetailMesureRepository detailMesureRepository;

    @Autowired
    private DetailSeanceProduitSpecialStandardRepository detailSeanceProduitSpecialStandardRepository;

    @Autowired
    private ProduitRepository produitRepository;
    
    private static final Logger logger = LoggerFactory.getLogger(MedicalController.class);

    /**
     * Récupère les produits disponibles pour l'inventaire.
     * @return Liste des produits.
     */
    @GetMapping("/produits")
    @PreAuthorize("hasAuthority('INFIRMIER')")
    public ResponseEntity<List<Produit>> getProduitsForInventaire() {
        return ResponseEntity.ok(stockService.getProduitsForInventaire());
    }
    
    @GetMapping("/produits-standards")
    @PreAuthorize("hasAuthority('PERSONNEL_MEDICAL')")
    public ResponseEntity<List<Produit>> getAllProduitsStandards() {
        return ResponseEntity.ok(produitRepository.findByStandardTrue());
    }

    /**
     * Crée une nouvelle séance.
     * @param seance Les données de la séance.
     * @param serumSaleChoix Choix du sérum salé ("Sérum salé 1L" ou "Sérum salé 0.5L").
     * @return La séance créée.
     */
    @PostMapping("/seances")
    @PreAuthorize("hasAuthority('PERSONNEL_MEDICAL')")
    public ResponseEntity<Seance> createSeance(@RequestBody Seance seance, @RequestParam String serumSaleChoix) {
        return ResponseEntity.ok(seanceService.createSeance(seance, serumSaleChoix));
    }

    /**
     * Ajoute des produits non standards à une séance.
     * @param seanceId L'ID de la séance.
     * @param requestBody Corps de la requête contenant les différents types de produits.
     * @return Liste des détails enregistrés.
     */
    @PostMapping("/seances/{seanceId}/produits-non-standards")
    @PreAuthorize("hasAuthority('PERSONNEL_MEDICAL')")
    public ResponseEntity<List<DetailSeanceProduitSpecialStandard>> addProduitNonStandard(
            @PathVariable Long seanceId,
            @RequestBody Map<String, Map<String, String>> requestBody) {
        Map<String, String> produitsNonStandards = requestBody.getOrDefault("produitsNonStandards", new java.util.HashMap<>());
        Map<String, String> produitsSansStock = requestBody.getOrDefault("produitsSansStock", new java.util.HashMap<>());
        Map<String, String> produitsHorsStock = requestBody.getOrDefault("produitsHorsStock", new java.util.HashMap<>());
        Map<String, String> produitsSpeciaux = requestBody.getOrDefault("produitsSpeciaux", new java.util.HashMap<>());
        
        return ResponseEntity.ok(seanceService.addProduitNonStandard(
                seanceId, produitsNonStandards, produitsSansStock, produitsHorsStock, produitsSpeciaux));
    }

    /**
     * Met à jour les produits utilisés dans une séance.
     * @param seanceId L'ID de la séance.
     * @param requestBody Corps de la requête contenant les différents types de produits.
     * @return Liste des détails mis à jour.
     */
    @PutMapping("/seances/{seanceId}/produits")
    @PreAuthorize("hasAuthority('INTENDANT')")
    public ResponseEntity<List<DetailSeanceProduitSpecialStandard>> updateProduitNonStandard(
            @PathVariable Long seanceId,
            @RequestBody Map<String, Map<String, String>> requestBody) {
        Map<String, String> produitsNonStandards = requestBody.getOrDefault("produitsNonStandards", new java.util.HashMap<>());
        Map<String, String> produitsSansStock = requestBody.getOrDefault("produitsSansStock", new java.util.HashMap<>());
        Map<String, String> produitsHorsStock = requestBody.getOrDefault("produitsHorsStock", new java.util.HashMap<>());
        Map<String, String> produitsSpeciaux = requestBody.getOrDefault("produitsSpeciaux", new java.util.HashMap<>());
        
        return ResponseEntity.ok(seanceService.updateProduitNonStandard(
                seanceId, produitsNonStandards, produitsSansStock, produitsHorsStock, produitsSpeciaux));
    }

    /**
     * Ajoute une mesure à une séance.
     * @param seanceId L'ID de la séance.
     * @param mesure Les données de la mesure.
     * @return La mesure ajoutée.
     */
    @PostMapping("/seances/{seanceId}/mesures")
    @PreAuthorize("hasAuthority('PERSONNEL_MEDICAL')")
    @Transactional
    public ResponseEntity<DetailMesure> addMesure(@PathVariable Long seanceId, @RequestBody DetailMesure mesure) {
        logger.info("Ajout d'une mesure pour la séance ID: {}, Mesure: {}", seanceId, mesure);
        Seance seance = seanceRepository.findById(seanceId)
                .orElseThrow(() -> new RuntimeException("Séance non trouvée"));
        mesure.setSeance(seance);
        if (mesure.getHeure() == null) {
            logger.warn("Heure non fournie, utilisation de l'heure actuelle");
            mesure.setHeure(LocalDateTime.now());
        }
        try {
            DetailMesure savedMesure = detailMesureRepository.save(mesure);
            logger.info("Mesure enregistrée avec succès: {}", savedMesure);
            return ResponseEntity.ok(savedMesure);
        } catch (Exception e) {
            logger.error("Erreur lors de l'enregistrement de la mesure: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'enregistrement de la mesure: " + e.getMessage());
        }
    }

    /**
     * Récupère une séance par son ID.
     * @param seanceId L'ID de la séance.
     * @return La séance correspondante.
     */
    @GetMapping("/seances/{seanceId}")
    @PreAuthorize("hasAnyAuthority('PERSONNEL_MEDICAL','INTENDANT')")
    public ResponseEntity<Seance> getSeanceById(@PathVariable Long seanceId) {
        Seance seance = seanceRepository.findById(seanceId)
                .orElseThrow(() -> new RuntimeException("Séance non trouvée"));
        return ResponseEntity.ok(seance);
    }

    /**
     * Récupère toutes les séances ou les séances d'un patient spécifique.
     * @param patientId L'ID du patient (optionnel).
     * @return Liste des séances.
     */
    @GetMapping("/seances")
    @PreAuthorize("hasAuthority('PERSONNEL_MEDICAL')")
    public ResponseEntity<List<Seance>> getSeances(@RequestParam(required = false) Long patientId) {
        List<Seance> seances = seanceService.getSeancesByPatient(patientId, null, null);
        return ResponseEntity.ok(seances);
    }

    /**
     * Met à jour une séance existante.
     * @param seanceId L'ID de la séance.
     * @param seanceData Les données mises à jour de la séance.
     * @return La séance mise à jour.
     */
    @PutMapping("/seances/{seanceId}")
    @PreAuthorize("hasAuthority('PERSONNEL_MEDICAL')")
    public ResponseEntity<Seance> updateSeance(@PathVariable Long seanceId, @RequestBody Seance seanceData) {
        Seance existingSeance = seanceRepository.findById(seanceId)
                .orElseThrow(() -> new RuntimeException("Séance non trouvée"));

        existingSeance.setPatient(seanceData.getPatient());
        existingSeance.setMachine(seanceData.getMachine());
        existingSeance.setInfirmier(seanceData.getInfirmier());
        existingSeance.setMedecin(seanceData.getMedecin());
        existingSeance.setDate(seanceData.getDate());
        existingSeance.setObservation(seanceData.getObservation());
        existingSeance.setDialyseur(seanceData.getDialyseur());
        existingSeance.setCaBain(seanceData.getCaBain());
        existingSeance.setPpid(seanceData.getPpid());
        existingSeance.setPs(seanceData.getPs());
        existingSeance.setDebutDialyse(seanceData.getDebutDialyse());
        existingSeance.setFinDialyse(seanceData.getFinDialyse());
        existingSeance.setPoidsEntree(seanceData.getPoidsEntree());
        existingSeance.setPoidsSortie(seanceData.getPoidsSortie());
        existingSeance.setRestitution(seanceData.getRestitution());
        existingSeance.setCircuitFiltre(seanceData.getCircuitFiltre());
        existingSeance.setTaDebutDebout(seanceData.getTaDebutDebout());
        existingSeance.setTaDebutCouche(seanceData.getTaDebutCouche());
        existingSeance.setTemperatureDebut(seanceData.getTemperatureDebut());
        existingSeance.setTaFinDebout(seanceData.getTaFinDebout());
        existingSeance.setTaFinCouche(seanceData.getTaFinCouche());
        existingSeance.setTemperatureFin(seanceData.getTemperatureFin());
        existingSeance.setTraitement(seanceData.getTraitement());

        if (existingSeance.getDebutDialyse() != null && existingSeance.getFinDialyse() != null) {
            existingSeance.setDureeSeance((int) Duration.between(existingSeance.getDebutDialyse(), existingSeance.getFinDialyse()).toMinutes());
        }
        if (existingSeance.getPoidsEntree() != null && existingSeance.getPoidsSortie() != null) {
            existingSeance.setPertePoids(existingSeance.getPoidsEntree() - existingSeance.getPoidsSortie());
        }

        Seance updatedSeance = seanceRepository.save(existingSeance);
        return ResponseEntity.ok(updatedSeance);
    }

    /**
     * Récupère les produits utilisés dans une séance.
     * @param seanceId L'ID de la séance.
     * @return Liste des produits utilisés.
     */
    @GetMapping("/seances/{seanceId}/produits")
    @PreAuthorize("hasAnyAuthority('PERSONNEL_MEDICAL', 'RESPONSABLE_STOCK','INTENDANT')")
    public ResponseEntity<List<DetailSeanceProduitSpecialStandard>> getProduitsBySeance(@PathVariable Long seanceId) {
        return ResponseEntity.ok(detailSeanceProduitSpecialStandardRepository.findBySeanceIdSeance(seanceId));
    }

    /**
     * Récupère l'historique des mesures d'une séance.
     * @param seanceId L'ID de la séance.
     * @return Liste des mesures.
     */
    @GetMapping("/seances/{seanceId}/mesures")
    @PreAuthorize("hasAnyAuthority('PERSONNEL_MEDICAL', 'INTENDANT')")
    public ResponseEntity<List<DetailMesure>> getMesuresBySeance(@PathVariable Long seanceId) {
        return ResponseEntity.ok(detailMesureRepository.findBySeanceIdSeance(seanceId));
    }

    /**
     * Récupère les séances par date.
     * @param date La date au format YYYY-MM-DD.
     * @return Liste des séances.
     */
    @GetMapping("/by-date")
    @PreAuthorize("hasAuthority('PERSONNEL_MEDICAL')")
    public ResponseEntity<List<Seance>> getSeancesByDate(@RequestParam String date) {
        return ResponseEntity.ok(seanceService.getSeancesByDate(date));
    }
    
    @GetMapping("/produits-usage")
    @PreAuthorize("hasAuthority('PERSONNEL_MEDICAL')")
    public ResponseEntity<List<DetailSeanceProduitSpecialStandard>> getAllProduitsUsage() {
        logger.info("Récupération de tous les produits utilisés dans les séances");
        List<DetailSeanceProduitSpecialStandard> produitsUsage = detailSeanceProduitSpecialStandardRepository.findAllWithSeanceAndPatient();
        return ResponseEntity.ok(produitsUsage);
    }

    /**
     * Retrieves sessions for a specific patient, optionally filtered by date range.
     * If patientId is null, retrieves all sessions ordered by idSeance descending.
     * @param patientId The ID of the patient (optional).
     * @param startDate Optional start date (ISO format).
     * @param endDate Optional end date (ISO format).
     * @return List of sessions.
     */
    @GetMapping("/seances/patient")
    @PreAuthorize("hasAuthority('PERSONNEL_MEDICAL')")
    public ResponseEntity<List<Seance>> getSeancesByPatient(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
        ) {
            LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
            LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;
            return ResponseEntity.ok(seanceService.getSeancesByPatient(patientId, startDateTime, endDateTime));
        }
    
    /**
     * Retrieves sessions by date range, optionally filtered by patient.
     * @param patientId The patient ID (optional).
     * @param startDate The start date (required, format: yyyy-MM-dd).
     * @param endDate The end date (required, format: yyyy-MM-dd).
     * @return List of sessions.
     */
    @GetMapping("/seances/filter")
    @PreAuthorize("hasAnyAuthority('PERSONNEL_MEDICAL', 'RESPONSABLE_STOCK','INTENDANT')")
    public List<Seance> getSeancesByDateRange(
            @RequestParam(required = false) Long patientId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        return seanceService.getSeancesByDateRange(patientId, startDateTime, endDateTime);
    }
    
    @PutMapping("/seances/mesures/{mesureId}")
    @PreAuthorize("hasAuthority('PERSONNEL_MEDICAL')")
    @Transactional
    public ResponseEntity<DetailMesure> updateMesure(@PathVariable Long mesureId, @RequestBody DetailMesure mesureData) {
        logger.info("Mise à jour de la mesure ID: {}", mesureId);
        DetailMesure existingMesure = detailMesureRepository.findById(mesureId)
                .orElseThrow(() -> new RuntimeException("Mesure non trouvée"));
        existingMesure.setHeure(mesureData.getHeure());
        existingMesure.setTa(mesureData.getTa());
        existingMesure.setPouls(mesureData.getPouls());
        existingMesure.setDebitMlMn(mesureData.getDebitMlMn());
        existingMesure.setHep(mesureData.getHep());
        existingMesure.setPv(mesureData.getPv());
        existingMesure.setPtm(mesureData.getPtm());
        existingMesure.setConduc(mesureData.getConduc());
        existingMesure.setUfMlH(mesureData.getUfMlH());
        existingMesure.setUfTotalAffiche(mesureData.getUfTotalAffiche());
        existingMesure.setObservation(mesureData.getObservation());
        DetailMesure updatedMesure = detailMesureRepository.save(existingMesure);
        logger.info("Mesure mise à jour avec succès: {}", updatedMesure);
        return ResponseEntity.ok(updatedMesure);
    }
    @GetMapping("/produitsStandards")
    @PreAuthorize("hasAnyAuthority('PERSONNEL_MEDICAL', 'RESPONSABLE_STOCK')")
    public ResponseEntity<List<Produit>> getProduitByNom(@RequestParam(required = false) String nom) {
        if (nom != null && !nom.isEmpty()) {
        	Optional<Produit> produitOptional = stockService.getProduitByNom(nom);
        	return ResponseEntity.ok(produitOptional.map(List::of).orElse(List.of()));
        }
        return ResponseEntity.ok(stockService.getProduitsForInventaire());
    }
}