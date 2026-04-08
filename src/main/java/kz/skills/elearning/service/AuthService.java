package kz.skills.elearning.service;

import kz.skills.elearning.dto.AuthResponse;
import kz.skills.elearning.dto.CurrentUserResponse;
import kz.skills.elearning.dto.EmailVerificationResponse;
import kz.skills.elearning.dto.LoginRequest;
import kz.skills.elearning.dto.MessageResponse;
import kz.skills.elearning.dto.RegisterRequest;
import kz.skills.elearning.dto.RegistrationResponse;
import kz.skills.elearning.dto.ResendVerificationRequest;
import kz.skills.elearning.entity.EmailVerificationToken;
import kz.skills.elearning.entity.PlatformUser;
import kz.skills.elearning.entity.UserRole;
import kz.skills.elearning.exception.EmailNotVerifiedException;
import kz.skills.elearning.exception.EmailVerificationTokenExpiredException;
import kz.skills.elearning.exception.EmailVerificationTokenInvalidException;
import kz.skills.elearning.exception.InvalidCredentialsException;
import kz.skills.elearning.exception.UserAlreadyExistsException;
import kz.skills.elearning.repository.EmailVerificationTokenRepository;
import kz.skills.elearning.repository.PlatformUserRepository;
import kz.skills.elearning.config.EmailVerificationProperties;
import kz.skills.elearning.security.JwtService;
import kz.skills.elearning.security.PlatformUserPrincipal;
import kz.skills.elearning.service.email.EmailVerificationEmailService;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Locale;

@Service
@Transactional
public class AuthService {

    private final PlatformUserRepository platformUserRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailVerificationProperties emailVerificationProperties;
    private final EmailVerificationEmailService emailVerificationEmailService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(PlatformUserRepository platformUserRepository,
                       EmailVerificationTokenRepository emailVerificationTokenRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       EmailVerificationProperties emailVerificationProperties,
                       EmailVerificationEmailService emailVerificationEmailService) {
        this.platformUserRepository = platformUserRepository;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.emailVerificationProperties = emailVerificationProperties;
        this.emailVerificationEmailService = emailVerificationEmailService;
    }

    public RegistrationResponse register(RegisterRequest request) {
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
        user.setRole(user.getRole() == null ? UserRole.STUDENT : user.getRole());
        user.setEmailVerified(false);

        PlatformUser savedUser = platformUserRepository.save(user);
        sendVerificationEmail(savedUser);

        return RegistrationResponse.of(savedUser);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());

        PlatformUser user = platformUserRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (!user.isEmailVerified()) {
            throw new EmailNotVerifiedException("Email is not verified. Please confirm your email before signing in.");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(normalizedEmail, request.getPassword())
            );

            PlatformUserPrincipal principal = (PlatformUserPrincipal) authentication.getPrincipal();
            String token = jwtService.generateToken(principal);
            return AuthResponse.of(token, jwtService.getExpirationSeconds(), user);
        } catch (BadCredentialsException | DisabledException ex) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
    }

    public EmailVerificationResponse verifyEmail(String rawToken) {
        String normalizedToken = normalizeText(rawToken);
        if (normalizedToken == null || normalizedToken.isBlank()) {
            throw new EmailVerificationTokenInvalidException("Verification token is required.");
        }

        EmailVerificationToken token = emailVerificationTokenRepository.findByTokenHash(hashToken(normalizedToken))
                .orElseThrow(() -> new EmailVerificationTokenInvalidException(
                        "Verification link is invalid. Please request a new email."
                ));

        if (token.getUsedAt() != null) {
            throw new EmailVerificationTokenInvalidException(
                    "Verification link has already been used. Please request a new email."
            );
        }

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (token.getExpiresAt().isBefore(now)) {
            throw new EmailVerificationTokenExpiredException(
                    "Verification link has expired. Please request a new email."
            );
        }

        PlatformUser user = token.getUser();
        if (!user.isEmailVerified()) {
            user.setEmailVerified(true);
            platformUserRepository.save(user);
        }

        token.setUsedAt(now);
        emailVerificationTokenRepository.save(token);

        return EmailVerificationResponse.success("Email verified successfully. You can now sign in.", user);
    }

    public MessageResponse resendVerificationEmail(ResendVerificationRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());
        platformUserRepository.findByEmailIgnoreCase(normalizedEmail)
                .filter(user -> !user.isEmailVerified())
                .ifPresent(this::sendVerificationEmail);

        return MessageResponse.of("If an unverified account exists for this email, a new verification link has been sent.");
    }

    @Transactional(readOnly = true)
    public CurrentUserResponse me(PlatformUserPrincipal principal) {
        return CurrentUserResponse.fromPrincipal(principal);
    }

    private void sendVerificationEmail(PlatformUser user) {
        LocalDateTime expiresAt = LocalDateTime.now(ZoneOffset.UTC).plus(emailVerificationProperties.getTokenTtl());
        String rawToken = generateSecureToken();
        EmailVerificationToken token = new EmailVerificationToken();
        token.setUser(user);
        token.setTokenHash(hashToken(rawToken));
        token.setExpiresAt(expiresAt);
        emailVerificationTokenRepository.save(token);

        String verificationUrl = UriComponentsBuilder.fromUriString(emailVerificationProperties.getVerificationUrlBase())
                .queryParam("token", rawToken)
                .build(true)
                .toUriString();

        emailVerificationEmailService.sendVerificationEmail(user, verificationUrl, expiresAt);
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

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return toHex(hashed);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            builder.append(String.format("%02x", value));
        }
        return builder.toString();
    }
}
