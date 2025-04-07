// src/main/java/privateApp/dtos/PrixDTO.java
package privateApp.dtos;

import java.util.Date;

public class PrixDTO {
    private Long idPrix; // Ajout de l'ID
    private Long idProduit;
    private String nomProduit;
    private Long idFournisseur;
    private String nomFournisseur;
    private double prixUnitaire;
    private Double tauxTva;
    private Date date;

    public PrixDTO(Long idProduit, String nomProduit, Long idFournisseur, String nomFournisseur, 
                   double prixUnitaire, Double tauxTva, Date date) {
        this.idProduit = idProduit;
        this.nomProduit = nomProduit;
        this.idFournisseur = idFournisseur;
        this.nomFournisseur = nomFournisseur;
        this.prixUnitaire = prixUnitaire;
        this.tauxTva = tauxTva;
        this.date = date;
    }

    // Ajouter un constructeur avec idPrix
    public PrixDTO(Long idPrix, Long idProduit, String nomProduit, Long idFournisseur, String nomFournisseur, 
                   double prixUnitaire, Double tauxTva, Date date) {
        this.idPrix = idPrix;
        this.idProduit = idProduit;
        this.nomProduit = nomProduit;
        this.idFournisseur = idFournisseur;
        this.nomFournisseur = nomFournisseur;
        this.prixUnitaire = prixUnitaire;
        this.tauxTva = tauxTva;
        this.date = date;
    }

    // Getters et Setters
    public Long getIdPrix() { return idPrix; }
    public void setIdPrix(Long idPrix) { this.idPrix = idPrix; }
    public Long getIdProduit() { return idProduit; }
    public void setIdProduit(Long idProduit) { this.idProduit = idProduit; }
    public String getNomProduit() { return nomProduit; }
    public void setNomProduit(String nomProduit) { this.nomProduit = nomProduit; }
    public Long getIdFournisseur() { return idFournisseur; }
    public void setIdFournisseur(Long idFournisseur) { this.idFournisseur = idFournisseur; }
    public String getNomFournisseur() { return nomFournisseur; }
    public void setNomFournisseur(String nomFournisseur) { this.nomFournisseur = nomFournisseur; }
    public double getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(double prixUnitaire) { this.prixUnitaire = prixUnitaire; }
    public Double getTauxTva() { return tauxTva; }
    public void setTauxTva(Double tauxTva) { this.tauxTva = tauxTva; }
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
}