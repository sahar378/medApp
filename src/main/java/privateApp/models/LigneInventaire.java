package privateApp.models;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;

@Entity
@Table(name = "ligne_inventaire")
public class LigneInventaire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idLigne;

    @Column(name = "qte_saisie")
    private int qteSaisie;

    @Column(name = "observation_produit")
    private String observationProduit;

    @ManyToOne
    @JoinColumn(name = "id_produit", nullable = false)
    private Produit produit;

    @ManyToOne
    @JoinColumn(name = "id_inv", nullable = false)
    @JsonBackReference
    private Inventaire inventaire;

    // Constructeurs
    public LigneInventaire() {}

    // Getters et Setters
    public Long getIdLigne() { return idLigne; }
    public void setIdLigne(Long idLigne) { this.idLigne = idLigne; }
    public int getQteSaisie() { return qteSaisie; }
    public void setQteSaisie(int qteSaisie) { this.qteSaisie = qteSaisie; }
    public String getObservationProduit() { return observationProduit; }
    public void setObservationProduit(String observationProduit) { this.observationProduit = observationProduit; }
    public Produit getProduit() { return produit; }
    public void setProduit(Produit produit) { this.produit = produit; }
    public Inventaire getInventaire() { return inventaire; }
    public void setInventaire(Inventaire inventaire) { this.inventaire = inventaire; }
}