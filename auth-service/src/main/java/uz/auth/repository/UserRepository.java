package uz.auth.repository;
import jakarta.validation.constraints.NotNull;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.auth.entity.User;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Cacheable(value = "users", key = "#email")
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
