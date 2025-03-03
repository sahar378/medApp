package privateApp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import privateApp.models.Token;
import privateApp.models.User;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByToken(String token);
    List<Token> findByUser(User user);
    void deleteByLoggedOutTrueAndLogoutTimestampBefore(Date cutoffDate);
    List<Token> findByLoggedOutTrue();
}