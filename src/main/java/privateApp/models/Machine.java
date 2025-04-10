// privateApp.models/Machine.java
package privateApp.models;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "machine")
public class Machine {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_machine")
  private Long idMachine;

  @Column(name = "date_mise_en_service", nullable = false)
  @Temporal(TemporalType.DATE)
  private Date dateMiseEnService;

  @Column(name = "disponibilite", nullable = false)
  private int disponibilite = 0; // 0 = disponible, 1 = en intervention

  @Column(name = "type")
  private String type;

  @Column(name = "constructeur")
  private String constructeur;

  @Column(name = "fournisseur")
  private String fournisseur;

  @Column(name = "caracteristique")
  private String caracteristique;

  @Column(name = "voltage")
  private String voltage;

  @Column(name = "archived", nullable = false)
  private boolean archived = false;

  // Constructeurs
  public Machine() {}

  // Getters et Setters
  public Long getIdMachine() { return idMachine; }
  public void setIdMachine(Long idMachine) { this.idMachine = idMachine; }
  public Date getDateMiseEnService() { return dateMiseEnService; }
  public void setDateMiseEnService(Date dateMiseEnService) { this.dateMiseEnService = dateMiseEnService; }
  public int getDisponibilite() { return disponibilite; }
  public void setDisponibilite(int disponibilite) { this.disponibilite = disponibilite; }
  public String getType() { return type; }
  public void setType(String type) { this.type = type; }
  public String getConstructeur() { return constructeur; }
  public void setConstructeur(String constructeur) { this.constructeur = constructeur; }
  public String getFournisseur() { return fournisseur; }
  public void setFournisseur(String fournisseur) { this.fournisseur = fournisseur; }
  public String getCaracteristique() { return caracteristique; }
  public void setCaracteristique(String caracteristique) { this.caracteristique = caracteristique; }
  public String getVoltage() { return voltage; }
  public void setVoltage(String voltage) { this.voltage = voltage; }
  public boolean isArchived() { return archived; }
  public void setArchived(boolean archived) { this.archived = archived; }
}