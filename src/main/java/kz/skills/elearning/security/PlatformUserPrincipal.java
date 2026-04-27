package kz.skills.elearning.security;

import java.util.Collection;
import java.util.List;

import kz.skills.elearning.entity.PlatformUser;
import kz.skills.elearning.entity.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class PlatformUserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String passwordHash;
    private final String fullName;
    private final String locale;
    private final UserRole role;
    private final String avatarUrl;
    private final String location;

    public PlatformUserPrincipal(Long id,
                                 String email,
                                 String passwordHash,
                                 String fullName,
                                 String locale,
                                 UserRole role,
                                 String avatarUrl,
                                 String location) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.locale = locale;
        this.role = role == null ? UserRole.STUDENT : role;
        this.avatarUrl = avatarUrl;
        this.location = location;
    }

    public static PlatformUserPrincipal from(PlatformUser user) {
        return new PlatformUserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getFullName(),
                user.getLocale(),
                user.getRole(),
                user.getAvatarUrl(),
                user.getLocation()
        );
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getLocale() { return locale; }
    public UserRole getRole() { return role; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getLocation() { return location; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() { return passwordHash; }

    @Override
    public String getUsername() { return email; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}