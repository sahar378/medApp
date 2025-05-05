package privateApp.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import privateApp.models.*;
import privateApp.repositories.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service pour gérer les opérations sur les séances de dialyse.
 * Inclut la création de séances, l'ajout de produits, et l'enregistrement des mesures.
 */
@Service
public class SeanceService {
	
	private static final Logger logger = LoggerFactory.getLogger(SeanceService.class);

    @Autowired
    private SeanceRepository seanceRepository;

    @Autowired
    private DetailSeanceProduitSpecialStandardRepository detailProduitRepository;

    @Autowired
    private DetailMesureRepository detailMesureRepository;

    @Autowired
    private MachineRepository machineRepository;

    @Autowired
    private StockService stockService;

    @Autowired
    private ProduitLogRepository produitLogRepository;
    
    @Autowired
    private PatientRepository patientRepository;
    
    // Liste des produits non standards qui affectent le stock
    private static final List<String> PRODUITS_NON_STANDARDS_AFFECTANT_STOCK = Arrays.asList(
        "Sérum salé 0.5L", "Seringue 10cc", "Seringue 5cc", "Robinet",
        "Transfuseur", "Lame bistouri", "Compress", "Gants", "Masque O2", "Lunette O2",
        "Fils de suture", "Bandelette", "Filtre (dialyseur)", "ligne artérielle",
        "ligne veineuse", "2 aiguilles", "champ stérile"
    );

    // Liste des produits non standards qui n'affectent pas le stock (inclut traitements)
    private static final List<String> PRODUITS_NON_STANDARDS_SANS_STOCK = Arrays.asList(
        "Héparine", "Ether", "Néofix"
    );

    // Observations pour chaque type de produit
    private static final String OBSERVATION_PRODUITS_STANDARDS = 
        "utilisation normal de produits standard qui affecte le stock";
    private static final String OBSERVATION_PRODUITS_NON_STANDARDS_AFFECTANT_STOCK = 
        "utilisation additionnel de produits non standard qui affectent le stock";
    private static final String OBSERVATION_PRODUITS_NON_STANDARDS_SANS_STOCK = 
        "utilisation additionnel de produits non standard qui n'affectent pas le stock";
    private static final String OBSERVATION_PRODUITS_SPECIAUX = 
        "utilisation spécial de produits spécial qui affectent le stock";
    private static final String OBSERVATION_PRODUITS_HORS_STOCK = 
        "utilisation de produits hors de stock";

