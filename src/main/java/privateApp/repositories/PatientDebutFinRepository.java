package privateApp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import privateApp.models.Patient;
import privateApp.models.PatientDebutFin;

import java.util.Optional;

/**
 * Repository pour gérer les opérations sur l'entité PatientDebutFin.
 */
public interface PatientDebutFinRepository extends JpaRepository<PatientDebutFin, Long> {
    Optional<PatientDebutFin> findTopByPatientOrderByIdPatDFDesc(Patient patient);
}