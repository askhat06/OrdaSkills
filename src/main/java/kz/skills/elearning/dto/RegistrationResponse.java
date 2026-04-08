package kz.skills.elearning.dto;

import kz.skills.elearning.entity.PlatformUser;

public class RegistrationResponse {

    private String message;
    private boolean emailVerificationRequired;
    private CurrentUserResponse user;

    public static RegistrationResponse of(PlatformUser user) {
        RegistrationResponse response = new RegistrationResponse();
        response.setMessage("Registration successful. Please verify your email before signing in.");
        response.setEmailVerificationRequired(true);
        response.setUser(CurrentUserResponse.fromEntity(user));
        return response;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isEmailVerificationRequired() {
        return emailVerificationRequired;
    }

    public void setEmailVerificationRequired(boolean emailVerificationRequired) {
        this.emailVerificationRequired = emailVerificationRequired;
    }

    public CurrentUserResponse getUser() {
        return user;
    }

    public void setUser(CurrentUserResponse user) {
        this.user = user;
    }
}
