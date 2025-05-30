package privateApp.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import privateApp.models.Patient;
import privateApp.models.PatientDebutFin;
import privateApp.models.Produit;
import privateApp.repositories.PatientDebutFinRepository;
import privateApp.repositories.PatientRepository;
import privateApp.repositories.ProduitRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service pour gérer les opérations sur les patients.
 * Inclut la création, la mise à jour, l'archivage, l'activation/désactivation, et la recherche.
 */
@Service
public class PatientService {

    private static final Logger logger = LoggerFactory.getLogger(PatientService.class);

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PatientDebutFinRepository patientDebutFinRepository;

    @Autowired
    private ProduitRepository produitRepository;

    /**
     * Crée un nouveau patient, enregistre sa date de début de dialyse et associe les produits standards.
     * @param patient Les données du patient.
     * @return Le patient créé.
     */
    @Transactional
    public Patient createPatient(Patient patient) {
        logger.info("Création d'un nouveau patient : {} {}", patient.getNom(), patient.getPrenom());
        patient.setActif(true);
        patient.setArchive(false);
        Patient savedPatient = patientRepository.save(patient);

        // Créer un enregistrement dans PatientDebutFin
        PatientDebutFin debutFin = new PatientDebutFin();
        debutFin.setPatient(savedPatient);
        debutFin.setDateDebutDialyse(LocalDate.now());
        patientDebutFinRepository.save(debutFin);

        // Associer tous les produits standards au patient
        List<Produit> produitsStandards = produitRepository.findByStandardTrue();
        savedPatient.getProduitsStandards().addAll(produitsStandards);
        patientRepository.save(savedPatient);

        return savedPatient;
    }

    /**
     * Met à jour les informations d'un patient existant.
     * @param id L'ID du patient.
     * @param patientDetails Les nouvelles informations.
     * @return Le patient mis à jour.
     * @throws RuntimeException si le patient est archivé.
     */
    @Transactional
    public Patient updatePatient(Long id, Patient patientDetails) {
        logger.info("Mise à jour du patient ID: {}", id);
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient non trouvé"));
        if (patient.isArchive()) {
            throw new RuntimeException("Impossible de modifier un patient archivé");
        }
        patient.setNom(patientDetails.getNom());
        patient.setPrenom(patientDetails.getPrenom());
        patient.setDateNaissance(patientDetails.getDateNaissance());
        patient.setDomicile(patientDetails.getDomicile());
        patient.setCarnetSoin(patientDetails.getCarnetSoin());
        patient.setGroupeSanguin(patientDetails.getGroupeSanguin());
        patient.setNumeroTelephone(patientDetails.getNumeroTelephone());
        patient.setHistoriqueMaladie(patientDetails.getHistoriqueMaladie());
        patient.setAntecedent(patientDetails.getAntecedent());
        patient.setEvolution(patientDetails.getEvolution());
        patient.setTraitement(patientDetails.getTraitement());
        return patientRepository.save(patient);
    }

    /**
     * Archive un patient et le désactive.
     * @param id L'ID du patient.
     * @throws RuntimeException si le patient est déjà archivé.
     */
    @Transactional
    public void archivePatient(Long id) {
        logger.info("Archivage du patient ID: {}", id);
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient non trouvé"));
        if (patient.isArchive()) {
            throw new RuntimeException("Le patient est déjà archivé");
        }
        patient.setArchive(true);
        patient.setActif(false);
        
        // Vider la liste des produits standards associés
        patient.getProduitsStandards().clear();
        
        PatientDebutFin lastRecord = patientDebutFinRepository.findTopByPatientOrderByIdPatDFDesc(patient)
                .orElseThrow(() -> new RuntimeException("Aucun enregistrement debut-fin trouvé"));
        if (lastRecord.getDateFinDialyse() == null) {
            lastRecord.setDateFinDialyse(LocalDate.now());
            patientDebutFinRepository.save(lastRecord);
        }
    }

    /**
     * Active ou désactive un patient et met à jour les périodes de dialyse.
     * @param id L'ID du patient.
     * @return Le patient mis à jour.
     * @throws RuntimeException si le patient est archivé.
     */
    @Transactional
    public Patient togglePatientActif(Long id) {
        logger.info("Changement d'état actif du patient ID: {}", id);
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient non trouvé"));
        if (patient.isArchive()) {
            throw new RuntimeException("Impossible de modifier l'état actif d'un patient archivé");
        }
        patient.setActif(!patient.isActif());
        if (!patient.isActif()) {
            PatientDebutFin lastRecord = patientDebutFinRepository.findTopByPatientOrderByIdPatDFDesc(patient)
                    .orElseThrow(() -> new RuntimeException("Aucun enregistrement debut-fin trouvé"));
            if (lastRecord.getDateFinDialyse() == null) {
                lastRecord.setDateFinDialyse(LocalDate.now());
                patientDebutFinRepository.save(lastRecord);
            }
        } else {
            PatientDebutFin newRecord = new PatientDebutFin();
            newRecord.setPatient(patient);
            newRecord.setDateDebutDialyse(LocalDate.now());
            patientDebutFinRepository.save(newRecord);
        }
        return patientRepository.save(patient);
    }

