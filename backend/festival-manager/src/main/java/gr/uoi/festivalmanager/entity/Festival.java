package gr.uoi.festivalmanager.entity;

import gr.uoi.festivalmanager.enums.FestivalState;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "festivals")
public class Festival {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FestivalState state = FestivalState.CREATED;

    @OneToMany(mappedBy = "festival", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Performance> performances = new ArrayList<>();

    public Festival() {}

    // getters/setters

    public Long getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public FestivalState getState() { return state; }
    public void setState(FestivalState state) { this.state = state; }

    public List<Performance> getPerformances() { return performances; }
    public void setPerformances(List<Performance> performances) { this.performances = performances; }
}
