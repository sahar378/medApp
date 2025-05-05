package privateApp.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entité représentant une séance de dialyse pour un patient.
 * Une séance est associée à un patient, une machine, un infirmier, un médecin, et contient des détails sur la dialyse.
 */
@Entity
@Table(name = "seance")
public class Seance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSeance;

    @ManyToOne
    @JoinColumn(name = "id_patient", nullable = false)
    private Patient patient; // Patient associé à la séance

    private LocalDateTime date; // Date et heure de la séance

    private String observation; // Observations générales

    @ManyToOne
    @JoinColumn(name = "id_machine", nullable = false)
    private Machine machine; // Machine utilisée (relation Many-to-One)

    @ManyToOne
    @JoinColumn(name = "infirmier")
    private User infirmier; // Infirmier responsable

    @ManyToOne
    @JoinColumn(name = "medecin")
    private User medecin; // Médecin responsable

    private String dialyseur; // Type de dialyseur utilisé

    private Float caBain = 1.5f; // Concentration du bain de dialyse (par défaut 1.5)

    private Float ppid; // Prise de poids interdialytique (kg)

    private Float ps; // Poids sec (kg)

    private LocalDateTime debutDialyse; // Heure de début de la dialyse
    private LocalDateTime finDialyse; // Heure de fin de la dialyse

    private Double poidsEntree; // Poids à l'entrée (kg)
    private Double poidsSortie; // Poids à la sortie (kg)
    private Integer dureeSeance; // Durée de la séance (minutes, calculé)
    private Double pertePoids; // Perte de poids (kg, calculé)
    private Integer ufTotal; // Ultrafiltration totale (ml)

    private String restitution; // Méthode de restitution
    private String circuitFiltre; // Type de circuit/filtre

    private String taDebutDebout; // Tension artérielle début (debout)
    private String taDebutCouche; // Tension artérielle début (couché)
    private Double temperatureDebut; // Température au début

    private String taFinDebout; // Tension artérielle fin (debout)
    private String taFinCouche; // Tension artérielle fin (couché)
    private Double temperatureFin; // Température à la fin

   
    private String traitement; // Médicaments utilisés pendant la séance (texte)

    // Getters et Setters
    public Long getIdSeance() { return idSeance; }
    public void setIdSeance(Long idSeance) { this.idSeance = idSeance; }
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }
    public String getObservation() { return observation; }
    public void setObservation(String observation) { this.observation = observation; }
    public Machine getMachine() { return machine; }
    public void setMachine(Machine machine) { this.machine = machine; }
    public User getInfirmier() { return infirmier; }
    public void setInfirmier(User infirmier) { this.infirmier = infirmier; }
    public User getMedecin() { return medecin; }
    public void setMedecin(User medecin) { this.medecin = medecin; }
    public String getDialyseur() { return dialyseur; }
    public void setDialyseur(String dialyseur) { this.dialyseur = dialyseur; }
    public Float getCaBain() { return caBain; }
    public void setCaBain(Float caBain) { this.caBain = caBain; }
    public Float getPpid() { return ppid; }
    public void setPpid(Float ppid) { this.ppid = ppid; }
    public Float getPs() { return ps; }
    public void setPs(Float ps) { this.ps = ps; }
    public LocalDateTime getDebutDialyse() { return debutDialyse; }
    public void setDebutDialyse(LocalDateTime debutDialyse) { this.debutDialyse = debutDialyse; }
    public LocalDateTime getFinDialyse() { return finDialyse; }
    public void setFinDialyse(LocalDateTime finDialyse) { this.finDialyse = finDialyse; }
    public Double getPoidsEntree() { return poidsEntree; }
    public void setPoidsEntree(Double poidsEntree) { this.poidsEntree = poidsEntree; }
    public Double getPoidsSortie() { return poidsSortie; }
    public void setPoidsSortie(Double poidsSortie) { this.poidsSortie = poidsSortie; }
    public Integer getDureeSeance() { return dureeSeance; }
    public void setDureeSeance(Integer dureeSeance) { this.dureeSeance = dureeSeance; }
    public Double getPertePoids() { return pertePoids; }
    public void setPertePoids(Double pertePoids) { this.pertePoids = pertePoids; }
    public Integer getUfTotal() { return ufTotal; }
    public void setUfTotal(Integer ufTotal) { this.ufTotal = ufTotal; }
    public String getRestitution() { return restitution; }
    public void setRestitution(String restitution) { this.restitution = restitution; }
    public String getCircuitFiltre() { return circuitFiltre; }
    public void setCircuitFiltre(String circuitFiltre) { this.circuitFiltre = circuitFiltre; }
    public String getTaDebutDebout() { return taDebutDebout; }
    public void setTaDebutDebout(String taDebutDebout) { this.taDebutDebout = taDebutDebout; }
    public String getTaDebutCouche() { return taDebutCouche; }
    public void setTaDebutCouche(String taDebutCouche) { this.taDebutCouche = taDebutCouche; }
    public Double getTemperatureDebut() { return temperatureDebut; }
    public void setTemperatureDebut(Double temperatureDebut) { this.temperatureDebut = temperatureDebut; }
    public String getTaFinDebout() { return taFinDebout; }
    public void setTaFinDebout(String taFinDebout) { this.taFinDebout = taFinDebout; }
    public String getTaFinCouche() { return taFinCouche; }
    public void setTaFinCouche(String taFinCouche) { this.taFinCouche = taFinCouche; }
    public Double getTemperatureFin() { return temperatureFin; }
    public void setTemperatureFin(Double temperatureFin) { this.temperatureFin = temperatureFin; }
    public String getTraitement() { return traitement; }
    public void setTraitement(String traitement) { this.traitement = traitement; }
}