    /**
     * Récupère tous les patients non archivés.
     * @return Liste des patients non archivés.
     */
    public List<Patient> getAllPatients() {
        logger.info("Récupération de tous les patients non archivés");
        return patientRepository.findByArchiveFalse();
    }

    /**
     * Récupère tous les patients archivés.
     * @return Liste des patients archivés.
     */
    public List<Patient> getArchivedPatients() {
        logger.info("Récupération de tous les patients archivés");
        return patientRepository.findByArchiveTrue();
    }

    /**
     * Récupère un patient par son ID.
     * @param id L'ID du patient.
     * @return Le patient correspondant.
     * @throws RuntimeException si le patient n'existe pas ou est archivé.
     */
    public Patient getPatientById(Long id) {
        logger.info("Récupération du patient ID: {}", id);
        return patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient non trouvé"));
    }

    /**
     * Recherche des patients actifs et non archivés par nom.
     * @param nom Le nom ou une partie du nom.
     * @return Liste des patients actifs et non archivés correspondants.
     */
    public List<Patient> searchActiveNonArchivedPatientsByNom(String nom) {
        logger.info("Recherche des patients actifs et non archivés par nom: {}", nom);
        return patientRepository.findByNomContainingIgnoreCaseAndActifTrueAndArchiveFalse(nom);
    }

    /**
     * Recherche des patients non actifs et non archivés par nom.
     * @param nom Le nom ou une partie du nom.
     * @return Liste des patients non actifs et non archivés correspondants.
     */
    public List<Patient> searchInactiveNonArchivedPatientsByNom(String nom) {
        logger.info("Recherche des patients non actifs et non archivés par nom: {}", nom);
        return patientRepository.findByNomContainingIgnoreCaseAndActifFalseAndArchiveFalse(nom);
    }

    /**
     * Recherche des patients archivés par nom.
     * @param nom Le nom ou une partie du nom.
     * @return Liste des patients archivés correspondants.
     */
    public List<Patient> searchArchivedPatientsByNom(String nom) {
        logger.info("Recherche des patients archivés par nom: {}", nom);
        return patientRepository.findByNomContainingIgnoreCaseAndArchiveTrue(nom);
    }

    /**
     * Récupère les patients actifs et non archivés.
     * @return Liste des patients actifs et non archivés.
     */
    public List<Patient> getActivePatients() {
        logger.info("Récupération des patients actifs et non archivés");
        return patientRepository.findByActifTrueAndArchiveFalse();
    }
    
    /**
     * Restaure un patient archivé en le rendant non archivé et actif.
     * Crée un nouvel enregistrement PatientDebutFin pour refléter la reprise de la dialyse.
     * Vérifie que les produits standards associés sont toujours valides.
     * @param id L'ID du patient.
     * @throws RuntimeException si le patient n'est pas archivé ou n'existe pas.
     */
    @Transactional
    public void unarchivePatient(Long id) {
        logger.info("Désarchivage du patient ID: {}", id);
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient non trouvé"));
        if (!patient.isArchive()) {
            throw new RuntimeException("Le patient n'est pas archivé");
        }

        // Recréer la liste des produits standards comme pour un nouveau patient
        List<Produit> produitsStandards = produitRepository.findByStandardTrue();
        patient.getProduitsStandards().clear(); // Assurer que la liste est vide
        patient.getProduitsStandards().addAll(produitsStandards);

        patient.setArchive(false);
        patient.setActif(true);
        PatientDebutFin newRecord = new PatientDebutFin();
        newRecord.setPatient(patient);
        newRecord.setDateDebutDialyse(LocalDate.now());
        patientDebutFinRepository.save(newRecord);
        patientRepository.save(patient);
    }
    
    /**
     * Récupère les patients non actifs et non archivés.
     * @return Liste des patients non actifs et non archivés.
     */
    public List<Patient> getInactiveNonArchivedPatients() {
        logger.info("Récupération des patients non actifs et non archivés");
        return patientRepository.findByActifFalseAndArchiveFalse();
    }
    
    public List<Patient> getNonArchivedPatients() {
        return patientRepository.findByArchiveFalse();
    }
}