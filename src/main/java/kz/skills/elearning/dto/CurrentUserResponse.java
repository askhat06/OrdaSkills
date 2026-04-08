package kz.skills.elearning.dto;

import kz.skills.elearning.entity.PlatformUser;
import kz.skills.elearning.entity.UserRole;
import kz.skills.elearning.security.PlatformUserPrincipal;

public class CurrentUserResponse {

    private Long id;
    private String fullName;
    private String email;
    private String locale;
    private UserRole role;
    private boolean emailVerified;

    public static CurrentUserResponse fromEntity(PlatformUser user) {
        CurrentUserResponse response = new CurrentUserResponse();
        response.setId(user.getId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setLocale(user.getLocale());
        response.setRole(user.getRole() == null ? UserRole.STUDENT : user.getRole());
        response.setEmailVerified(user.isEmailVerified());
        return response;
    }

    public static CurrentUserResponse fromPrincipal(PlatformUserPrincipal principal) {
        CurrentUserResponse response = new CurrentUserResponse();
        response.setId(principal.getId());
        response.setFullName(principal.getFullName());
        response.setEmail(principal.getUsername());
        response.setLocale(principal.getLocale());
        response.setRole(principal.getRole());
        response.setEmailVerified(principal.isEmailVerified());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
}
