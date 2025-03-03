package privateApp.dtos;

public class ResetPasswordRequest {
    private Long userId;
    private String tempPassword;

    // Getters et Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getTempPassword() { return tempPassword; }
    public void setTempPassword(String tempPassword) { this.tempPassword = tempPassword; }
}
