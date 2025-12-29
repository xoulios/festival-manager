package gr.uoi.festivalmanager.dto;

import java.time.LocalDate;

public class FestivalCreateRequest {
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;

    public FestivalCreateRequest() {}

    public FestivalCreateRequest(String title, String description, LocalDate startDate, LocalDate endDate) {
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
