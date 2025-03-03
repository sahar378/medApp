package privateApp.services;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import privateApp.models.Token;
import privateApp.repositories.TokenRepository;

import java.util.Date;
import java.util.List;

@Service
public class TokenCleanupService {

    private final TokenRepository tokenRepository;

    public TokenCleanupService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Scheduled(cron = "0 0 0 * * ?") // Tous les jours à minuit
    public void cleanupExpiredTokens() {
        Date cutoffDate = new Date(System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000); // 7 jours
        List<Token> invalidTokens = tokenRepository.findByLoggedOutTrue();
        invalidTokens.stream()
                .filter(token -> token.getLogoutTimestamp() == null || token.getLogoutTimestamp().before(cutoffDate))
                .forEach(tokenRepository::delete);
    }
}

/*La tâche planifiée dans TokenCleanupService :

Récupère tous les tokens invalidés (loggedOut = true) dans invalidTokens.
Filtre cette liste pour supprimer les tokens qui :
N’ont pas de logoutTimestamp (null) OU
Ont un logoutTimestamp datant de plus de 30 jours par rapport à aujourd’hui.
S’exécute tous les jours à minuit.
Les tokens expirés sont marqués loggedOut = true dès qu’ils sont utilisés après expiration.

Par exemple, le 31 mars 2025 à 00:00:00.
cutoffDate :
Calculé comme 30 jours avant le 31 mars = 1er mars 2025 (2025-03-01).

Scénario détaillé :
Avant le changement de mot de passe :
token1 est actif (loggedOut = false).
Après le changement de mot de passe :
token1 devient loggedOut = true, logoutTimestamp = 2025-03-01 10:00:00.
Nouvelle connexion :
token2 est créé (loggedOut = false).
Déconnexion explicite :
token2 devient loggedOut = true, logoutTimestamp = 2025-03-01 12:00:00.
*/