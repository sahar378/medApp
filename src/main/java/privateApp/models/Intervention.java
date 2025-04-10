// privateApp.models/Intervention.java
package privateApp.models;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "intervention")
public class Intervention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_intervention")
    private Long idIntervention;

    @ManyToOne
    @JoinColumn(name = "id_machine", nullable = false)
    private Machine machine;

    @ManyToOne
    @JoinColumn(name = "id_technicien", nullable = false)
    private Technicien technicien;

    @ManyToOne
    @JoinColumn(name = "id_personnel", nullable = false) // Lien avec l'utilisateur qui crée l'intervention
    private User personnel; // Assurez-vous que cette entité User existe (ex. pour PERSONNEL_MEDICAL)

    @Column(name = "date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date date; // Date de création de l'intervention

    @Column(name = "nature", nullable = false)
    private int nature; // 0 = préventif, 1 = réparation

    @Column(name = "date_panne")
    @Temporal(TemporalType.DATE)
    private Date datePanne;

    @Column(name = "panne")
    private String panne; // Rempli si nature = 1

    @Column(name = "reparation")
    private String reparation;

    @Column(name = "date_reparation")
    @Temporal(TemporalType.DATE)
    private Date dateReparation;

    @Column(name = "lieu_reparation")
    private String lieuReparation; // Champ optionnel

    @Column(name = "est_fermee", nullable = false)
    private boolean estFermee = false; // Indique si l'intervention est terminée
    
    @Column(name = "archived", nullable = false)
    private boolean archived = false;

    // Constructeurs
    public Intervention() {}

    public Intervention(Machine machine, Technicien technicien, User personnel, Date date, int nature, Date datePanne, String panne, String reparation, Date dateReparation, String lieuReparation) {
        this.machine = machine;
        this.technicien = technicien;
        this.personnel = personnel;
        this.date = date;
        this.nature = nature;
        this.datePanne = datePanne;
        this.panne = panne;
        this.reparation = reparation;
        this.dateReparation = dateReparation;
        this.lieuReparation = lieuReparation;
    }

    // Getters et Setters
    public Long getIdIntervention() {
        return idIntervention;
    }

    public void setIdIntervention(Long idIntervention) {
        this.idIntervention = idIntervention;
    }

    public Machine getMachine() {
        return machine;
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
    }

    public Technicien getTechnicien() {
        return technicien;
    }

    public void setTechnicien(Technicien technicien) {
        this.technicien = technicien;
    }

    public User getPersonnel() {
        return personnel;
    }

    public void setPersonnel(User personnel) {
        this.personnel = personnel;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getNature() {
        return nature;
    }

    public void setNature(int nature) {
        this.nature = nature;
    }

    public Date getDatePanne() {
        return datePanne;
    }

    public void setDatePanne(Date datePanne) {
        this.datePanne = datePanne;
    }

    public String getPanne() {
        return panne;
    }

    public void setPanne(String panne) {
        this.panne = panne;
    }

    public String getReparation() {
        return reparation;
    }

    public void setReparation(String reparation) {
        this.reparation = reparation;
    }

    public Date getDateReparation() {
        return dateReparation;
    }

    public void setDateReparation(Date dateReparation) {
        this.dateReparation = dateReparation;
    }

    public String getLieuReparation() {
        return lieuReparation;
    }

    public void setLieuReparation(String lieuReparation) {
        this.lieuReparation = lieuReparation;
    }

    public boolean isEstFermee() {
        return estFermee;
    }

    public void setEstFermee(boolean estFermee) {
        this.estFermee = estFermee;
    }
    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }
}