package kz.skills.elearning.controller;

import kz.skills.elearning.dto.AuthResponse;
import kz.skills.elearning.dto.CurrentUserResponse;
import kz.skills.elearning.dto.ForgotPasswordRequest;
import kz.skills.elearning.dto.LoginRequest;
import kz.skills.elearning.dto.MessageResponse;
import kz.skills.elearning.dto.RegisterRequest;
import kz.skills.elearning.dto.ResetPasswordRequest;
import kz.skills.elearning.dto.UpdateProfileRequest;
import kz.skills.elearning.security.PlatformUserPrincipal;
import kz.skills.elearning.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Value("${app.email.base-url:http://localhost:5173}")
    private String frontendUrl;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        AuthService.RegisterResult result = authService.register(request);
        if (result.requiresVerification()) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new MessageResponse("Registration successful. Please check your email to verify your account."));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result.authResponse());
    }

    @GetMapping("/verify")
    public ResponseEntity<Void> verify(@RequestParam String token) {
        try {
            authService.verifyEmail(token);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, frontendUrl + "/login?verified=true")
                    .build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, frontendUrl + "/login?error=invalid-token")
                    .build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> me(@AuthenticationPrincipal PlatformUserPrincipal principal) {
        return ResponseEntity.ok(authService.me(principal));
    }

    @PutMapping("/me")
    public ResponseEntity<CurrentUserResponse> updateProfile(
            @AuthenticationPrincipal PlatformUserPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(authService.updateProfile(principal, request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(new MessageResponse("If this email exists, a reset link has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(new MessageResponse("Password reset successfully."));
    }
}
