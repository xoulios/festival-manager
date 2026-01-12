package gr.uoi.festivalmanager.dto;

import java.time.LocalDate;
import java.util.List;

public class FestivalViewDto {
    private Long id;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String state; 
    private List<String> programmerUsernames;

    public FestivalViewDto() {}

    public FestivalViewDto(Long id, String title, String description,
                          LocalDate startDate, LocalDate endDate,
                          String state, List<String> programmerUsernames) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.state = state;
        this.programmerUsernames = programmerUsernames;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public List<String> getProgrammerUsernames() { return programmerUsernames; }
    public void setProgrammerUsernames(List<String> programmerUsernames) { this.programmerUsernames = programmerUsernames; }
}
