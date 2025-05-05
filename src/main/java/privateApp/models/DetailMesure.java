package privateApp.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entité représentant une mesure prise pendant une séance de dialyse.
 * Contient des informations comme la tension artérielle, le pouls, et d'autres paramètres.
 */
@Entity
@Table(name = "detail_mesure")
public class DetailMesure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDetailMesure;

    @ManyToOne
    @JoinColumn(name = "id_seance", nullable = false)
    private Seance seance; // Séance associée

    private LocalDateTime heure; // Heure de la mesure
    private String ta; // Tension artérielle (ex. : "15/7")
    private Integer pouls; // Pouls (bpm)
    private Integer debitMlMn; // Débit sanguin (ml/min)
    private String hep; // Héparine administrée
    private Integer pv; // Pression veineuse
    private Integer ptm; // Pression transmembranaire
    private Integer conduc; // Conductivité
    private Integer ufMlH; // Ultrafiltration (ml/h)
    private Integer ufTotalAffiche; // Ultrafiltration totale affichée
    private String observation; // Observations supplémentaires

    // Getters et Setters
    public Long getIdDetailMesure() { return idDetailMesure; }
    public void setIdDetailMesure(Long idDetailMesure) { this.idDetailMesure = idDetailMesure; }
    public Seance getSeance() { return seance; }
    public void setSeance(Seance seance) { this.seance = seance; }
    public LocalDateTime getHeure() { return heure; }
    public void setHeure(LocalDateTime heure) { this.heure = heure; }
    public String getTa() { return ta; }
    public void setTa(String ta) { this.ta = ta; }
    public Integer getPouls() { return pouls; }
    public void setPouls(Integer pouls) { this.pouls = pouls; }
    public Integer getDebitMlMn() { return debitMlMn; }
    public void setDebitMlMn(Integer debitMlMn) { this.debitMlMn = debitMlMn; }
    public String getHep() { return hep; }
    public void setHep(String hep) { this.hep = hep; }
    public Integer getPv() { return pv; }
    public void setPv(Integer pv) { this.pv = pv; }
    public Integer getPtm() { return ptm; }
    public void setPtm(Integer ptm) { this.ptm = ptm; }
    public Integer getConduc() { return conduc; }
    public void setConduc(Integer conduc) { this.conduc = conduc; }
    public Integer getUfMlH() { return ufMlH; }
    public void setUfMlH(Integer ufMlH) { this.ufMlH = ufMlH; }
    public Integer getUfTotalAffiche() { return ufTotalAffiche; }
    public void setUfTotalAffiche(Integer ufTotalAffiche) { this.ufTotalAffiche = ufTotalAffiche; }
    public String getObservation() { return observation; }
    public void setObservation(String observation) { this.observation = observation; }
}