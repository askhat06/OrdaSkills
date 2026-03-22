package kz.skills.elearning.security;

import kz.skills.elearning.repository.PlatformUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class PlatformUserDetailsService implements UserDetailsService {

    private final PlatformUserRepository platformUserRepository;

    public PlatformUserDetailsService(PlatformUserRepository platformUserRepository) {
        this.platformUserRepository = platformUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String normalizedEmail = username == null ? null : username.trim().toLowerCase(Locale.ROOT);

        return platformUserRepository.findByEmailIgnoreCase(normalizedEmail)
                .map(PlatformUserPrincipal::from)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
