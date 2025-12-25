package gr.uoi.festivalmanager.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // N:1 προς Performance
    @ManyToOne(optional = false)
    @JoinColumn(name = "performance_id", nullable = false)
    private Performance performance;

    // N:1 προς User (reviewer)
    @ManyToOne(optional = false)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    @Column(nullable = false)
    private int score;

    @Column(length = 2000)
    private String comments;

    public Review() {}

    // getters/setters

    public Long getId() { return id; }

    public Performance getPerformance() { return performance; }
    public void setPerformance(Performance performance) { this.performance = performance; }

    public User getReviewer() { return reviewer; }
    public void setReviewer(User reviewer) { this.reviewer = reviewer; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
}
