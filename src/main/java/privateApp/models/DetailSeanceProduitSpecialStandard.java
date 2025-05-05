package privateApp.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entité représentant un produit (standard ou spécial) utilisé dans une séance.
 * Permet de suivre les quantités administrées et les observations.
 */
@Entity
@Table(name = "detail_seance_produit_special_standard")
public class DetailSeanceProduitSpecialStandard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDetail;

    @ManyToOne
    @JoinColumn(name = "id_seance", nullable = false)
    private Seance seance; // Séance associée

    @ManyToOne
    @JoinColumn(name = "id_produit")
    private Produit produit; // Produit utilisé (null pour produits hors stock)

    private String qteAdministre; // Quantité administrée (ex. : "1", "200g")

    private LocalDateTime dateTemps; // Date et heure d'administration

    private String observation; // Observation optionnelle

    @Column
    private String nomProduit; // Nom du produit (saisi pour hors stock, déduit du produit sinon)

    private boolean standard = true; // True = produit standard, False = produit spécial

    // Getters et Setters
    public Long getIdDetail() { return idDetail; }
    public void setIdDetail(Long idDetail) { this.idDetail = idDetail; }
    public Seance getSeance() { return seance; }
    public void setSeance(Seance seance) { this.seance = seance; }
    public Produit getProduit() { return produit; }
    public void setProduit(Produit produit) { this.produit = produit; }
    public String getQteAdministre() { return qteAdministre; }
    public void setQteAdministre(String qteAdministre) { this.qteAdministre = qteAdministre; }
    public LocalDateTime getDateTemps() { return dateTemps; }
    public void setDateTemps(LocalDateTime dateTemps) { this.dateTemps = dateTemps; }
    public String getObservation() { return observation; }
    public void setObservation(String observation) { this.observation = observation; }
    public String getNomProduit() { return nomProduit; }
    public void setNomProduit(String nomProduit) { this.nomProduit = nomProduit; }
    public boolean isStandard() { return standard; }
    public void setStandard(boolean standard) { this.standard = standard; }
}