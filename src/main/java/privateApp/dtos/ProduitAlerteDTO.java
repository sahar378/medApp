package privateApp.dtos;

import java.util.List;

import privateApp.models.Produit;

public class ProduitAlerteDTO {
    private Produit produit;
    private List<String> messages;

    // Constructeurs
    public ProduitAlerteDTO(Produit produit, List<String> messages) {
        this.produit = produit;
        this.messages = messages;
    }

    // Getters et Setters
    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }
}