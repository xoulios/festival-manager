package gr.uoi.festivalmanager.dto;

public class FestivalRoleDto {
    private Long festivalId;
    private String role;

    public FestivalRoleDto() {}

    public FestivalRoleDto(Long festivalId, String role) {
        this.festivalId = festivalId;
        this.role = role;
    }

    public Long getFestivalId() {
        return festivalId;
    }

    public void setFestivalId(Long festivalId) {
        this.festivalId = festivalId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
