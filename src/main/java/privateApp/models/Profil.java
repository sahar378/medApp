package privateApp.models;
//Définit les rôles disponibles dans l’application (ex. "Responsable de stock", "Personnel médical").
import jakarta.persistence.*;

@Entity
@Table(name = "profil")
public class Profil {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProfil;
    private String libelleProfil; // Ex. "Responsable de stock"
    private String url; // URL de redirection

    // Getters et Setters
    public Long getIdProfil() { return idProfil; }
    public void setIdProfil(Long idProfil) { this.idProfil = idProfil; }
    public String getLibelleProfil() { return libelleProfil; }
    public void setLibelleProfil(String libelleProfil) { this.libelleProfil = libelleProfil; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}