    /**
     * Crée une nouvelle séance et gère les produits standards du patient.
     * - Vérifie que la machine est disponible (disponibilite = 0) et non archivée.
     * - Calcule automatiquement dureeSeance et pertePoids.
     * - Utilise les produits standards associés au patient via patient_produit_standard.
     * @param seance Les données de la séance.
     * @param serumSaleChoix Choix entre "Sérum salé 1L" ou "Sérum salé 0.5L".
     * @return La séance créée.
     */
    @Transactional
    public Seance createSeance(Seance seance, String serumSaleChoix) {
        // Vérifier la disponibilité de la machine
        Machine machine = seance.getMachine();
        if (machine == null || machine.getDisponibilite() != 0 || machine.isArchived()) {
            throw new RuntimeException("La machine sélectionnée n'est pas disponible");
        }

        // Calculer dureeSeance et pertePoids
        if (seance.getDebutDialyse() != null && seance.getFinDialyse() != null) {
            seance.setDureeSeance((int) Duration.between(seance.getDebutDialyse(), seance.getFinDialyse()).toMinutes());
        }
        if (seance.getPoidsEntree() != null && seance.getPoidsSortie() != null) {
            seance.setPertePoids(seance.getPoidsEntree() - seance.getPoidsSortie());
        }

        // Enregistrer la séance
        Seance savedSeance = seanceRepository.save(seance);

        // Gérer les produits standards
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Choix du Sérum salé
        if (!serumSaleChoix.equals("Sérum salé 1L") && !serumSaleChoix.equals("Sérum salé 0.5L")) {
            throw new RuntimeException("Choix de Sérum salé invalide");
        }
        Produit serumSale = stockService.getProduitByNom(serumSaleChoix)
                .orElseThrow(() -> new RuntimeException("Sérum salé non trouvé : " + serumSaleChoix));
        // Quantité administrée : 1 unité pour 1L, 2 unités pour 0.5L (équivalent à 1L)
        int qteAdministreSerum = serumSaleChoix.equals("Sérum salé 1L") ? 1 : 2;
        if (serumSale.getQteDisponible() < qteAdministreSerum) {
            throw new RuntimeException("Stock insuffisant pour : " + serumSaleChoix);
        }
        if (serumSale.isArchive()) {
            throw new RuntimeException("Produit archivé : " + serumSaleChoix);
        }
        serumSale.setQteDisponible(serumSale.getQteDisponible() - qteAdministreSerum);
        stockService.updateProduit(serumSale);
        
        logger.info("Sérum salé choisi pour la séance {} : {}", savedSeance.getIdSeance(), serumSaleChoix);
        
        // Enregistrer dans ProduitLog
        ProduitLog logSerum = new ProduitLog();
        logSerum.setUser(currentUser);
        logSerum.setProduit(serumSale);
        logSerum.setAction("UTILISATION");
        logSerum.setDetails("Produit utilisé dans une séance - Quantité diminuée de " + qteAdministreSerum);
        logSerum.setDateAction(LocalDateTime.now());
        produitLogRepository.save(logSerum);

        // Enregistrer dans DetailSeanceProduitSpecialStandard
        DetailSeanceProduitSpecialStandard detailSerum = new DetailSeanceProduitSpecialStandard();
        detailSerum.setSeance(savedSeance);
        detailSerum.setProduit(serumSale);
        detailSerum.setQteAdministre(String.valueOf(qteAdministreSerum));
        detailSerum.setDateTemps(seance.getDate());
        detailSerum.setNomProduit(serumSale.getNom());
        detailSerum.setStandard(true);
        detailSerum.setObservation(OBSERVATION_PRODUITS_STANDARDS); // Set observation
        detailProduitRepository.save(detailSerum);

        // Utiliser les produits standards du patient depuis patient_produit_standard
        List<Produit> produitsStandards = patientRepository.findProduitsStandardsByPatientId(
        	    seance.getPatient().getIdPatient()
        	);
        if (produitsStandards == null || produitsStandards.isEmpty()) {
            logger.error("Patient {} n'a aucun produit standard", savedSeance.getPatient().getIdPatient());
            throw new RuntimeException("Configuration erreur : Aucun produit standard défini dans le système");
        }

        for (Produit produit : produitsStandards) {
            if (produit.getQteDisponible() <= 0) {
                throw new RuntimeException("Stock insuffisant pour le produit standard : " + produit.getNom());
            }
            if (produit.isArchive()) {
                throw new RuntimeException("Produit archivé : " + produit.getNom());
            }
            produit.setQteDisponible(produit.getQteDisponible() - 1);
            stockService.updateProduit(produit);

            // Enregistrer dans ProduitLog
            ProduitLog log = new ProduitLog();
            log.setUser(currentUser);
            log.setProduit(produit);
            log.setAction("UTILISATION");
            log.setDetails("Produit utilisé dans une séance - Quantité diminuée de 1");
            log.setDateAction(LocalDateTime.now());
            produitLogRepository.save(log);

            // Enregistrer dans DetailSeanceProduitSpecialStandard
            DetailSeanceProduitSpecialStandard detail = new DetailSeanceProduitSpecialStandard();
            detail.setSeance(savedSeance);
            detail.setProduit(produit);
            detail.setQteAdministre("1");
            detail.setDateTemps(seance.getDate());
            detail.setNomProduit(produit.getNom());
            detail.setStandard(true);
            detail.setObservation(OBSERVATION_PRODUITS_STANDARDS); // Set observation
            detailProduitRepository.save(detail);
        }

        return savedSeance;
    }

