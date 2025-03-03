package privateApp.dtos;

public class AssignRoleRequest {
	private Long userId;
    private Long profilId;

    // Getters et Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getProfilId() { return profilId; }
    public void setProfilId(Long profilId) { this.profilId = profilId; }
}
