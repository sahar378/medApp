// privateApp.dtos/LivraisonDTO.java
package privateApp.dtos;

import java.util.Date;

public class LivraisonRequest {
    private Long idProduit;
    private Long idFournisseur;
    private int quantiteLivree;
    private Date date;
    private String observation;
    private String livreur;

    // Constructeurs
    public LivraisonRequest() {}

    public LivraisonRequest(Long idProduit, Long idFournisseur, int quantiteLivree, Date date, String observation, String livreur) {
        this.idProduit = idProduit;
        this.idFournisseur = idFournisseur;
        this.quantiteLivree = quantiteLivree;
        this.date = date;
        this.observation = observation;
        this.livreur = livreur;
    }

    // Getters et Setters
    public Long getIdProduit() { return idProduit; }
    public void setIdProduit(Long idProduit) { this.idProduit = idProduit; }
    public Long getIdFournisseur() { return idFournisseur; }
    public void setIdFournisseur(Long idFournisseur) { this.idFournisseur = idFournisseur; }
    public int getQuantiteLivree() { return quantiteLivree; }
    public void setQuantiteLivree(int quantiteLivree) { this.quantiteLivree = quantiteLivree; }
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
    public String getObservation() { return observation; }
    public void setObservation(String observation) { this.observation = observation; }
    public String getLivreur() { return livreur; }
    public void setLivreur(String livreur) { this.livreur = livreur; }
}