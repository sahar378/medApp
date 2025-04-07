package privateApp.dtos;

public class ApprobationRequest {
    private boolean approuve;
    private String commentaireRejet; // Obligatoire si non approuvé

    public boolean isApprouve() { return approuve; }
    public void setApprouve(boolean approuve) { this.approuve = approuve; }
    public String getCommentaireRejet() { return commentaireRejet; }
    public void setCommentaireRejet(String commentaireRejet) { this.commentaireRejet = commentaireRejet; }
}