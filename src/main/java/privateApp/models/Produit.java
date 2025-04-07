package privateApp.models;

import jakarta.persistence.*;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
    private Date dateExpiration;

    @ManyToOne
    @JoinColumn(name = "id_categorie", nullable = false)
    private Categorie categorie;

    @Column(name = "archive", nullable = false) // Nouveau champ
    private boolean archive = false; // Par défaut, pas archivé
    
    @ManyToMany(mappedBy = "produits")
    @JsonIgnore
    private List<Fournisseur> fournisseurs;

    public List<Fournisseur> getFournisseurs() {
		return fournisseurs;
	}

	public void setFournisseurs(List<Fournisseur> fournisseurs) {
		this.fournisseurs = fournisseurs;
	}

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
    public boolean isArchive() { return archive; }
    public void setArchive(boolean archive) { this.archive = archive; }
}