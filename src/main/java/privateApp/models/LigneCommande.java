package privateApp.models;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;

@Entity
@Table(name = "ligne_commande")
public class LigneCommande {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idLigneCommande;

    @ManyToOne
    @JoinColumn(name = "id_produit", nullable = false)
    private Produit produit;

    @Column(name = "quantite", nullable = false)
    private int quantite;

    @ManyToOne
    @JoinColumn(name = "id_bon_commande", nullable = false)
    @JsonBackReference
    private BonCommande bonCommande;

    @ManyToOne
    @JoinColumn(name = "id_prix", nullable = false)
    private Prix prix;

    @ManyToOne
    @JoinColumn(name = "id_fournisseur", nullable = false)
    private Fournisseur fournisseur;

    // Constructeurs
    public LigneCommande() {}

    // Getters et Setters
    public Long getIdLigneCommande() { return idLigneCommande; }
    public void setIdLigneCommande(Long idLigneCommande) { this.idLigneCommande = idLigneCommande; }
    public Produit getProduit() { return produit; }
    public void setProduit(Produit produit) { this.produit = produit; }
    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }
    public BonCommande getBonCommande() { return bonCommande; }
    public void setBonCommande(BonCommande bonCommande) { this.bonCommande = bonCommande; }
    public Prix getPrix() { return prix; }
    public void setPrix(Prix prix) { this.prix = prix; }
    public Fournisseur getFournisseur() { return fournisseur; }
    public void setFournisseur(Fournisseur fournisseur) { this.fournisseur = fournisseur; }
}