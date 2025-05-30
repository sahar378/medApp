package privateApp.models;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "prix")
public class Prix {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPrix;

    @Temporal(TemporalType.DATE)
    private Date date;

    @Column(name = "prix_unitaire", nullable = false)
    private double prixUnitaire;

    @Column(name = "statut", nullable = false)
    private int statut; // 0 = ancien, 1 = actif

    @Column(name = "taux_tva", nullable = true)
    private double tauxTva = 0.0; // Double (objet) au lieu de double (primitif) pour permettre null, 0 par défaut
    
    @ManyToOne
    @JoinColumn(name = "id_produit", nullable = false)
    private Produit produit;

    @ManyToOne
    @JoinColumn(name = "id_fournisseur", nullable = false)
    private Fournisseur fournisseur;

 // Constructeurs
    public Prix() {
        this.tauxTva = 0.0; // Par défaut, TVA à 0
    }
    // Getters et Setters
    public Long getIdPrix() { return idPrix; }
    public void setIdPrix(Long idPrix) { this.idPrix = idPrix; }
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
    public double getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(double prixUnitaire) { this.prixUnitaire = prixUnitaire; }
    public int getStatut() { return statut; }
    public void setStatut(int statut) { this.statut = statut; }
    public double getTauxTva() { return tauxTva; }
    public void setTauxTva(double tauxTva) { this.tauxTva = tauxTva; }
    public Produit getProduit() { return produit; }
    public void setProduit(Produit produit) { this.produit = produit; }
    public Fournisseur getFournisseur() { return fournisseur; }
    public void setFournisseur(Fournisseur fournisseur) { this.fournisseur = fournisseur; }
}