package privateApp.dtos;

import java.util.List;

public class BonCommandeDTO {
    private Long idFournisseur;
    private List<LigneCommandeDTO> lignesCommande;
    private List<LigneCommandeAvecCalculsDTO> lignesAvecCalculs; // Nouveau champ pour les calculs

    // Getters et Setters
    public Long getIdFournisseur() { return idFournisseur; }
    public void setIdFournisseur(Long idFournisseur) { this.idFournisseur = idFournisseur; }
    public List<LigneCommandeDTO> getLignesCommande() { return lignesCommande; }
    public void setLignesCommande(List<LigneCommandeDTO> lignesCommande) { this.lignesCommande = lignesCommande; }
    public List<LigneCommandeAvecCalculsDTO> getLignesAvecCalculs() { return lignesAvecCalculs; }
    public void setLignesAvecCalculs(List<LigneCommandeAvecCalculsDTO> lignesAvecCalculs) { this.lignesAvecCalculs = lignesAvecCalculs; }
}