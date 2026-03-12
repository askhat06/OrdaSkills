package kz.skills.elearning.dto;

import kz.skills.elearning.entity.PlatformUser;

public class AuthResponse {

    private String accessToken;
    private String tokenType;
    private long expiresInSeconds;
    private CurrentUserResponse user;

    public static AuthResponse of(String accessToken, long expiresInSeconds, PlatformUser user) {
        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setTokenType("Bearer");
        response.setExpiresInSeconds(expiresInSeconds);
        response.setUser(CurrentUserResponse.fromEntity(user));
        return response;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public void setExpiresInSeconds(long expiresInSeconds) {
        this.expiresInSeconds = expiresInSeconds;
    }

    public CurrentUserResponse getUser() {
        return user;
    }

    public void setUser(CurrentUserResponse user) {
        this.user = user;
    }
}