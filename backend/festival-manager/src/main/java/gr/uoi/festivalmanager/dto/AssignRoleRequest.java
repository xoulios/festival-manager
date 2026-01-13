package gr.uoi.festivalmanager.dto;

public class AssignRoleRequest {
    private Long userId; // ID του χρήστη στον οποίο θα ανατεθεί ο ρόλος
    private Long roleId; // ID του ρόλου που θα ανατεθεί

    public AssignRoleRequest() {} 

    public AssignRoleRequest(Long userId, Long roleId) {  // Constructor με παραμέτρους
        this.userId = userId; // ID του χρήστη
        this.roleId = roleId; // ID του ρόλου
    }

    public Long getUserId() { return userId; } 
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
}
