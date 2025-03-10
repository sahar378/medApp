package privateApp.dtos;

import java.util.Set;

public class AssignRolesRequest {
    private Long userId;
    private Set<Long> profilIds;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Set<Long> getProfilIds() { return profilIds; }
    public void setProfilIds(Set<Long> profilIds) { this.profilIds = profilIds; }
}