package gr.uoi.festivalmanager.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_festival_roles")
public class UserFestivalRole {

    @EmbeddedId
    private UserFestivalRoleId id;

    @ManyToOne(optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @MapsId("festivalId")
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;

    @ManyToOne(optional = false)
    @MapsId("roleId")
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    public UserFestivalRole() {}

    public UserFestivalRole(User user, Festival festival, Role role) {
        this.user = user;
        this.festival = festival;
        this.role = role;
        this.id = new UserFestivalRoleId(user.getId(), festival.getId(), role.getId());
    }

    public UserFestivalRoleId getId() { return id; }
    public void setId(UserFestivalRoleId id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Festival getFestival() { return festival; }
    public void setFestival(Festival festival) { this.festival = festival; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
