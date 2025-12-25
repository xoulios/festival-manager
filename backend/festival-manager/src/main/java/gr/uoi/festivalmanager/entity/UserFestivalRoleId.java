package gr.uoi.festivalmanager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UserFestivalRoleId implements Serializable {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "festival_id")
    private Long festivalId;

    @Column(name = "role_id")
    private Long roleId;

    public UserFestivalRoleId() {}

    public UserFestivalRoleId(Long userId, Long festivalId, Long roleId) {
        this.userId = userId;
        this.festivalId = festivalId;
        this.roleId = roleId;
    }

    public Long getUserId() { return userId; }
    public Long getFestivalId() { return festivalId; }
    public Long getRoleId() { return roleId; }

    public void setUserId(Long userId) { this.userId = userId; }
    public void setFestivalId(Long festivalId) { this.festivalId = festivalId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserFestivalRoleId that)) return false;
        return Objects.equals(userId, that.userId)
                && Objects.equals(festivalId, that.festivalId)
                && Objects.equals(roleId, that.roleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, festivalId, roleId);
    }
}
