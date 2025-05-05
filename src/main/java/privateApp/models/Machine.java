package privateApp.models;

import jakarta.persistence.*;
import java.util.Date;

/**
 * Entité représentant une machine de dialyse.
 * Contient des informations sur la disponibilité et les caractéristiques techniques.
 */
@Entity
@Table(name = "machine")
public class Machine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMachine;

    @Temporal(TemporalType.DATE)
    private Date dateMiseEnService; // Date de mise en service

    private int disponibilite = 0; // 0 = disponible, 1 = en intervention, 2 = réformé

    private String type; // Type de machine
    private String constructeur; // Constructeur
    private String fournisseur; // Fournisseur
    private String caracteristique; // Caractéristiques techniques
    private String voltage; // Voltage requis
    private boolean archived = false; // Statut d'archivage

    // Constructeurs
    public Machine() {}

    // Getters et Setters
    public Long getIdMachine() { return idMachine; }
    public void setIdMachine(Long idMachine) { this.idMachine = idMachine; }
    public Date getDateMiseEnService() { return dateMiseEnService; }
    public void setDateMiseEnService(Date dateMiseEnService) { this.dateMiseEnService = dateMiseEnService; }
    public int getDisponibilite() { return disponibilite; }
    public void setDisponibilite(int disponibilite) { this.disponibilite = disponibilite; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getConstructeur() { return constructeur; }
    public void setConstructeur(String constructeur) { this.constructeur = constructeur; }
    public String getFournisseur() { return fournisseur; }
    public void setFournisseur(String fournisseur) { this.fournisseur = fournisseur; }
    public String getCaracteristique() { return caracteristique; }
    public void setCaracteristique(String caracteristique) { this.caracteristique = caracteristique; }
    public String getVoltage() { return voltage; }
    public void setVoltage(String voltage) { this.voltage = voltage; }
    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }
}