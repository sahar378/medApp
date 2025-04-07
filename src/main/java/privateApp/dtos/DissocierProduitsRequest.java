package privateApp.dtos;

import java.util.List;

public class DissocierProduitsRequest {
	private List<Long> produitIds;

    public List<Long> getProduitIds() {
        return produitIds;
    }

    public void setProduitIds(List<Long> produitIds) {
        this.produitIds = produitIds;
    }

}
