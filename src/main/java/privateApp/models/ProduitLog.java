package privateApp.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "produit_log")
public class ProduitLog {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_log")
    private Long idLog;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "details")
    private String details;

    @Column(name = "date_action")
    private LocalDateTime dateAction = LocalDateTime.now();
    public Long getIdLog() {
		return idLog;
	}

	public ProduitLog() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ProduitLog(Long idLog, User user, Produit produit, String action, String details, LocalDateTime dateAction) {
		super();
		this.idLog = idLog;
		this.user = user;
		this.produit = produit;
		this.action = action;
		this.details = details;
		this.dateAction = dateAction;
	}

	public void setIdLog(Long idLog) {
		this.idLog = idLog;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Produit getProduit() {
		return produit;
	}

	public void setProduit(Produit produit) {
		this.produit = produit;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public LocalDateTime getDateAction() {
		return dateAction;
	}

	public void setDateAction(LocalDateTime dateAction) {
		this.dateAction = dateAction;
	}

}