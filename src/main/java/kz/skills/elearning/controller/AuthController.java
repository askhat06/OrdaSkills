package kz.skills.elearning.controller;

import kz.skills.elearning.dto.AuthResponse;
import kz.skills.elearning.dto.CurrentUserResponse;
import kz.skills.elearning.dto.EmailVerificationResponse;
import kz.skills.elearning.dto.LoginRequest;
import kz.skills.elearning.dto.MessageResponse;
import kz.skills.elearning.dto.RegisterRequest;
import kz.skills.elearning.dto.RegistrationResponse;
import kz.skills.elearning.dto.ResendVerificationRequest;
import kz.skills.elearning.security.PlatformUserPrincipal;
import kz.skills.elearning.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<EmailVerificationResponse> verifyEmail(@RequestParam String token) {
        return ResponseEntity.ok(authService.verifyEmail(token));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<MessageResponse> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        return ResponseEntity.accepted().body(authService.resendVerificationEmail(request));
    }

    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> me(@AuthenticationPrincipal PlatformUserPrincipal principal) {
        return ResponseEntity.ok(authService.me(principal));
    }
}
