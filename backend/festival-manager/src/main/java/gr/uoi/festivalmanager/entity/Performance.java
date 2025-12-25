package gr.uoi.festivalmanager.entity;

import gr.uoi.festivalmanager.enums.PerformanceState;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "performances")
public class Performance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // N:1 προς Festival
    @ManyToOne(optional = false)
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;

    // N:1 προς User (artist)
    @ManyToOne(optional = false)
    @JoinColumn(name = "artist_id", nullable = false)
    private User artist;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PerformanceState state = PerformanceState.CREATED;

    // 1:N προς Review
    @OneToMany(mappedBy = "performance", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    public Performance() {}

    // getters/setters

    public Long getId() { return id; }

    public Festival getFestival() { return festival; }
    public void setFestival(Festival festival) { this.festival = festival; }

    public User getArtist() { return artist; }
    public void setArtist(User artist) { this.artist = artist; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public PerformanceState getState() { return state; }
    public void setState(PerformanceState state) { this.state = state; }

    public List<Review> getReviews() { return reviews; }
    public void setReviews(List<Review> reviews) { this.reviews = reviews; }
}
