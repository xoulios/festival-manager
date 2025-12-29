package gr.uoi.festivalmanager.dto;

public class ReviewRequest {
    private int score;
    private String comments;

    public ReviewRequest() {}

    public ReviewRequest(int score, String comments) {
        this.score = score;
        this.comments = comments;
    }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
}
