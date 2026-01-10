package gr.uoi.festivalmanager.dto;

import java.util.List;

public class MeResponse {
    private Long userId;
    private String username;
    private String effectiveRole;
    private List<FestivalRoleDto> festivalRoles;

    public MeResponse() {}

    public MeResponse(Long userId, String username, String effectiveRole, List<FestivalRoleDto> festivalRoles) {
        this.userId = userId;
        this.username = username;
        this.effectiveRole = effectiveRole;
        this.festivalRoles = festivalRoles;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEffectiveRole() { return effectiveRole; }
    public void setEffectiveRole(String effectiveRole) { this.effectiveRole = effectiveRole; }

    public List<FestivalRoleDto> getFestivalRoles() { return festivalRoles; }
    public void setFestivalRoles(List<FestivalRoleDto> festivalRoles) { this.festivalRoles = festivalRoles; }
}