    /**
     * Ajoute des produits non standards à une séance existante (utilisé par les infirmiers).
     * - Gère les produits affectant le stock, sans impact sur le stock, hors stock, et spéciaux.
     * - Enregistre un log dans ProduitLog pour les produits affectant le stock et spéciaux.
     * @param seanceId L'ID de la séance.
     * @param produitsNonStandards Map des produits affectant le stock (nom -> quantité).
     * @param produitsSansStock Map des produits sans impact (nom -> quantité).
     * @param produitsHorsStock Map des produits hors stock (nom -> quantité).
     * @param produitsSpeciaux Map des produits spéciaux (matériel, nom -> quantité).
     * @return Liste des détails enregistrés.
     */
    @Transactional
    public List<DetailSeanceProduitSpecialStandard> addProduitNonStandard(Long seanceId,
            Map<String, String> produitsNonStandards, Map<String, String> produitsSansStock,
            Map<String, String> produitsHorsStock, Map<String, String> produitsSpeciaux) {
        Seance seance = seanceRepository.findById(seanceId)
                .orElseThrow(() -> new RuntimeException("Séance non trouvée"));
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<DetailSeanceProduitSpecialStandard> details = new java.util.ArrayList<>();

        // Produits non standards affectant le stock
        for (Map.Entry<String, String> entry : produitsNonStandards.entrySet()) {
            String produitNom = entry.getKey();
            String qte = entry.getValue();
            if (!PRODUITS_NON_STANDARDS_AFFECTANT_STOCK.contains(produitNom)) {
                throw new RuntimeException("Produit non standard invalide : " + produitNom);
            }
            Produit produit = stockService.getProduitByNom(produitNom)
                    .orElseThrow(() -> new RuntimeException("Produit non trouvé : " + produitNom));
            if (produit.isArchive()) {
                throw new RuntimeException("Produit archivé : " + produitNom);
            }
            int qteToDeduct;
            try {
                qteToDeduct = Integer.parseInt(qte);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Quantité invalide pour le produit : " + produitNom);
            }
            if (qteToDeduct <= 0) {
                continue; // Skip if quantity is zero or negative
            }
            if (produit.getQteDisponible() < qteToDeduct) {
                throw new RuntimeException("Stock insuffisant pour le produit : " + produitNom);
            }
            produit.setQteDisponible(produit.getQteDisponible() - qteToDeduct);
            stockService.updateProduit(produit);

            // Enregistrer dans ProduitLog
            ProduitLog log = new ProduitLog();
            log.setUser(currentUser);
            log.setProduit(produit);
            log.setAction("UTILISATION");
            log.setDetails("Produit utilisé dans une séance - Quantité diminuée de " + qteToDeduct);
            log.setDateAction(LocalDateTime.now());
            produitLogRepository.save(log);

            // Enregistrer dans DetailSeanceProduitSpecialStandard
            DetailSeanceProduitSpecialStandard detail = new DetailSeanceProduitSpecialStandard();
            detail.setSeance(seance);
            detail.setProduit(produit);
            detail.setQteAdministre(qte);
            detail.setDateTemps(LocalDateTime.now());
            detail.setNomProduit(produit.getNom());
            detail.setStandard(false);
            detail.setObservation(OBSERVATION_PRODUITS_NON_STANDARDS_AFFECTANT_STOCK); // Set observation
            details.add(detailProduitRepository.save(detail));
        }

        // Produits non standards sans impact sur le stock
        for (Map.Entry<String, String> entry : produitsSansStock.entrySet()) {
            String produitNom = entry.getKey();
            String qte = entry.getValue();
            if (!PRODUITS_NON_STANDARDS_SANS_STOCK.contains(produitNom)) {
                throw new RuntimeException("Produit sans stock invalide : " + produitNom);
            }
            int qteAdmin;
            try {
                qteAdmin = Integer.parseInt(qte);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Quantité invalide pour le produit : " + produitNom);
            }
            if (qteAdmin <= 0) {
                continue; // Skip if quantity is zero or negative
            }
            DetailSeanceProduitSpecialStandard detail = new DetailSeanceProduitSpecialStandard();
            detail.setSeance(seance);
            detail.setProduit(null); // Pas de produit en stock
            detail.setQteAdministre(qte);
            detail.setDateTemps(LocalDateTime.now());
            detail.setNomProduit(produitNom);
            detail.setStandard(false);
            detail.setObservation(OBSERVATION_PRODUITS_NON_STANDARDS_SANS_STOCK); // Set observation
            details.add(detailProduitRepository.save(detail));
        }

        // Produits hors stock
        for (Map.Entry<String, String> entry : produitsHorsStock.entrySet()) {
            String produitNom = entry.getKey();
            String qte = entry.getValue();
            int qteAdmin;
            try {
                qteAdmin = Integer.parseInt(qte);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Quantité invalide pour le produit : " + produitNom);
            }
            if (qteAdmin <= 0) {
                continue; // Skip if quantity is zero or negative
            }
            DetailSeanceProduitSpecialStandard detail = new DetailSeanceProduitSpecialStandard();
            detail.setSeance(seance);
            detail.setProduit(null); // Pas de produit en stock
            detail.setQteAdministre(qte);
            detail.setDateTemps(LocalDateTime.now());
            detail.setNomProduit(produitNom);
            detail.setStandard(false);
            detail.setObservation(OBSERVATION_PRODUITS_HORS_STOCK); // Set observation
            details.add(detailProduitRepository.save(detail));
        }

        // Produits spéciaux (matériel)
        for (Map.Entry<String, String> entry : produitsSpeciaux.entrySet()) {
            String produitNom = entry.getKey();
            String qte = entry.getValue();
            Produit produit = stockService.getProduitByNom(produitNom)
                    .orElseThrow(() -> new RuntimeException("Produit spécial non trouvé : " + produitNom));
            if (produit.isArchive()) {
                throw new RuntimeException("Produit archivé : " + produitNom);
            }
            if (!produit.getCategorie().getLibelleCategorie().equalsIgnoreCase("materiel")) {
                throw new RuntimeException("Produit spécial doit être de catégorie matériel : " + produitNom);
            }
            int qteToDeduct;
            try {
                qteToDeduct = Integer.parseInt(qte);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Quantité invalide pour le produit spécial : " + produitNom);
            }
            if (qteToDeduct <= 0) {
                continue; // Skip if quantity is zero or negative
            }
            if (produit.getQteDisponible() < qteToDeduct) {
                throw new RuntimeException("Stock insuffisant pour le produit spécial : " + produitNom);
            }
            produit.setQteDisponible(produit.getQteDisponible() - qteToDeduct);
            stockService.updateProduit(produit);

            // Enregistrer dans ProduitLog
            ProduitLog log = new ProduitLog();
            log.setUser(currentUser);
            log.setProduit(produit);
            log.setAction("UTILISATION_SPECIAL");
            log.setDetails("Produit spécial utilisé dans une séance - Quantité diminuée de " + qteToDeduct);
            log.setDateAction(LocalDateTime.now());
            produitLogRepository.save(log);

            // Enregistrer dans DetailSeanceProduitSpecialStandard
            DetailSeanceProduitSpecialStandard detail = new DetailSeanceProduitSpecialStandard();
            detail.setSeance(seance);
            detail.setProduit(produit);
            detail.setQteAdministre(qte);
            detail.setDateTemps(LocalDateTime.now());
            detail.setNomProduit(produit.getNom());
            detail.setStandard(false);
            detail.setObservation(OBSERVATION_PRODUITS_SPECIAUX); // Set observation
            details.add(detailProduitRepository.save(detail));
        }

        return details;
    }

