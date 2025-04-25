package privateApp.models;

import jakarta.persistence.*;

@Entity
@Table(name = "technicien", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class Technicien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_technicien")
    private Long idTechnicien;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "prenom", nullable = false)
    private String prenom;

    @Column(name = "societe")
    private String societe;

    @Column(name = "telephone")
    private String telephone;

    @Column(name = "email" , nullable = false)
    private String email;

    @Column(name = "archived", nullable = false)
    private boolean archived = false;

    // Constructeurs
    public Technicien() {}

    public Technicien(String nom, String prenom, String societe, String telephone, String email) {
        this.nom = nom;
        this.prenom = prenom;
        this.societe = societe;
        this.telephone = telephone;
        this.email = email;
    }

    // Getters et Setters
    public Long getIdTechnicien() {
        return idTechnicien;
    }

    public void setIdTechnicien(Long idTechnicien) {
        this.idTechnicien = idTechnicien;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getSociete() {
        return societe;
    }

    public void setSociete(String societe) {
        this.societe = societe;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }
}