package privateApp.models;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "tokens")
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;
    private boolean loggedOut;
    @Temporal(TemporalType.TIMESTAMP)
    private Date logoutTimestamp;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Constructeurs, Getters et Setters
    public Token() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public boolean isLoggedOut() { return loggedOut; }
    public void setLoggedOut(boolean loggedOut) { this.loggedOut = loggedOut; }
    public Date getLogoutTimestamp() { return logoutTimestamp; }
    public void setLogoutTimestamp(Date logoutTimestamp) { this.logoutTimestamp = logoutTimestamp; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}