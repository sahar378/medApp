// src/main/java/privateApp/models/Fournisseur.java
package privateApp.models;

import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "fournisseur")
public class Fournisseur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idFournisseur;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false, unique = true)
    @Email(message = "L'email doit être valide")
    @NotBlank(message = "L'email ne peut pas être vide")
    private String email;

    @Column
    private String adresse;

    @Column
    private String telephone;

    @Column
    private String fax;

    @Column(nullable = true)
    private String matriculeFiscale;

    @Column(nullable = true)
    private String rib;

    @Column(nullable = true)
    private String rc;

    @Column(nullable = true)
    private String codeTva;

    @Column(nullable = false)
    private int statut; // 0 = actif, 1 = inactif, 2 = erased

    @Column(nullable = true, length = 1000)
    private String causeSuppression; // Obligatoire si statut = 2 (erased)

    @ManyToMany
    @JoinTable(
        name = "produit_fournisseur",
        joinColumns = @JoinColumn(name = "id_fournisseur"),
        inverseJoinColumns = @JoinColumn(name = "id_produit")
    )
    private List<Produit> produits;

    // Constructeurs
    public Fournisseur() {
        this.statut = 0; // Par défaut actif lors de la création
    }

    // Getters et Setters
    public Long getIdFournisseur() { return idFournisseur; }
    public void setIdFournisseur(Long idFournisseur) { this.idFournisseur = idFournisseur; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public String getFax() { return fax; }
    public void setFax(String fax) { this.fax = fax; }
    public String getMatriculeFiscale() { return matriculeFiscale; }
    public void setMatriculeFiscale(String matriculeFiscale) { this.matriculeFiscale = matriculeFiscale; }
    public String getRib() { return rib; }
    public void setRib(String rib) { this.rib = rib; }
    public String getRc() { return rc; }
    public void setRc(String rc) { this.rc = rc; }
    public String getCodeTva() { return codeTva; }
    public void setCodeTva(String codeTva) { this.codeTva = codeTva; }
    public int getStatut() { return statut; }
    public void setStatut(int statut) { this.statut = statut; }
    public String getCauseSuppression() { return causeSuppression; }
    public void setCauseSuppression(String causeSuppression) { this.causeSuppression = causeSuppression; }
    public List<Produit> getProduits() { return produits; }
    public void setProduits(List<Produit> produits) { this.produits = produits; }
}