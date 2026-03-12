package kz.skills.elearning.service;

import kz.skills.elearning.dto.AuthResponse;
import kz.skills.elearning.dto.CurrentUserResponse;
import kz.skills.elearning.dto.LoginRequest;
import kz.skills.elearning.dto.RegisterRequest;
import kz.skills.elearning.entity.PlatformUser;
import kz.skills.elearning.entity.UserRole;
import kz.skills.elearning.exception.InvalidCredentialsException;
import kz.skills.elearning.exception.UserAlreadyExistsException;
import kz.skills.elearning.repository.PlatformUserRepository;
import kz.skills.elearning.security.JwtService;
import kz.skills.elearning.security.PlatformUserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final PlatformUserRepository platformUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(PlatformUserRepository platformUserRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService) {
        this.platformUserRepository = platformUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
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

        PlatformUser savedUser = platformUserRepository.save(user);
        PlatformUserPrincipal principal = PlatformUserPrincipal.from(savedUser);
        String token = jwtService.generateToken(principal);

        return AuthResponse.of(token, jwtService.getExpirationSeconds(), savedUser);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());

        PlatformUser user = platformUserRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            throw new InvalidCredentialsException("This account exists but has no password yet. Please register first.");
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

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private String normalizeText(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeLocale(String locale) {
        if (locale == null || locale.isBlank()) {
            return "ru";
        }
        return locale.trim().toLowerCase();
    }
}