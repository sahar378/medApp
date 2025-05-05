package privateApp.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

/**
 * Entité représentant un patient dans le système de dialyse.
 * Contient les informations personnelles, médicales et les produits standards associés.
 */
@Entity
@Table(name = "patient")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPatient;

    private String nom; // Nom du patient
    private String prenom; // Prénom du patient

    @Temporal(TemporalType.DATE)
    private LocalDate dateNaissance; // Date de naissance

    private String domicile; // Adresse du patient

    @Enumerated(EnumType.STRING)
    private CarnetSoin carnetSoin; // Type de carnet de soin (CNSS ou CNRPS)

    private String groupeSanguin; // Groupe sanguin (ex. : A+, O-)

    private String numeroTelephone; // Numéro de téléphone


    private String historiqueMaladie; // Historique médical 

  
    private String antecedent; // Antécédents médicaux 

    private String evolution; // Évolution de la maladie 

    private String traitement; // Traitement actuel 

    private boolean actif = true; // Indique si le patient est en dialyse
    
    private boolean archive = false; // pour l'archivage

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<PatientDebutFin> debutFinRecords = new ArrayList<>(); // Historique des périodes de dialyse

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER) // ← AJOUT
    @JoinTable(
        name = "patient_produit_standard",
        joinColumns = @JoinColumn(name = "id_patient"),
        inverseJoinColumns = @JoinColumn(name = "id_produit")
    )
    @JsonIgnore
    private List<Produit> produitsStandards = new ArrayList<>(); // Produits standards associés

    // Enum pour le carnet de soin
    public enum CarnetSoin {
        CNSS, CNRPS
    }

    // Getters et Setters
    public Long getIdPatient() { return idPatient; }
    public void setIdPatient(Long idPatient) { this.idPatient = idPatient; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }
    public String getDomicile() { return domicile; }
    public void setDomicile(String domicile) { this.domicile = domicile; }
    public CarnetSoin getCarnetSoin() { return carnetSoin; }
    public void setCarnetSoin(CarnetSoin carnetSoin) { this.carnetSoin = carnetSoin; }
    public String getGroupeSanguin() { return groupeSanguin; }
    public void setGroupeSanguin(String groupeSanguin) { this.groupeSanguin = groupeSanguin; }
    public String getNumeroTelephone() { return numeroTelephone; }
    public void setNumeroTelephone(String numeroTelephone) { this.numeroTelephone = numeroTelephone; }
    public String getHistoriqueMaladie() { return historiqueMaladie; }
    public void setHistoriqueMaladie(String historiqueMaladie) { this.historiqueMaladie = historiqueMaladie; }
    public String getAntecedent() { return antecedent; }
    public void setAntecedent(String antecedent) { this.antecedent = antecedent; }
    public String getEvolution() { return evolution; }
    public void setEvolution(String evolution) { this.evolution = evolution; }
    public String getTraitement() { return traitement; }
    public void setTraitement(String traitement) { this.traitement = traitement; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
    public boolean isArchive() { return archive; }
    public void setArchive(boolean archive) { this.archive = archive; }
    public List<PatientDebutFin> getDebutFinRecords() { return debutFinRecords; }
    public void setDebutFinRecords(List<PatientDebutFin> debutFinRecords) { this.debutFinRecords = debutFinRecords; }
    public List<Produit> getProduitsStandards() { return produitsStandards; }
    public void setProduitsStandards(List<Produit> produitsStandards) { this.produitsStandards = produitsStandards; }
}