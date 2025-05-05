package privateApp.models;

import jakarta.persistence.*;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonBackReference;

/**
 * Entité représentant les périodes de dialyse d'un patient.
 * Permet de suivre les dates de début et de fin de chaque période.
 */
@Entity
@Table(name = "patient_debut_fin")
public class PatientDebutFin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPatDF;

    private LocalDate dateDebutDialyse; // Date de début de la période
    private LocalDate dateFinDialyse; // Date de fin (null si en cours)
    private String cause; // Cause de la fin de dialyse (optionnel)

    @ManyToOne
    @JoinColumn(name = "id_patient", nullable = false)
    @JsonBackReference
    private Patient patient; // Patient associé

    // Getters et Setters
    public Long getIdPatDF() { return idPatDF; }
    public void setIdPatDF(Long idPatDF) { this.idPatDF = idPatDF; }
    public LocalDate getDateDebutDialyse() { return dateDebutDialyse; }
    public void setDateDebutDialyse(LocalDate dateDebutDialyse) { this.dateDebutDialyse = dateDebutDialyse; }
    public LocalDate getDateFinDialyse() { return dateFinDialyse; }
    public void setDateFinDialyse(LocalDate dateFinDialyse) { this.dateFinDialyse = dateFinDialyse; }
    public String getCause() { return cause; }
    public void setCause(String cause) { this.cause = cause; }
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
}