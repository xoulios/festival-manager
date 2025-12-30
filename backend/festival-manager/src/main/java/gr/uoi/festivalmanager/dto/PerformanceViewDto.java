package gr.uoi.festivalmanager.dto;

public class PerformanceViewDto {

    private Long id;
    private Long festivalId;

    private String name;
    private String genre;
    private String description;
    private String scheduledSlot;
    private String preferredRehearsalTimes;
    private String preferredTimeSlots;
    private String finalSetlist;
    private String finalRehearsalTimes;
    private String finalTimeSlots;
    private Integer lastReviewScore;
    private String lastReviewComments;
    private String state;

    public PerformanceViewDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getFestivalId() { return festivalId; }
    public void setFestivalId(Long festivalId) { this.festivalId = festivalId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getScheduledSlot() { return scheduledSlot; }
    public void setScheduledSlot(String scheduledSlot) { this.scheduledSlot = scheduledSlot; }

    public String getPreferredRehearsalTimes() { return preferredRehearsalTimes; }
    public void setPreferredRehearsalTimes(String preferredRehearsalTimes) { this.preferredRehearsalTimes = preferredRehearsalTimes; }

    public String getPreferredTimeSlots() { return preferredTimeSlots; }
    public void setPreferredTimeSlots(String preferredTimeSlots) { this.preferredTimeSlots = preferredTimeSlots; }

    public String getFinalSetlist() { return finalSetlist; }
    public void setFinalSetlist(String finalSetlist) { this.finalSetlist = finalSetlist; }

    public String getFinalRehearsalTimes() { return finalRehearsalTimes; }
    public void setFinalRehearsalTimes(String finalRehearsalTimes) { this.finalRehearsalTimes = finalRehearsalTimes; }

    public String getFinalTimeSlots() { return finalTimeSlots; }
    public void setFinalTimeSlots(String finalTimeSlots) { this.finalTimeSlots = finalTimeSlots; }

    public Integer getLastReviewScore() { return lastReviewScore; }
    public void setLastReviewScore(Integer lastReviewScore) { this.lastReviewScore = lastReviewScore; }

    public String getLastReviewComments() { return lastReviewComments; }
    public void setLastReviewComments(String lastReviewComments) { this.lastReviewComments = lastReviewComments; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
}
