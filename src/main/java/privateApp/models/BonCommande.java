// src/main/java/privateApp/models/BonCommande.java
package privateApp.models;

import jakarta.persistence.*;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "bon_commande")
public class BonCommande {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idBonCommande;

    @Temporal(TemporalType.TIMESTAMP) // Changement ici
    private Date date;

    @Column(nullable = false)
    private String etat; // "brouillon", "approuvé", "envoyé", "annulé", "livré"

    @ManyToOne
    @JoinColumn(name = "id_fournisseur", nullable = false)
    private Fournisseur fournisseur;

    @OneToMany(mappedBy = "bonCommande", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<LigneCommande> lignesCommande;

    @Column(length = 1000)
    private String commentaireRejet; // Commentaire si rejeté par l'intendant

    @Column(length = 1000)
    private String motifAnnulation; // Motif si annulé par l'intendant

    @Column(nullable = true)
    @Temporal(TemporalType.TIMESTAMP) // Changement ici
    private Date dateModification; // Suivi des modifications

    @Column(nullable = true)
    @Temporal(TemporalType.TIMESTAMP) // Changement ici
    private Date dateRejet; // Nouveau champ pour suivre le rejet
    
    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy; // Responsable de stock qui a créé le bon
    
    @ManyToOne
    @JoinColumn(name = "modified_by", nullable = true)
    private User modifiedBy; // Nouveau champ pour le modificateur

    @OneToMany(mappedBy = "bonCommande", cascade = CascadeType.ALL) // Nouvelle relation
    @JsonManagedReference // Sérialise cette relation
    private List<EnvoiBonCommande> envois; // Liste des envois associés
    
    // Constructeurs
    public BonCommande() {}

    // Getters et Setters
    public Long getIdBonCommande() { return idBonCommande; }
    public void setIdBonCommande(Long idBonCommande) { this.idBonCommande = idBonCommande; }
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
    public String getEtat() { return etat; }
    public void setEtat(String etat) { this.etat = etat; }
    public Fournisseur getFournisseur() { return fournisseur; }
    public void setFournisseur(Fournisseur fournisseur) { this.fournisseur = fournisseur; }
    public List<LigneCommande> getLignesCommande() { return lignesCommande; }
    public void setLignesCommande(List<LigneCommande> lignesCommande) { this.lignesCommande = lignesCommande; }
    public String getCommentaireRejet() { return commentaireRejet; }
    public void setCommentaireRejet(String commentaireRejet) { this.commentaireRejet = commentaireRejet; }
    public String getMotifAnnulation() { return motifAnnulation; }
    public void setMotifAnnulation(String motifAnnulation) { this.motifAnnulation = motifAnnulation; }
    public Date getDateModification() { return dateModification; }
    public void setDateModification(Date dateModification) { this.dateModification = dateModification; }
    public Date getDateRejet() { return dateRejet; }
    public void setDateRejet(Date dateRejet) { this.dateRejet = dateRejet; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public User getModifiedBy() { return modifiedBy; }
    public void setModifiedBy(User modifiedBy) { this.modifiedBy = modifiedBy; }
    public List<EnvoiBonCommande> getEnvois() { return envois; }
    public void setEnvois(List<EnvoiBonCommande> envois) { this.envois = envois; }
}