package privateApp.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import privateApp.models.Profil;
import privateApp.models.Token;
import privateApp.models.User;
import privateApp.repositories.ProfilRepository;
import privateApp.repositories.TokenRepository;
import privateApp.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProfilRepository profilRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private EmailService emailService;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
 // Méthode temporaire pour générer le hachage de "intendant123"
    public void generateHashForIntendant() {
        String rawPassword = "intendant123";
        String hashedPassword = passwordEncoder.encode(rawPassword);
        System.out.println("Hachage pour 'intendant123' : " + hashedPassword);
        logger.info("Hachage pour 'intendant123' : {}", hashedPassword); // Log supplémentaire
    }
  //Récupère un utilisateur par son matricule.
    public User findUserById(Long userId) {
        return userRepository.findByUserId(userId).orElse(null);
    }
    
 // Nouvelle méthode pour mettre à jour le profil de l'utilisateur
    public void updateUserProfile(Long userId, String nom, String prenom, String email, Date dateNaissance, String numeroTelephone) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé pour userId : " + userId));
        user.setNom(nom);
        user.setPrenom(prenom);
        user.setEmail(email);
        user.setDateNaissance(dateNaissance);
        user.setNumeroTelephone(numeroTelephone);
        userRepository.save(user);
    }
    
  //Met à jour le mot de passe (haché) et marque isPasswordExpired comme faux.
    public void updatePassword(Long userId, String newPassword) {
        User user = userRepository.findByUserId(userId).orElseThrow();
     // Invalider tous les tokens existants avant de changer le mot de passe
        //Récupère tous les tokens associés à l’utilisateur dans TokenRepository via la méthode findByUser.
        List<Token> existingTokens = tokenRepository.findByUser(user);
        //Parcourt chaque token existant et met :loggedOut = true pour les invalider.logoutTimestamp = new Date() pour marquer la date d’invalidation.
        for (Token token : existingTokens) {
            token.setLoggedOut(true);
            token.setLogoutTimestamp(new Date());
        }
        tokenRepository.saveAll(existingTokens);
     // Mettre à jour le mot de passe
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordExpired(false);
        userRepository.save(user);
    }
    
  //Réinitialise le mot de passe (haché) et marque isPasswordExpired comme vrai.
    public void resetPassword(Long userId, String tempPassword) {
    	logger.info("Mot de passe avant hachage pour userId {} : {}", userId, tempPassword);
       /* User user = userRepository.findByUserId(userId).orElse(null);
        if (user == null) {
            user = new User();
            user.setUserId(userId);
        }*/
    	User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé pour userId : " + userId));
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setPasswordExpired(true);
        userRepository.save(user);
     // Envoi de l'email avec les identifiants
       emailService.sendTemporaryPasswordEmail(
            user.getEmail(),
            String.valueOf(user.getUserId()),
            tempPassword,
            user.getNom(),
            user.getPrenom()
        );
    }
    
  //Associe un profil à un utilisateur.
    public void assignRole(Long userId, Long profilId) {
        User user = userRepository.findByUserId(userId).orElseThrow();
        Profil profil = profilRepository.findById(profilId).orElseThrow();
        user.setProfil(profil);
        userRepository.save(user);
    }
    
//logique de la connexion
    public String authenticate(Long userId, String password) {
        logger.info("Tentative d'authentification pour userId: {}", userId);
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(String.valueOf(userId), password)
            );
            logger.info("Authentification réussie pour userId: {}", userId);
            User user = userRepository.findByUserId(userId).orElseThrow();
         // Invalider tous les tokens existants avant de générer un nouveau
           /* Une déconnexion inattendue sur un autre appareil pourrait nécessiter une reconnexion fréquente.
            * Si Marie se connecte depuis un autre appareil (ex. PC puis mobile), 
            * chaque nouvelle connexion invalide la session précédente, forçant une déconnexion sur l’autre appareil.
            * Ne permet qu’une seule session active, ce qui peut être une limitation.
            * 
            * List<Token> existingTokens = tokenRepository.findByUser(user);
            for (Token token : existingTokens) {
                token.setLoggedOut(true);
                token.setLogoutTimestamp(new Date());
            }
            tokenRepository.saveAll(existingTokens);*/

            // Générer un nouveau token
            String token = jwtService.generateToken(user);
            Token tokenEntity = new Token();
            tokenEntity.setToken(token);
            tokenEntity.setLoggedOut(false);
            tokenEntity.setUser(user);
            tokenRepository.save(tokenEntity);
            return token;
        } catch (AuthenticationException e) {
            logger.error("Échec de l'authentification pour userId {} : {}", userId, e.getMessage());
            throw new RuntimeException("Authentication failed: Invalid userId or password");
        }
    }
    
  //Retourne tous les utilisateurs.
    public List<User> findAll() {
        return userRepository.findAll();
    }
 // Ajouter un nouvel agent (sans mot de passe)
    public User addAgent(Long userId, String nom, String prenom, String email, Date dateNaissance, String numeroTelephone) {
        Optional<User> existingUser = userRepository.findByUserId(userId);
        if (existingUser.isPresent()) {
            throw new RuntimeException("Un agent avec ce matricule existe déjà");
        }
        User user = new User();
        user.setUserId(userId);
        user.setNom(nom);
        user.setPrenom(prenom);
        user.setEmail(email);
        user.setDateNaissance(dateNaissance);
        user.setNumeroTelephone(numeroTelephone);
        user.setPasswordExpired(true); // Par défaut, pas de mot de passe défini
        return userRepository.save(user);
    }
 // Modifier les informations d’un agent (sans mot de passe)
    public void updateAgent(Long userId, String nom, String prenom, String email, Date dateNaissance, String numeroTelephone) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Agent non trouvé"));
        user.setNom(nom);
        user.setPrenom(prenom);
        user.setEmail(email);
        user.setDateNaissance(dateNaissance);
        user.setNumeroTelephone(numeroTelephone);
        userRepository.save(user);
    }

    // Supprimer un agent
    public void deleteAgent(Long userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Agent non trouvé"));
        userRepository.delete(user);
    }
 // Récupérer les agents avec accès (ayant un mot de passe)
    public List<User> getAgentsWithAccess() {
        return userRepository.findAll().stream()
                .filter(user -> user.getPassword() != null && !user.getPassword().isEmpty())
                .collect(Collectors.toList());
    }

    // Récupérer les agents sans accès (sans mot de passe)
    public List<User> getAgentsWithoutAccess() {
        return userRepository.findAll().stream()
                .filter(user -> user.getPassword() == null || user.getPassword().isEmpty())
                .collect(Collectors.toList());
    }
    
}