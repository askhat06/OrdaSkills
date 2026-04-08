package kz.skills.elearning.dto;

import kz.skills.elearning.entity.PlatformUser;

public class EmailVerificationResponse {

    private String message;
    private String email;
    private boolean emailVerified;

    public static EmailVerificationResponse success(String message, PlatformUser user) {
        EmailVerificationResponse response = new EmailVerificationResponse();
        response.setMessage(message);
        response.setEmail(user.getEmail());
        response.setEmailVerified(user.isEmailVerified());
        return response;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
}
