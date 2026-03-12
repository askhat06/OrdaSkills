package kz.skills.elearning.repository;

import kz.skills.elearning.entity.PlatformUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlatformUserRepository extends JpaRepository<PlatformUser, Long> {

    Optional<PlatformUser> findByEmailIgnoreCase(String email);
}
