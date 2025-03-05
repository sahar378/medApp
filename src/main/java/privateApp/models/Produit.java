package privateApp.models;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "produit")
public class Produit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProduit;

    private String nom;
    private String description;

    @Column(name = "qte_disponible")
    private int qteDisponible;

    @Column(name = "seuil_alerte")
    private int seuilAlerte;

    @Temporal(TemporalType.DATE)
    @Column(name = "date_expiration")
    private Date dateExpiration; // Nullable pour les matériels

    @ManyToOne
    @JoinColumn(name = "id_categorie", nullable = false)
    private Categorie categorie;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Responsable de stock qui gère ce produit

    // Constructeurs
    public Produit() {}

    // Getters et Setters
    public Long getIdProduit() { return idProduit; }
    public void setIdProduit(Long idProduit) { this.idProduit = idProduit; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getQteDisponible() { return qteDisponible; }
    public void setQteDisponible(int qteDisponible) { this.qteDisponible = qteDisponible; }
    public int getSeuilAlerte() { return seuilAlerte; }
    public void setSeuilAlerte(int seuilAlerte) { this.seuilAlerte = seuilAlerte; }
    public Date getDateExpiration() { return dateExpiration; }
    public void setDateExpiration(Date dateExpiration) { this.dateExpiration = dateExpiration; }
    public Categorie getCategorie() { return categorie; }
    public void setCategorie(Categorie categorie) { this.categorie = categorie; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
/*@JoinColumn : Cette annotation permet d’indiquer le nom de la clé étrangère dans la table de l’entité concernée.
 * 
 */
