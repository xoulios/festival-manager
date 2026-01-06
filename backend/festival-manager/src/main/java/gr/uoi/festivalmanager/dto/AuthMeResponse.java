package gr.uoi.festivalmanager.dto;

import java.util.List;

public class AuthMeResponse {

    public static class FestivalRole {
        private Long festivalId;
        private String role;

        public FestivalRole() {}

        public FestivalRole(Long festivalId, String role) {
            this.festivalId = festivalId;
            this.role = role;
        }

        public Long getFestivalId() { return festivalId; }
        public void setFestivalId(Long festivalId) { this.festivalId = festivalId; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    private Long userId;
    private String username;
    private String effectiveRole;
    private List<FestivalRole> festivalRoles;

    public AuthMeResponse() {}

    public AuthMeResponse(Long userId, String username, String effectiveRole, List<FestivalRole> festivalRoles) {
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

    public List<FestivalRole> getFestivalRoles() { return festivalRoles; }
    public void setFestivalRoles(List<FestivalRole> festivalRoles) { this.festivalRoles = festivalRoles; }
}
