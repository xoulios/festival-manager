package gr.uoi.festivalmanager.entity;

import gr.uoi.festivalmanager.enums.PerformanceState;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "performances")
public class Performance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;

    @ManyToOne(optional = false)
    @JoinColumn(name = "artist_id", nullable = false)
    private User artist;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;


    @Column(nullable = false)
    private String genre;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "band_members", length = 2000)
    private String bandMembers;

    @Column(name = "technical_requirements", length = 4000)
    private String technicalRequirements;

    @Column(length = 4000)
    private String setlist;

    @Column(name = "preferred_rehearsal_times", length = 2000)
    private String preferredRehearsalTimes;

    @Column(name = "preferred_time_slots", length = 2000)
    private String preferredTimeSlots;

    @Column(name = "scheduled_slot", length = 2000)
    private String scheduledSlot;

    @Column(name = "final_setlist", length = 4000)
    private String finalSetlist;

    @Column(name = "final_rehearsal_times", length = 2000)
    private String finalRehearsalTimes;

    @Column(name = "final_time_slots", length = 2000)
    private String finalTimeSlots;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "final_submitted_at")
    private LocalDateTime finalSubmittedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PerformanceState state = PerformanceState.CREATED;

    @OneToMany(mappedBy = "performance", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    public Performance() {}

    public Long getId() { return id; }

    public Festival getFestival() { return festival; }
    public void setFestival(Festival festival) { this.festival = festival; }

    public User getArtist() { return artist; }
    public void setArtist(User artist) { this.artist = artist; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getBandMembers() { return bandMembers; }
    public void setBandMembers(String bandMembers) { this.bandMembers = bandMembers; }

    public String getTechnicalRequirements() { return technicalRequirements; }
    public void setTechnicalRequirements(String technicalRequirements) { this.technicalRequirements = technicalRequirements; }

    public String getSetlist() { return setlist; }
    public void setSetlist(String setlist) { this.setlist = setlist; }

    public String getPreferredRehearsalTimes() { return preferredRehearsalTimes; }
    public void setPreferredRehearsalTimes(String preferredRehearsalTimes) { this.preferredRehearsalTimes = preferredRehearsalTimes; }

    public String getPreferredTimeSlots() { return preferredTimeSlots; }
    public void setPreferredTimeSlots(String preferredTimeSlots) { this.preferredTimeSlots = preferredTimeSlots; }

    public String getScheduledSlot() { return scheduledSlot; }
    public void setScheduledSlot(String scheduledSlot) { this.scheduledSlot = scheduledSlot; }

    public String getFinalSetlist() { return finalSetlist; }
    public void setFinalSetlist(String finalSetlist) { this.finalSetlist = finalSetlist; }

    public String getFinalRehearsalTimes() { return finalRehearsalTimes; }
    public void setFinalRehearsalTimes(String finalRehearsalTimes) { this.finalRehearsalTimes = finalRehearsalTimes; }

    public String getFinalTimeSlots() { return finalTimeSlots; }
    public void setFinalTimeSlots(String finalTimeSlots) { this.finalTimeSlots = finalTimeSlots; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public LocalDateTime getFinalSubmittedAt() { return finalSubmittedAt; }
    public void setFinalSubmittedAt(LocalDateTime finalSubmittedAt) { this.finalSubmittedAt = finalSubmittedAt; }

    public PerformanceState getState() { return state; }
    public void setState(PerformanceState state) { this.state = state; }

    public List<Review> getReviews() { return reviews; }
    public void setReviews(List<Review> reviews) { this.reviews = reviews; }
}
