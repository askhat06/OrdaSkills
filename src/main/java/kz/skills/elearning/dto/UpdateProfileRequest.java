package kz.skills.elearning.dto;

public class UpdateProfileRequest {
    private String fullName;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    private String avatarUrl;
private String location;

public String getAvatarUrl() { return avatarUrl; }
public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

public String getLocation() { return location; }
public void setLocation(String location) { this.location = location; }
}