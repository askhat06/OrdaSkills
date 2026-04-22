package kz.skills.elearning.service;

import kz.skills.elearning.dto.AuthResponse;
import kz.skills.elearning.dto.CurrentUserResponse;
import kz.skills.elearning.dto.LoginRequest;
import kz.skills.elearning.dto.RegisterRequest;
import kz.skills.elearning.entity.PlatformUser;
import kz.skills.elearning.entity.UserRole;
import kz.skills.elearning.exception.BadRequestException;
import kz.skills.elearning.exception.EmailNotVerifiedException;
import kz.skills.elearning.exception.InvalidCredentialsException;
import kz.skills.elearning.exception.UserAlreadyExistsException;
import kz.skills.elearning.repository.PlatformUserRepository;
import kz.skills.elearning.security.JwtService;
import kz.skills.elearning.security.PlatformUserPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
public class AuthService {

    private final PlatformUserRepository platformUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final boolean verificationEnabled;
    private final int tokenExpiryHours;

    public AuthService(PlatformUserRepository platformUserRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       EmailService emailService,
                       @Value("${app.email.verification-enabled:true}") boolean verificationEnabled,
                       @Value("${app.email.token-expiry-hours:24}") int tokenExpiryHours) {
        this.platformUserRepository = platformUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.verificationEnabled = verificationEnabled;
        this.tokenExpiryHours = tokenExpiryHours;
    }

    public RegisterResult register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());
        String normalizedFullName = normalizeText(request.getFullName());
        String normalizedLocale = normalizeLocale(request.getLocale());

        PlatformUser user = platformUserRepository.findByEmailIgnoreCase(normalizedEmail).orElse(null);

        if (user != null && user.getPasswordHash() != null && !user.getPasswordHash().isBlank()) {
            throw new UserAlreadyExistsException("User with this email already exists");
        }

        if (user == null) {
            user = new PlatformUser();
            user.setEmail(normalizedEmail);
        }

        user.setFullName(normalizedFullName);
        user.setLocale(normalizedLocale);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        if (user.getRole() == null) {
            user.setRole(resolveRegistrationRole(request.getRole()));
        }

        if (verificationEnabled) {
            String token = UUID.randomUUID().toString();
            user.setVerificationToken(token);
            user.setTokenExpiresAt(LocalDateTime.now(ZoneOffset.UTC).plusHours(tokenExpiryHours));
            user.setEmailVerified(false);

            PlatformUser savedUser = platformUserRepository.save(user);
            emailService.sendVerificationEmail(savedUser.getEmail(), token);

            return RegisterResult.pendingVerification();
        } else {
            user.setEmailVerified(true);
            PlatformUser savedUser = platformUserRepository.save(user);
            PlatformUserPrincipal principal = PlatformUserPrincipal.from(savedUser);
            String token = jwtService.generateToken(principal);
            return RegisterResult.withToken(AuthResponse.of(token, jwtService.getExpirationSeconds(), savedUser));
        }
    }

    public AuthResponse verifyEmail(String token) {
        PlatformUser user = platformUserRepository.findByVerificationToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid or expired verification token"));

        if (user.getTokenExpiresAt() == null || user.getTokenExpiresAt().isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
            throw new BadRequestException("Verification token has expired");
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setTokenExpiresAt(null);

        PlatformUser savedUser = platformUserRepository.save(user);
        PlatformUserPrincipal principal = PlatformUserPrincipal.from(savedUser);
        String jwt = jwtService.generateToken(principal);
        return AuthResponse.of(jwt, jwtService.getExpirationSeconds(), savedUser);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());

        PlatformUser user = platformUserRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (verificationEnabled && !user.isEmailVerified()) {
            throw new EmailNotVerifiedException("Please verify your email before logging in");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(normalizedEmail, request.getPassword())
            );

            PlatformUserPrincipal principal = (PlatformUserPrincipal) authentication.getPrincipal();
            String token = jwtService.generateToken(principal);
            return AuthResponse.of(token, jwtService.getExpirationSeconds(), user);
        } catch (BadCredentialsException ex) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
    }

    @Transactional(readOnly = true)
    public CurrentUserResponse me(PlatformUserPrincipal principal) {
        return CurrentUserResponse.fromPrincipal(principal);
    }

    private UserRole resolveRegistrationRole(String role) {
        if (role != null && role.equalsIgnoreCase(UserRole.TEACHER.name())) {
            return UserRole.TEACHER;
        }
        if (role != null && role.equalsIgnoreCase(UserRole.COMPANY.name())) {
            return UserRole.COMPANY;
        }
        return UserRole.STUDENT;
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeText(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeLocale(String locale) {
        if (locale == null || locale.isBlank()) {
            return "ru";
        }
        return locale.trim().toLowerCase(Locale.ROOT);
    }

    public record RegisterResult(boolean requiresVerification, AuthResponse authResponse) {
        static RegisterResult pendingVerification() {
            return new RegisterResult(true, null);
        }
        static RegisterResult withToken(AuthResponse response) {
            return new RegisterResult(false, response);
        }
    }
}
