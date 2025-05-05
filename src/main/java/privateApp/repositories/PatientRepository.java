package privateApp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import privateApp.models.Patient;
import privateApp.models.Produit;

import java.util.List;

/**
 * Repository pour gérer les opérations sur l'entité Patient.
 */
public interface PatientRepository extends JpaRepository<Patient, Long> {
    List<Patient> findByNomContainingIgnoreCase(String nom); // Recherche par nom (insensible à la casse)
    List<Patient> findByActifTrue(); // Patients actifs
    List<Patient> findByActifFalse(); // Patients inactifs
    List<Patient> findByArchiveFalse(); // Patients non archivés
    List<Patient> findByArchiveTrue(); // Patients archivés
    // Recherche des patients non archivés par nom (insensible à la casse)
    List<Patient> findByNomContainingIgnoreCaseAndArchiveFalse(String nom);
    // Recherche des patients archivés par nom (insensible à la casse)
    List<Patient> findByNomContainingIgnoreCaseAndArchiveTrue(String nom);
    // Recherche des patients actifs et non archivés par nom (insensible à la casse)
    List<Patient> findByNomContainingIgnoreCaseAndActifTrueAndArchiveFalse(String nom);
    // Recherche des patients non actifs et non archivés par nom (insensible à la casse)
    List<Patient> findByNomContainingIgnoreCaseAndActifFalseAndArchiveFalse(String nom);
    // Liste des patients actifs et non archivés
    List<Patient> findByActifTrueAndArchiveFalse();
    // Liste des patients non actifs et non archivés
    List<Patient> findByActifFalseAndArchiveFalse();
    
    @Query("SELECT p.produitsStandards FROM Patient p WHERE p.idPatient = :idPatient")
    List<Produit> findProduitsStandardsByPatientId(@Param("idPatient") Long idPatient);
}