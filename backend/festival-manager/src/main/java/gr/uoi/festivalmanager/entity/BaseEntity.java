package gr.uoi.festivalmanager.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.PrePersist;
import java.time.Instant;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    @PrePersist
    public void onCreate() {
    Instant now = Instant.now();
    if (this.createdAt == null) this.createdAt = now;
    if (this.updatedAt == null) this.updatedAt = now;
    }

}
