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
    
    private static final List<String> PRODUITS_NON_STANDARDS_AFFECTANT_STOCK = Arrays.asList(
        "Sérum salé 0.5L", "Seringue 10cc", "Seringue 5cc", "Robinet",
        "Transfuseur", "Lame bistouri", "Compress", "Gants", "Masque O2", "Lunette O2",
        "Fils de suture", "Bandelette", "Filtre (dialyseur)", "ligne artérielle",
        "ligne veineuse", "2 aiguilles", "champ stérile"
    );

    private static final List<String> PRODUITS_NON_STANDARDS_SANS_STOCK = Arrays.asList(
        "Héparine", "Ether", "Néofix"
    );

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

    @Transactional
    public Seance createSeance(Seance seance, String serumSaleChoix) {
        Machine machine = seance.getMachine();
        if (machine == null || machine.getDisponibilite() != 0 || machine.isArchived()) {
            throw new RuntimeException("La machine sélectionnée n'est pas disponible");
        }

        if (seance.getDebutDialyse() != null && seance.getFinDialyse() != null) {
            seance.setDureeSeance((int) Duration.between(seance.getDebutDialyse(), seance.getFinDialyse()).toMinutes());
        }
        if (seance.getPoidsEntree() != null && seance.getPoidsSortie() != null) {
            seance.setPertePoids(seance.getPoidsEntree() - seance.getPoidsSortie());
        }

        Seance savedSeance = seanceRepository.save(seance);
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!serumSaleChoix.equals("Sérum salé 1L") && !serumSaleChoix.equals("Sérum salé 0.5L")) {
            throw new RuntimeException("Choix de Sérum salé invalide");
        }
        Produit serumSale = stockService.getProduitByNom(serumSaleChoix)
                .orElseThrow(() -> new RuntimeException("Sérum salé non trouvé : " + serumSaleChoix));
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
        
        ProduitLog logSerum = new ProduitLog();
        logSerum.setUser(currentUser);
        logSerum.setProduit(serumSale);
        logSerum.setAction("UTILISATION");
        logSerum.setDetails("Produit utilisé dans une séance - Quantité diminuée de " + qteAdministreSerum);
        logSerum.setDateAction(LocalDateTime.now());
        produitLogRepository.save(logSerum);

        DetailSeanceProduitSpecialStandard detailSerum = new DetailSeanceProduitSpecialStandard();
        detailSerum.setSeance(savedSeance);
        detailSerum.setProduit(serumSale);
        detailSerum.setQteAdministre(String.valueOf(qteAdministreSerum));
        detailSerum.setDateTemps(seance.getDate());
        detailSerum.setNomProduit(serumSale.getNom());
        detailSerum.setStandard(true);
        detailSerum.setObservation(OBSERVATION_PRODUITS_STANDARDS);
        detailProduitRepository.save(detailSerum);

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

            ProduitLog log = new ProduitLog();
            log.setUser(currentUser);
            log.setProduit(produit);
            log.setAction("UTILISATION");
            log.setDetails("Produit utilisé dans une séance - Quantité diminuée de 1");
            log.setDateAction(LocalDateTime.now());
            produitLogRepository.save(log);

            DetailSeanceProduitSpecialStandard detail = new DetailSeanceProduitSpecialStandard();
            detail.setSeance(savedSeance);
            detail.setProduit(produit);
            detail.setQteAdministre("1");
            detail.setDateTemps(seance.getDate());
            detail.setNomProduit(produit.getNom());
            detail.setStandard(true);
            detail.setObservation(OBSERVATION_PRODUITS_STANDARDS);
            detailProduitRepository.save(detail);
        }

        return savedSeance;
    }

    @Transactional
    public List<DetailSeanceProduitSpecialStandard> addProduitNonStandard(Long seanceId,
            Map<String, String> produitsNonStandards, Map<String, String> produitsSansStock,
            Map<String, String> produitsHorsStock, Map<String, String> produitsSpeciaux) {
        Seance seance = seanceRepository.findById(seanceId)
                .orElseThrow(() -> new RuntimeException("Séance non trouvée"));
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<DetailSeanceProduitSpecialStandard> details = new java.util.ArrayList<>();

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
                continue;
            }
            if (produit.getQteDisponible() < qteToDeduct) {
                throw new RuntimeException("Stock insuffisant pour le produit : " + produitNom);
            }
            produit.setQteDisponible(produit.getQteDisponible() - qteToDeduct);
            stockService.updateProduit(produit);

            ProduitLog log = new ProduitLog();
            log.setUser(currentUser);
            log.setProduit(produit);
            log.setAction("UTILISATION");
            log.setDetails("Produit utilisé dans une séance - Quantité diminuée de " + qteToDeduct);
            log.setDateAction(LocalDateTime.now());
            produitLogRepository.save(log);

            DetailSeanceProduitSpecialStandard detail = new DetailSeanceProduitSpecialStandard();
            detail.setSeance(seance);
            detail.setProduit(produit);
            detail.setQteAdministre(qte);
            detail.setDateTemps(LocalDateTime.now());
            detail.setNomProduit(produit.getNom());
            detail.setStandard(false);
            detail.setObservation(OBSERVATION_PRODUITS_NON_STANDARDS_AFFECTANT_STOCK);
            details.add(detailProduitRepository.save(detail));
        }

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
                continue;
            }
            DetailSeanceProduitSpecialStandard detail = new DetailSeanceProduitSpecialStandard();
            detail.setSeance(seance);
            detail.setProduit(null);
            detail.setQteAdministre(qte);
            detail.setDateTemps(LocalDateTime.now());
            detail.setNomProduit(produitNom);
            detail.setStandard(false);
            detail.setObservation(OBSERVATION_PRODUITS_NON_STANDARDS_SANS_STOCK);
            details.add(detailProduitRepository.save(detail));
        }

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
                continue;
            }
            DetailSeanceProduitSpecialStandard detail = new DetailSeanceProduitSpecialStandard();
            detail.setSeance(seance);
            detail.setProduit(null);
            detail.setQteAdministre(qte);
            detail.setDateTemps(LocalDateTime.now());
            detail.setNomProduit(produitNom);
            detail.setStandard(false);
            detail.setObservation(OBSERVATION_PRODUITS_HORS_STOCK);
            details.add(detailProduitRepository.save(detail));
        }

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
                continue;
            }
            if (produit.getQteDisponible() < qteToDeduct) {
                throw new RuntimeException("Stock insuffisant pour le produit spécial : " + produitNom);
            }
            produit.setQteDisponible(produit.getQteDisponible() - qteToDeduct);
            stockService.updateProduit(produit);

            ProduitLog log = new ProduitLog();
            log.setUser(currentUser);
            log.setProduit(produit);
            log.setAction("UTILISATION_SPECIAL");
            log.setDetails("Produit spécial utilisé dans une séance - Quantité diminuée de " + qteToDeduct);
            log.setDateAction(LocalDateTime.now());
            produitLogRepository.save(log);

            DetailSeanceProduitSpecialStandard detail = new DetailSeanceProduitSpecialStandard();
            detail.setSeance(seance);
            detail.setProduit(produit);
            detail.setQteAdministre(qte);
            detail.setDateTemps(LocalDateTime.now());
            detail.setNomProduit(produit.getNom());
            detail.setStandard(false);
            detail.setObservation(OBSERVATION_PRODUITS_SPECIAUX);
            details.add(detailProduitRepository.save(detail));
        }

        return details;
    }

    @Transactional
    public List<DetailSeanceProduitSpecialStandard> updateProduitNonStandard(Long seanceId,
            Map<String, String> produitsNonStandards, Map<String, String> produitsSansStock,
            Map<String, String> produitsHorsStock, Map<String, String> produitsSpeciaux) {
        Seance seance = seanceRepository.findById(seanceId)
                .orElseThrow(() -> new RuntimeException("Séance non trouvée"));
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<DetailSeanceProduitSpecialStandard> details = new java.util.ArrayList<>();

        // Fetch existing non-standard products for the session
        List<DetailSeanceProduitSpecialStandard> existingProducts = detailProduitRepository.findBySeanceIdSeance(seanceId);
        // Revert stock for non-standard products only
        for (DetailSeanceProduitSpecialStandard existing : existingProducts) {
            if (!existing.isStandard()) { // Skip standard products like Sérum salé
                Produit produit = existing.getProduit();
                if (produit != null) { // Products with stock impact
                    int qteAdmin;
                    try {
                        qteAdmin = Integer.parseInt(existing.getQteAdministre());
                    } catch (NumberFormatException e) {
                        logger.warn("Quantité invalide pour le produit {}: {}", existing.getNomProduit(), existing.getQteAdministre());
                        continue;
                    }
                    produit.setQteDisponible(produit.getQteDisponible() + qteAdmin);
                    stockService.updateProduit(produit);

                    ProduitLog log = new ProduitLog();
                    log.setUser(currentUser);
                    log.setProduit(produit);
                    log.setAction("RESTAURATION");
                    log.setDetails("Stock restauré pour modification de séance - Quantité augmentée de " + qteAdmin);
                    log.setDateAction(LocalDateTime.now());
                    produitLogRepository.save(log);
                }
            }
        }

        // Delete existing non-standard products
        detailProduitRepository.deleteBySeanceIdSeanceAndStandardFalse(seanceId);

        // Add new non-standard products (same logic as addProduitNonStandard)
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
                continue;
            }
            if (produit.getQteDisponible() < qteToDeduct) {
                throw new RuntimeException("Stock insuffisant pour le produit : " + produitNom);
            }
            produit.setQteDisponible(produit.getQteDisponible() - qteToDeduct);
            stockService.updateProduit(produit);

            ProduitLog log = new ProduitLog();
            log.setUser(currentUser);
            log.setProduit(produit);
            log.setAction("UTILISATION");
            log.setDetails("Produit utilisé dans une séance modifiée - Quantité diminuée de " + qteToDeduct);
            log.setDateAction(LocalDateTime.now());
            produitLogRepository.save(log);

            DetailSeanceProduitSpecialStandard detail = new DetailSeanceProduitSpecialStandard();
            detail.setSeance(seance);
            detail.setProduit(produit);
            detail.setQteAdministre(qte);
            detail.setDateTemps(LocalDateTime.now());
            detail.setNomProduit(produit.getNom());
            detail.setStandard(false);
            detail.setObservation(OBSERVATION_PRODUITS_NON_STANDARDS_AFFECTANT_STOCK);
            details.add(detailProduitRepository.save(detail));
        }

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
                continue;
            }
            DetailSeanceProduitSpecialStandard detail = new DetailSeanceProduitSpecialStandard();
            detail.setSeance(seance);
            detail.setProduit(null);
            detail.setQteAdministre(qte);
            detail.setDateTemps(LocalDateTime.now());
            detail.setNomProduit(produitNom);
            detail.setStandard(false);
            detail.setObservation(OBSERVATION_PRODUITS_NON_STANDARDS_SANS_STOCK);
            details.add(detailProduitRepository.save(detail));
        }

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
                continue;
            }
            DetailSeanceProduitSpecialStandard detail = new DetailSeanceProduitSpecialStandard();
            detail.setSeance(seance);
            detail.setProduit(null);
            detail.setQteAdministre(qte);
            detail.setDateTemps(LocalDateTime.now());
            detail.setNomProduit(produitNom);
            detail.setStandard(false);
            detail.setObservation(OBSERVATION_PRODUITS_HORS_STOCK);
            details.add(detailProduitRepository.save(detail));
        }

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
                continue;
            }
            if (produit.getQteDisponible() < qteToDeduct) {
                throw new RuntimeException("Stock insuffisant pour le produit spécial : " + produitNom);
            }
            produit.setQteDisponible(produit.getQteDisponible() - qteToDeduct);
            stockService.updateProduit(produit);

            ProduitLog log = new ProduitLog();
            log.setUser(currentUser);
            log.setProduit(produit);
            log.setAction("UTILISATION_SPECIAL");
            log.setDetails("Produit spécial utilisé dans une séance modifiée - Quantité diminuée de " + qteToDeduct);
            log.setDateAction(LocalDateTime.now());
            produitLogRepository.save(log);

            DetailSeanceProduitSpecialStandard detail = new DetailSeanceProduitSpecialStandard();
            detail.setSeance(seance);
            detail.setProduit(produit);
            detail.setQteAdministre(qte);
            detail.setDateTemps(LocalDateTime.now());
            detail.setNomProduit(produit.getNom());
            detail.setStandard(false);
            detail.setObservation(OBSERVATION_PRODUITS_SPECIAUX);
            details.add(detailProduitRepository.save(detail));
        }

        return details;
    }

    public List<Seance> getSeancesByDate(String date) {
        LocalDate localDate = LocalDate.parse(date);
        LocalDateTime startDate = localDate.atStartOfDay();
        LocalDateTime endDate = localDate.atTime(23, 59, 59);
        logger.info("Fetching sessions for date: {} (from {} to {})", date, startDate, endDate);
        List<Seance> result = seanceRepository.findByDateRange(startDate, endDate);
        logger.info("Found {} sessions for date: {}", result.size(), date);
        return result;
    }
    
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