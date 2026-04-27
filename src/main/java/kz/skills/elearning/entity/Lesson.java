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

import java.time.LocalDateTime;

@Entity
@Table(name = "lessons", uniqueConstraints = {
        @UniqueConstraint(name = "uk_lessons_slug_per_course", columnNames = {"course_id", "slug"})
})
public class Lesson extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false, length = 120)
    private String slug;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(length = 500)
    private String summary;

    @Column(length = 8000)
    private String content;

    @Column(length = 400)
    private String videoUrl;

    @Column(length = 500)
    private String videoStorageKey;

    @Column(length = 255)
    private String videoOriginalFilename;

    @Column(length = 120)
    private String videoContentType;

    private Long videoSizeBytes;

    private LocalDateTime videoUploadedAt;

    @Column(nullable = false)
    private Integer position;

    private Integer durationMinutes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getVideoStorageKey() {
        return videoStorageKey;
    }

    public void setVideoStorageKey(String videoStorageKey) {
        this.videoStorageKey = videoStorageKey;
    }

    public String getVideoOriginalFilename() {
        return videoOriginalFilename;
    }

    public void setVideoOriginalFilename(String videoOriginalFilename) {
        this.videoOriginalFilename = videoOriginalFilename;
    }

    public String getVideoContentType() {
        return videoContentType;
    }

    public void setVideoContentType(String videoContentType) {
        this.videoContentType = videoContentType;
    }

    public Long getVideoSizeBytes() {
        return videoSizeBytes;
    }

    public void setVideoSizeBytes(Long videoSizeBytes) {
        this.videoSizeBytes = videoSizeBytes;
    }

    public LocalDateTime getVideoUploadedAt() {
        return videoUploadedAt;
    }

    public void setVideoUploadedAt(LocalDateTime videoUploadedAt) {
        this.videoUploadedAt = videoUploadedAt;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
}
