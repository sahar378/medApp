package privateApp.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import privateApp.dtos.ProduitAlerteDTO;
import privateApp.models.*;
import privateApp.repositories.CategorieRepository;
import privateApp.repositories.ProduitLogRepository;
import privateApp.repositories.ProduitRepository;
import privateApp.repositories.LivraisonRepository;
import privateApp.repositories.NotificationRepository;
import privateApp.repositories.UserRepository;
import privateApp.repositories.PatientRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StockService {

    private static final Logger logger = LoggerFactory.getLogger(StockService.class);

    @Autowired
    private ProduitRepository produitRepository;
    
    @Autowired
    private ProduitLogRepository produitLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public Produit addProduit(Produit produit) {
        Optional<Produit> existingProduit = produitRepository.findByNomAndCategorieIdCategorie(
            produit.getNom(), produit.getCategorie().getIdCategorie()
        );

        if (existingProduit.isPresent()) {
            Produit archivedProduit = existingProduit.get();
            if (archivedProduit.isArchive()) {
                archivedProduit.setArchive(false);
                archivedProduit.setDescription(produit.getDescription());
                archivedProduit.setQteDisponible(produit.getQteDisponible());
                archivedProduit.setDateExpiration(produit.getDateExpiration());
                Produit updatedProduit = produitRepository.save(archivedProduit);
                definirSeuilAlerteAutomatique(updatedProduit.getIdProduit()); // Définir seuil automatiquement
                logAction(updatedProduit, "UNARCHIVE", "Produit désarchivé et mis à jour");
                return updatedProduit;
            } else {
                throw new IllegalArgumentException("Un produit actif avec ce nom existe déjà.");
            }
        }

        Produit savedProduit = produitRepository.save(produit);
        definirSeuilAlerteAutomatique(savedProduit.getIdProduit()); // Définir seuil automatiquement
        logAction(savedProduit, "CREATE", "Produit créé - Quantité: " + savedProduit.getQteDisponible());
        return savedProduit;
    }

    public Produit updateProduit(Produit produit) {
        Produit existingProduit = produitRepository.findById(produit.getIdProduit())
            .orElseThrow(() -> new RuntimeException("Produit non trouvé pour mise à jour"));

        int ancienneQuantite = existingProduit.getQteDisponible();
        Date ancienneDateExpiration = existingProduit.getDateExpiration();

        existingProduit.setNom(produit.getNom());
        existingProduit.setDescription(produit.getDescription());
        existingProduit.setQteDisponible(produit.getQteDisponible());
        existingProduit.setDateExpiration(produit.getDateExpiration());
        existingProduit.setCategorie(produit.getCategorie());

        Produit updatedProduit = produitRepository.save(existingProduit);
        definirSeuilAlerteAutomatique(updatedProduit.getIdProduit()); // Définir seuil automatiquement

        StringBuilder logDetails = new StringBuilder();
        if (ancienneQuantite != updatedProduit.getQteDisponible()) {
            logDetails.append("Quantité: ").append(ancienneQuantite).append(" -> ").append(updatedProduit.getQteDisponible()).append(", ");
        }
        if (ancienneDateExpiration != null && updatedProduit.getDateExpiration() != null) {
            String ancienneDateStr = DATE_FORMAT.format(ancienneDateExpiration);
            String nouvelleDateStr = DATE_FORMAT.format(updatedProduit.getDateExpiration());
            if (!ancienneDateStr.equals(nouvelleDateStr)) {
                logDetails.append("Date d'expiration: ").append(ancienneDateStr).append(" -> ").append(nouvelleDateStr);
            }
        }

        if (logDetails.length() > 0 && logDetails.lastIndexOf(", ") == logDetails.length() - 2) {
            logDetails.setLength(logDetails.length() - 2);
        }

        if (logDetails.length() > 0) {
            logAction(updatedProduit, "UPDATE", logDetails.toString());
        }

        return updatedProduit;
    }

    public Optional<Produit> getProduitByNom(String nom) {
    	return produitRepository.findByNomIgnoreCaseAndArchiveFalse(nom);
    }

    public List<Produit> getProduits() {
        return produitRepository.findByArchiveFalse();
    }
    
    public List<Produit> getProduitsByCategorie(Long idCategorie) {
        return produitRepository.findByArchiveFalseAndCategorieIdCategorie(idCategorie);
    }

    public List<Produit> getAllProduits() {
        return produitRepository.findAll();
    }
    
    public Produit getProduitById(Long produitId) {
        return produitRepository.findById(produitId)
            .orElseThrow(() -> new RuntimeException("Produit non trouvé"));
    }

    public void definirSeuilAlerte(Long produitId, int nombreMalades) {
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé"));
        if ("medicament".equalsIgnoreCase(produit.getCategorie().getLibelleCategorie())) {
            produit.setSeuilAlerte(calculerSeuilMedicament());
        } else if ("materiel".equalsIgnoreCase(produit.getCategorie().getLibelleCategorie())) {
            if (produit.isStandard() || produit.getNom().equals("Sérum salé 1L")) {
                produit.setSeuilAlerte(calculerSeuilMateriel(nombreMalades));
            } else if (produit.getNom().equals("Sérum salé 0.5L")) {
                produit.setSeuilAlerte(calculerSeuilMateriel(nombreMalades) * 2);
            } else {
                produit.setSeuilAlerte(5);
            }
        }
        produitRepository.save(produit);
        logger.info("Seuil alerte mis à jour pour produit ID: {}, Seuil: {}", produitId, produit.getSeuilAlerte());
    }

    public void definirSeuilsCategorie(int idCategorie, int nombreMalades) {
        List<Produit> produits = produitRepository.findByCategorieIdCategorie(idCategorie);
        for (Produit produit : produits) {
            if (idCategorie == 1) { // Materiel
                if (produit.isStandard() || produit.getNom().equals("Sérum salé 1L")) {
                    produit.setSeuilAlerte(calculerSeuilMateriel(nombreMalades));
                } else if (produit.getNom().equals("Sérum salé 0.5L")) {
                    produit.setSeuilAlerte(calculerSeuilMateriel(nombreMalades) * 2);
                } else {
                    produit.setSeuilAlerte(5);
                }
            } else if (idCategorie == 2) { // Medicament
                produit.setSeuilAlerte(calculerSeuilMedicament());
            }
            produitRepository.save(produit);
            logger.info("Seuil alerte mis à jour pour produit ID: {}, Seuil: {}", produit.getIdProduit(), produit.getSeuilAlerte());
        }
    }

    private int calculerNombreMalades() {
        List<Patient> activePatients = patientRepository.findByActifTrueAndArchiveFalse();
        int nombreMalades = activePatients.size();
        logger.info("Nombre de malades calculé: {}", nombreMalades);
        return nombreMalades;
    }

    public void definirSeuilAlerteAutomatique(Long produitId) {
        int nombreMalades = calculerNombreMalades();
        definirSeuilAlerte(produitId, nombreMalades);
    }

    public void definirSeuilsCategorieAutomatique(int idCategorie) {
        int nombreMalades = calculerNombreMalades();
        definirSeuilsCategorie(idCategorie, nombreMalades);
    }

    public List<ProduitAlerteDTO> verifierAlertes() {
        List<Produit> produitsActifs = produitRepository.findByArchiveFalse();
        Date today = new Date();

        return produitsActifs.stream()
            .map(produit -> {
                List<String> messages = new ArrayList<>();

                if (produit.getCategorie().getIdCategorie() == 1) { // Matériel
                    if (produit.isStandard() || produit.getNom().equals("Sérum salé 1L") || produit.getNom().equals("Sérum salé 0.5L")) {
                        if (produit.getQteDisponible() <= produit.getSeuilAlerte()) {
                            messages.add(String.format("%s - Quantité: %d (Seuil: %d)",
                                produit.getNom(), produit.getQteDisponible(), produit.getSeuilAlerte()));
                        }
                    } else {
                        if (produit.getQteDisponible() <= 5) {
                            messages.add(String.format("%s - Quantité: %d (Alerte à 5 unités)",
                                produit.getNom(), produit.getQteDisponible()));
                        }
                    }
                } else if (produit.getCategorie().getIdCategorie() == 2) { // Médicaments
                    boolean isExpired = produit.getDateExpiration() != null && produit.getDateExpiration().before(today);
                    boolean isOneLeft = produit.getQteDisponible() == 1;
                    boolean isLowStock = produit.getQteDisponible() <= produit.getSeuilAlerte() && !isOneLeft;

                    if (isExpired) {
                        messages.add(String.format("%s - Expiré le: %s",
                            produit.getNom(), DATE_FORMAT.format(produit.getDateExpiration())));
                    }
                    if (isOneLeft) {
                        String expirationDate = produit.getDateExpiration() != null
                            ? DATE_FORMAT.format(produit.getDateExpiration())
                            : "-";
                        messages.add(String.format("%s - Quantité restante: 1 (Expire le: %s)",
                            produit.getNom(), expirationDate));
                    } else if (isLowStock) {
                        messages.add(String.format("%s - Quantité: %d (Seuil: %d)",
                            produit.getNom(), produit.getQteDisponible(), produit.getSeuilAlerte()));
                    }
                }

                return messages.isEmpty() ? null : new ProduitAlerteDTO(produit, messages);
            })
            .filter(dto -> dto != null)
            .collect(Collectors.toList());
    }

    public List<ProduitAlerteDTO> verifierAlertesMedicaments() {
        List<Produit> produitsActifs = produitRepository.findByArchiveFalseAndCategorieIdCategorie(2L);
        Date today = new Date();

        return produitsActifs.stream()
            .map(produit -> {
                List<String> messages = new ArrayList<>();

                boolean isExpired = produit.getDateExpiration() != null && produit.getDateExpiration().before(today);
                boolean isOneLeft = produit.getQteDisponible() == 1;
                boolean isLowStock = produit.getQteDisponible() <= produit.getSeuilAlerte() && !isOneLeft;

                if (isExpired) {
                    messages.add(String.format("%s - Expiré le: %s",
                        produit.getNom(), DATE_FORMAT.format(produit.getDateExpiration())));
                }
                if (isOneLeft) {
                    String expirationDate = produit.getDateExpiration() != null
                        ? DATE_FORMAT.format(produit.getDateExpiration())
                        : "-";
                    messages.add(String.format("%s - Quantité restante: 1 (Expire le: %s)",
                        produit.getNom(), expirationDate));
                } else if (isLowStock) {
                    messages.add(String.format("%s - Quantité: %d (Seuil: %d)",
                        produit.getNom(), produit.getQteDisponible(), produit.getSeuilAlerte()));
                }

                return messages.isEmpty() ? null : new ProduitAlerteDTO(produit, messages);
            })
            .filter(dto -> dto != null)
            .collect(Collectors.toList());
    }

    public List<ProduitAlerteDTO> verifierAlertesMateriels() {
        List<Produit> produitsActifs = produitRepository.findByArchiveFalseAndCategorieIdCategorie(1L);
        return produitsActifs.stream()
            .map(produit -> {
                List<String> messages = new ArrayList<>();

                if (produit.isStandard() || produit.getNom().equals("Sérum salé 1L") || produit.getNom().equals("Sérum salé 0.5L")) {
                    if (produit.getQteDisponible() <= produit.getSeuilAlerte()) {
                        messages.add(String.format("%s - Quantité: %d (Seuil: %d)",
                            produit.getNom(), produit.getQteDisponible(), produit.getSeuilAlerte()));
                    }
                } else {
                    if (produit.getQteDisponible() <= 5) {
                        messages.add(String.format("%s - Quantité: %d (Alerte à 5 unités)",
                            produit.getNom(), produit.getQteDisponible()));
                    }
                }

                return messages.isEmpty() ? null : new ProduitAlerteDTO(produit, messages);
            })
            .filter(dto -> dto != null)
            .collect(Collectors.toList());
    }

    public void deleteProduit(Long produitId) {
        Produit produit = produitRepository.findById(produitId)
            .orElseThrow(() -> new RuntimeException("Produit non trouvé"));
        produit.setArchive(true);
        produitRepository.save(produit);
        logAction(produit, "ARCHIVE", "Produit archivé - Quantité: " + produit.getQteDisponible());
    }

    private void logAction(Produit produit, String action, String details) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ProduitLog log = new ProduitLog();
        log.setUser(user);
        log.setProduit(produit);
        log.setAction(action);
        log.setDetails(details);
        produitLogRepository.save(log);
    }

    public int calculerSeuilMateriel(int nombreMalades) {
        int seancesParMois = 13;
        int moisCommande = 3;
        int consommationMensuelle = nombreMalades * seancesParMois;
        return consommationMensuelle / 2;
    }

    public int calculerSeuilMedicament() {
        return 1;
    }

    public List<Produit> getArchivedProduits() {
        return produitRepository.findByArchiveTrue();
    }

    public List<Produit> getActiveMedicaments() {
        return produitRepository.findByArchiveFalseAndCategorieIdCategorie(2L);
    }
    
    public List<Produit> getActiveMateriels() {
        return produitRepository.findByArchiveFalseAndCategorieIdCategorie(1L);
    }

    public List<Produit> getArchivedMedicaments() {
        return produitRepository.findByArchiveTrueAndCategorieIdCategorie(2L);
    }
    
    public List<Produit> getArchivedMateriels() {
        return produitRepository.findByArchiveTrueAndCategorieIdCategorie(1L);
    }

    public List<Produit> getProduitsForInventaire() {
        return produitRepository.findByArchiveFalse();
    }
}