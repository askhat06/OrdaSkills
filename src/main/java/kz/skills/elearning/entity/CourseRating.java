package kz.skills.elearning.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "course_ratings", uniqueConstraints = {
        @UniqueConstraint(name = "uk_rating_course_student", columnNames = {"course_id", "student_id"})
})
public class CourseRating extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private PlatformUser student;

    @Column(nullable = false)
    private int rating;

    public Long getId() { return id; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public PlatformUser getStudent() { return student; }
    public void setStudent(PlatformUser student) { this.student = student; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
}
