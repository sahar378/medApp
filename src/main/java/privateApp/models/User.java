package privateApp.models;
//Représente un utilisateur (agent ou intendant) dans l’application.
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

@Entity
@Table(name = "users")
public class User implements UserDetails{
    @Id
    private Long userId; // matricule comme identifiant
    private String password;
    private boolean isPasswordExpired = true; //Indique si le mot de passe est temporaire Par défaut, mot de passe temporaire
    private String nom;   
    private String prenom;   // Nom de l'utilisateur
    private String email; // Nouvel attribut pour l'email
    @Temporal(TemporalType.DATE)
    private Date dateNaissance; // Nouvel attribut pour la date de naissance
    private String numeroTelephone; // Nouvel attribut pour le numéro de téléphone
    @ManyToOne
    @JoinColumn(name = "id_profil")
    private Profil profil;//un rôle (profil) qui détermine ses privilèges et sa page de redirection.

    // Getters et Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public boolean isPasswordExpired() { return isPasswordExpired; }
    public void setPasswordExpired(boolean isPasswordExpired) { this.isPasswordExpired = isPasswordExpired; }
    public Profil getProfil() { return profil; }
    public void setProfil(Profil profil) { this.profil = profil; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Date getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(Date dateNaissance) { this.dateNaissance = dateNaissance; }
    public String getNumeroTelephone() { return numeroTelephone; }
    public void setNumeroTelephone(String numeroTelephone) { this.numeroTelephone = numeroTelephone; }
    
 // Implémentation de UserDetails
    @Override
/*Si l’utilisateur a un profil (non null), l’autorité est le libelleProfil en majuscules (ex. "INTENDANT", "RESPONSABLE_STOCK").
Si profil = null, une autorité par défaut "USER" est attribuée.

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(profil != null ? profil.getLibelleProfil().toUpperCase() : "USER"));
    }
    */
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (profil != null) {
            return Collections.singletonList(new SimpleGrantedAuthority(profil.getLibelleProfil().toUpperCase()));
        }
        return Collections.emptyList(); // Pas d'autorité si pas de profil
    }

    @Override
    public String getUsername() {
        return String.valueOf(userId);
    }

    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; } // Changé ici pour toujours true
    @Override
    public boolean isEnabled() { return true; }
}