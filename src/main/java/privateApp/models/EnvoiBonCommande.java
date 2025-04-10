package privateApp.models;

import jakarta.persistence.*;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "envoi_bon_commande")
public class EnvoiBonCommande {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sent_by", nullable = false)
    private User sentBy; // Intendant qui a envoyé le bon

    @ManyToOne
    @JoinColumn(name = "id_fournisseur", nullable = false)
    private Fournisseur fournisseur; // Fournisseur destinataire

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateEnvoi; // Date d'envoi

    @ManyToOne
    @JoinColumn(name = "id_bon_commande", nullable = false)
    @JsonBackReference // Ignore cette relation lors de la sérialisation
    private BonCommande bonCommande; // Bon de commande associé

    // Constructeurs
    public EnvoiBonCommande() {}

    public EnvoiBonCommande(User sentBy, Fournisseur fournisseur, Date dateEnvoi, BonCommande bonCommande) {
        this.sentBy = sentBy;
        this.fournisseur = fournisseur;
        this.dateEnvoi = dateEnvoi;
        this.bonCommande = bonCommande;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getSentBy() { return sentBy; }
    public void setSentBy(User sentBy) { this.sentBy = sentBy; }
    public Fournisseur getFournisseur() { return fournisseur; }
    public void setFournisseur(Fournisseur fournisseur) { this.fournisseur = fournisseur; }
    public Date getDateEnvoi() { return dateEnvoi; }
    public void setDateEnvoi(Date dateEnvoi) { this.dateEnvoi = dateEnvoi; }
    public BonCommande getBonCommande() { return bonCommande; }
    public void setBonCommande(BonCommande bonCommande) { this.bonCommande = bonCommande; }
}