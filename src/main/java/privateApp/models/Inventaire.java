package privateApp.models;

import jakarta.persistence.*;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "inventaire")
public class Inventaire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idInv;

    @Temporal(TemporalType.TIMESTAMP) // Changement ici pour inclure heure et minutes
    private Date date;

    @ManyToOne
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    @Column(name = "etat")
    private boolean etat; // 0 = false (différence trouvée), 1 = true (aucune différence)

    @Column(name = "observation_inventaire", length = 1000)
    private String observationInventaire;

    @OneToMany(mappedBy = "inventaire", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<LigneInventaire> lignesInventaire;

    // Constructeurs
    public Inventaire() {}

    // Getters et Setters
    public Long getIdInv() { return idInv; }
    public void setIdInv(Long idInv) { this.idInv = idInv; }
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public boolean isEtat() { return etat; }
    public void setEtat(boolean etat) { this.etat = etat; }
    public String getObservationInventaire() { return observationInventaire; }
    public void setObservationInventaire(String observationInventaire) { this.observationInventaire = observationInventaire; }
    public List<LigneInventaire> getLignesInventaire() { return lignesInventaire; }
    public void setLignesInventaire(List<LigneInventaire> lignesInventaire) { this.lignesInventaire = lignesInventaire; }
}