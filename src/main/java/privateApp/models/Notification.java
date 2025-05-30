// src/main/java/privateApp/models/Notification.java
package privateApp.models;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private Date dateCreation;
    
    

    @ManyToOne
    @JoinColumn(name = "emetteur_id", nullable = false)
    private User emetteur; // Intendant qui envoie la notification

    // Constructeurs
    public Notification() {
        this.dateCreation = new Date();
    }

    public Notification(String message, User emetteur) {
        this.message = message;
        this.emetteur = emetteur;
        this.dateCreation = new Date();
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Date getDateCreation() { return dateCreation; }
    public void setDateCreation(Date dateCreation) { this.dateCreation = dateCreation; }
    public User getEmetteur() { return emetteur; }
    public void setEmetteur(User emetteur) { this.emetteur = emetteur; }
}