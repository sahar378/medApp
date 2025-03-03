package privateApp.dtos;

import java.util.Date;

public class UpdateProfileRequest {
 private Long userId;
 private String nom;
 private String prenom;
 private String email;
 private Date dateNaissance;
 private String numeroTelephone;

 // Getters et Setters
 public Long getUserId() { return userId; }
 public void setUserId(Long userId) { this.userId = userId; }
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
}