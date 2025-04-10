package privateApp.models;

import jakarta.persistence.*;

@Entity
@Table(name = "technicien")
public class Technicien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_technicien")
    private Long idTechnicien;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "telephone")
    private String telephone;

    @Column(name = "email")
    private String email;
    @Column(name = "archived", nullable = false)
    private boolean archived = false;

    // Constructeurs
    public Technicien() {}

    public Technicien(String nom, String telephone, String email) {
        this.nom = nom;
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
    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }
}