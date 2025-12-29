package gr.uoi.festivalmanager.dto;

public class FinalSubmitRequest {
    private String finalSetlist;
    private String finalRehearsalTimes;
    private String finalTimeSlots;

    public FinalSubmitRequest() {}

    public FinalSubmitRequest(String finalSetlist, String finalRehearsalTimes, String finalTimeSlots) {
        this.finalSetlist = finalSetlist;
        this.finalRehearsalTimes = finalRehearsalTimes;
        this.finalTimeSlots = finalTimeSlots;
    }

    public String getFinalSetlist() { return finalSetlist; }
    public void setFinalSetlist(String finalSetlist) { this.finalSetlist = finalSetlist; }

    public String getFinalRehearsalTimes() { return finalRehearsalTimes; }
    public void setFinalRehearsalTimes(String finalRehearsalTimes) { this.finalRehearsalTimes = finalRehearsalTimes; }

    public String getFinalTimeSlots() { return finalTimeSlots; }
    public void setFinalTimeSlots(String finalTimeSlots) { this.finalTimeSlots = finalTimeSlots; }
}
