package privateApp.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import privateApp.repositories.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        logger.info("Chargement de l'utilisateur avec userId: {}", userId);
        UserDetails user = userRepository.findByUserId(Long.valueOf(userId))
                .orElseThrow(() -> {
                    logger.error("Utilisateur non trouvé pour userId: {}", userId);
                    return new UsernameNotFoundException("User not found with userId: " + userId);
                });
        logger.info("Utilisateur chargé avec succès: {}", userId);
        return user;
    }
}