    /**
     * Retrieves sessions for a specific date.
     * @param date The date to filter sessions by (YYYY-MM-DD).
     * @return List of sessions on the specified date.
     */
    public List<Seance> getSeancesByDate(String date) {
        LocalDate localDate = LocalDate.parse(date);
        LocalDateTime startDate = localDate.atStartOfDay();
        LocalDateTime endDate = localDate.atTime(23, 59, 59);
        logger.info("Fetching sessions for date: {} (from {} to {})", date, startDate, endDate);
        List<Seance> result = seanceRepository.findByDateRange(startDate, endDate);
        logger.info("Found {} sessions for date: {}", result.size(), date);
        return result;
    }

    /**
     * Retrieves sessions for a specific patient, optionally filtered by date range.
     * If patientId is null, retrieves all sessions ordered by idSeance descending.
     * @param patientId The ID of the patient, or null for all sessions.
     * @param startDate Optional start date for filtering.
     * @param endDate Optional end date for filtering.
     * @return List of sessions.
     */
    public List<Seance> getSeancesByPatient(Long patientId, LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Fetching sessions for patient ID: {}, startDate: {}, endDate: {}", 
            patientId != null ? patientId : "null", 
            startDate != null ? startDate.toString() : "null", 
            endDate != null ? endDate.toString() : "null");
        
        List<Seance> result;
        if (patientId == null) {
            logger.debug("No patient ID provided, fetching all sessions");
            result = seanceRepository.findAllOrderByIdSeanceDesc();
        } else if (startDate != null && endDate != null) {
            logger.debug("Fetching sessions for patient ID {} with date range {} to {}", 
                patientId, startDate, endDate);
            result = seanceRepository.findByPatientIdAndDateRange(patientId, startDate, endDate);
        } else {
            logger.debug("Fetching sessions for patient ID {} without date range", patientId);
            result = seanceRepository.findByPatientIdPatientOrderByDateDesc(patientId);
        }
        
        logger.info("Found {} sessions", result.size());
        return result;
    }

    /**
     * Retrieves sessions by date range, optionally filtered by patient.
     * @param patientId The ID of the patient, or null for all patients.
     * @param startDate Start date for filtering.
     * @param endDate End date for filtering.
     * @return List of sessions within the date range, optionally for a specific patient.
     */
    public List<Seance> getSeancesByDateRange(Long patientId, LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Fetching sessions for patient ID: {}, date range: {} to {}", 
            patientId != null ? patientId : "null", startDate, endDate);
        
        List<Seance> result;
        if (patientId == null) {
            logger.debug("No patient ID provided, fetching sessions for date range {} to {}", startDate, endDate);
            result = seanceRepository.findByDateRange(startDate, endDate);
        } else {
            logger.debug("Fetching sessions for patient ID {} with date range {} to {}", 
                patientId, startDate, endDate);
            result = seanceRepository.findByPatientIdAndDateRange(patientId, startDate, endDate);
        }
        
        logger.info("Found {} sessions for date range", result.size());
        return result;
    }
}