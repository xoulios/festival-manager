package gr.uoi.festivalmanager.dto;

public class ScheduleRequest {
    private String scheduledSlot;

    public ScheduleRequest() {}

    public ScheduleRequest(String scheduledSlot) {
        this.scheduledSlot = scheduledSlot;
    }

    public String getScheduledSlot() { return scheduledSlot; }
    public void setScheduledSlot(String scheduledSlot) { this.scheduledSlot = scheduledSlot; }
}
