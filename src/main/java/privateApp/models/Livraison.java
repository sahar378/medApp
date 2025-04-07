package privateApp.models;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "livraison")
public class Livraison {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idLivraison;

    @ManyToOne
    @JoinColumn(name = "id_produit", nullable = false)
    private Produit produit;
    
    @ManyToOne
    @JoinColumn(name = "id_fournisseur", nullable = false)
    private Fournisseur fournisseur;

    @Column(name = "quantite_livree", nullable = false)
    private int quantiteLivree;

    @Temporal(TemporalType.DATE)
    @Column(name = "date", nullable = false)
    private Date date;

    @Column(name = "observation")
    private String observation;

    @Column(name = "livreur", nullable = false)
    private String livreur;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Constructeurs
    public Livraison() {}

    public Livraison(Produit produit, Fournisseur fournisseur, int quantiteLivree, Date date, String observation, String livreur, User user) {
        this.produit = produit;
        this.fournisseur = fournisseur;
        this.quantiteLivree = quantiteLivree;
        this.date = date;
        this.observation = observation;
        this.livreur = livreur;
        this.user = user;
    }

    // Getters et Setters
    public Long getIdLivraison() { return idLivraison; }
    public void setIdLivraison(Long idLivraison) { this.idLivraison = idLivraison; }
    public Produit getProduit() { return produit; }
    public void setProduit(Produit produit) { this.produit = produit; }
    public Fournisseur getFournisseur() { return fournisseur; }
    public void setFournisseur(Fournisseur fournisseur) { this.fournisseur = fournisseur; }
    public int getQuantiteLivree() { return quantiteLivree; }
    public void setQuantiteLivree(int quantiteLivree) { this.quantiteLivree = quantiteLivree; }
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
    public String getObservation() { return observation; }
    public void setObservation(String observation) { this.observation = observation; }
    public String getLivreur() { return livreur; }
    public void setLivreur(String livreur) { this.livreur = livreur; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}