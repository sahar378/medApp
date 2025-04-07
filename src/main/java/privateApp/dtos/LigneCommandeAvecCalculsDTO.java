package privateApp.dtos;

import privateApp.models.LigneCommande;

public class LigneCommandeAvecCalculsDTO {

	private Long idProduit;
    private String nomProduit;
    private Long idFournisseur;
    private String nomFournisseur;
    private int quantite;
    private double prixUnitaire;
    private double tauxTva;
    private double sousTotal;
    private double montantTva;
    private double total;

    public LigneCommandeAvecCalculsDTO(LigneCommande ligne) {
        this.idProduit = ligne.getProduit().getIdProduit();
        this.nomProduit = ligne.getProduit().getNom();
        this.idFournisseur = ligne.getFournisseur().getIdFournisseur();
        this.nomFournisseur = ligne.getFournisseur().getNom();
        this.quantite = ligne.getQuantite();
        this.prixUnitaire = ligne.getPrix().getPrixUnitaire();
        this.tauxTva = ligne.getPrix().getTauxTva();
        // Les calculs sont faits dans la méthode appelante
    }

    // Getters et Setters
    public Long getIdProduit() { return idProduit; }
    public void setIdProduit(Long idProduit) { this.idProduit = idProduit; }
    public String getNomProduit() { return nomProduit; }
    public void setNomProduit(String nomProduit) { this.nomProduit = nomProduit; }
    public Long getIdFournisseur() { return idFournisseur; }
    public void setIdFournisseur(Long idFournisseur) { this.idFournisseur = idFournisseur; }
    public String getNomFournisseur() { return nomFournisseur; }
    public void setNomFournisseur(String nomFournisseur) { this.nomFournisseur = nomFournisseur; }
    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }
    public double getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(double prixUnitaire) { this.prixUnitaire = prixUnitaire; }
    public double getTauxTva() { return tauxTva; }
    public void setTauxTva(double tauxTva) { this.tauxTva = tauxTva; }
    public double getSousTotal() { return sousTotal; }
    public void setSousTotal(double sousTotal) { this.sousTotal = sousTotal; }
    public double getMontantTva() { return montantTva; }
    public void setMontantTva(double montantTva) { this.montantTva = montantTva; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
}
