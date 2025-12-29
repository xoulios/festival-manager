package gr.uoi.festivalmanager.dto;

public class AssignRoleRequest {
    private Long userId;
    private Long roleId;

    public AssignRoleRequest() {}

    public AssignRoleRequest(Long userId, Long roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
}
