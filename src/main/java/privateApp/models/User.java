package privateApp.models;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    private Long userId; // matricule comme identifiant
    private String password;
    private boolean isPasswordExpired = true;
    private String nom;
    private String prenom;
    private String email;
    @Temporal(TemporalType.DATE)
    private Date dateNaissance;
    private String numeroTelephone;

    // Relation ManyToMany avec Profil
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_profil",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "id_profil")
    )
    private Set<Profil> profils = new HashSet<>(); // Set pour éviter les doublons

    // Getters et Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public boolean isPasswordExpired() { return isPasswordExpired; }
    public void setPasswordExpired(boolean isPasswordExpired) { this.isPasswordExpired = isPasswordExpired; }
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
    public Set<Profil> getProfils() { return profils; }
    public void setProfils(Set<Profil> profils) { this.profils = profils; }
    public void addProfil(Profil profil) { this.profils.add(profil); }
    public void removeProfil(Profil profil) { this.profils.remove(profil); }

    // Implémentation de UserDetails
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return profils.stream()
            .map(profil -> new SimpleGrantedAuthority(profil.getLibelleProfil().toUpperCase()))
            .collect(Collectors.toSet());
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
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }
}