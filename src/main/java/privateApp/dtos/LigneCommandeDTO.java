package privateApp.dtos;

public class LigneCommandeDTO {
    private Long idProduit;
    private int quantite;

    public Long getIdProduit() { return idProduit; }
    public void setIdProduit(Long idProduit) { this.idProduit = idProduit; }
    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }
}