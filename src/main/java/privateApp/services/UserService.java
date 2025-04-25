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
import java.util.Set;
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
        String rawPassword = "superadmin123";
        String hashedPassword = passwordEncoder.encode(rawPassword);
        System.out.println("Hachage pour 'superadmin123' : " + hashedPassword);
        logger.info("Hachage pour 'superadmin123' : {}", hashedPassword); // Log supplémentaire
    }
    
  //Récupère un utilisateur par son matricule.
    public User findUserById(Long userId) {
        return userRepository.findByUserId(userId).orElse(null);
    }
    
 // méthode pour mettre à jour le profil de l'utilisateur
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
    	User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé pour userId : " + userId));
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setPasswordExpired(true);
        userRepository.save(user);
     // Gestion de l'envoi d'email avec gestion d'erreur réseau
        try {
            emailService.sendTemporaryPasswordEmail(
                user.getEmail(),
                String.valueOf(user.getUserId()),
                tempPassword,
                user.getNom(),
                user.getPrenom()
            );
            logger.info("Email temporaire envoyé avec succès à {} pour userId {}", user.getEmail(), userId);
        } catch (Exception e) {
            logger.error("Échec de l'envoi de l'email pour userId {} : {}", userId, e.getMessage(), e);
            // Option 1 : Réessayer une fois
            try {
                emailService.sendTemporaryPasswordEmail(
                    user.getEmail(),
                    String.valueOf(user.getUserId()),
                    tempPassword,
                    user.getNom(),
                    user.getPrenom()
                );
                logger.info("Réessai réussi pour l'envoi de l'email à {}", user.getEmail());
            } catch (Exception e2) {
                logger.error("Échec du réessai d'envoi de l'email pour userId {} : {}", userId, e2.getMessage(), e2);
                // Option 2 : Notifier un administrateur (par log ou autre mécanisme)
                logger.warn("Connexion réseau indisponible. L'email n'a pas été envoyé pour userId {}. Veuillez vérifier la connexion.", userId);
                // Tu pourrais aussi déclencher une notification (ex. email à un admin via un autre canal)
            }
        }
    }
    /*
 // Assigner plusieurs rôles à un utilisateur
    public void assignRoles(Long userId, Set<Long> profilIds) {
        User user = userRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        Set<Profil> profils = profilIds.stream()
            .map(profilId -> profilRepository.findById(profilId)
                .orElseThrow(() -> new RuntimeException("Profil non trouvé")))
            .collect(Collectors.toSet());
        user.setProfils(profils);
        userRepository.save(user);
    }*/
    
 // Assigner plusieurs rôles à un utilisateur avec validation d'héritage
    //La logique d'héritage des rôles (PERSONNEL_MEDICAL nécessitant MEDECIN ou INFIRMIER)
    public void assignRoles(Long userId, Set<Long> profilIds) {
        User user = userRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        List<Profil> allProfils = profilRepository.findAll();
        Set<Profil> profilsToAssign = profilIds.stream()
            .map(profilId -> allProfils.stream()
                .filter(p -> p.getIdProfil().equals(profilId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Profil non trouvé : " + profilId)))
            .collect(Collectors.toSet());

        // Vérifier l'héritage des rôles MEDECIN et INFIRMIER
        boolean hasPersonnelMedical = profilsToAssign.stream()
            .anyMatch(profil -> profil.getLibelleProfil().equals("PERSONNEL_MEDICAL"));
        boolean hasMedicalSubRole = profilsToAssign.stream()
            .anyMatch(profil -> profil.getLibelleProfil().equals("MEDECIN") || profil.getLibelleProfil().equals("INFIRMIER"));

        if (hasMedicalSubRole && !hasPersonnelMedical) {
            throw new RuntimeException("Les rôles MEDECIN ou INFIRMIER nécessitent le rôle PERSONNEL_MEDICAL.");
        }

        if (hasPersonnelMedical && !hasMedicalSubRole) {
            throw new RuntimeException("Le rôle PERSONNEL_MEDICAL nécessite un sous-rôle MEDECIN ou INFIRMIER.");
        }

        // Vérifier que les rôles INTENDANT et SUPER_ADMIN ne sont pas assignés
        boolean hasForbiddenRole = profilsToAssign.stream()
            .anyMatch(profil -> profil.getLibelleProfil().equals("INTENDANT") || profil.getLibelleProfil().equals("SUPER_ADMIN"));
        if (hasForbiddenRole) {
            throw new RuntimeException("Les rôles INTENDANT et SUPER_ADMIN ne peuvent pas être assignés via l'habilitation.");
        }

        user.setProfils(profilsToAssign);
        userRepository.save(user);
    }
//logique de la connexion
 // Authentification
    public String authenticate(Long userId, String password) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(String.valueOf(userId), password)
        );
        User user = userRepository.findByUserId(userId).orElseThrow();
        String token = jwtService.generateToken(user);
        Token tokenEntity = new Token();
        tokenEntity.setToken(token);
        tokenEntity.setLoggedOut(false);
        tokenEntity.setUser(user);
        tokenRepository.save(tokenEntity);
        return token;
    }
 // Récupérer tous les profils disponibles
    public List<Profil> getAllProfils() {
        return profilRepository.findAll();
    }
    
 // Nouvelle méthode pour l'habilitation : exclut INTENDANT, SUPER_ADMIN, MEDECIN et INFIRMIER
    public List<Profil> getAvailableProfilsForHabilitation() {
        return profilRepository.findAll().stream()
            .filter(profil -> !profil.getLibelleProfil().equals("INTENDANT") &&
                             !profil.getLibelleProfil().equals("SUPER_ADMIN") &&
                             !profil.getLibelleProfil().equals("MEDECIN") &&
                             !profil.getLibelleProfil().equals("INFIRMIER"))
            .collect(Collectors.toList());
    }
    
 /* //Retourne tous les utilisateurs.
    public List<User> findAll() {
        return userRepository.findAll();
    }*/
    
 // Retourne tous les utilisateurs (exclut SUPER_ADMIN et utilisateurs archivés)
    public List<User> findAll() {
        return userRepository.findAll().stream()
            .filter(user -> user.getProfils().stream()
                .noneMatch(profil -> profil.getLibelleProfil().equals("SUPER_ADMIN")))
            .filter(user -> user.getArchived() == 0) // Exclure les utilisateurs archivés
            .collect(Collectors.toList());
    }
    
 // Récupérer les agents avec accès (ayant un mot de passe, exclut SUPER_ADMIN et utilisateurs archivés)
    public List<User> getAgentsWithAccess() {
        return userRepository.findAll().stream()
            .filter(user -> user.getPassword() != null && !user.getPassword().isEmpty())
            .filter(user -> user.getProfils().stream()
                .noneMatch(profil -> profil.getLibelleProfil().equals("SUPER_ADMIN")))
            .filter(user -> user.getArchived() == 0) // Exclure les utilisateurs archivés
            .collect(Collectors.toList());
    }
    
 // Récupérer les agents sans accès (sans mot de passe, exclut SUPER_ADMIN et utilisateurs archivés)
    public List<User> getAgentsWithoutAccess() {
        return userRepository.findAll().stream()
            .filter(user -> user.getPassword() == null || user.getPassword().isEmpty())
            .filter(user -> user.getProfils().stream()
                .noneMatch(profil -> profil.getLibelleProfil().equals("SUPER_ADMIN")))
            .filter(user -> user.getArchived() == 0) // Exclure les utilisateurs archivés
            .collect(Collectors.toList());
    }
    
 //  Récupérer les agents archivés (exclut SUPER_ADMIN)
    public List<User> getArchivedAgents() {
        return userRepository.findAll().stream()
            .filter(user -> user.getProfils().stream()
                .noneMatch(profil -> profil.getLibelleProfil().equals("SUPER_ADMIN")))
            .filter(user -> user.getArchived() == 1) // Inclure uniquement les utilisateurs archivés
            .collect(Collectors.toList());
    }
    
 //  Archiver un agent
    public void archiveAgent(Long userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Agent non trouvé"));
        // Vérifier si l'utilisateur est un intendant
        boolean isIntendant = user.getProfils().stream()
            .anyMatch(profil -> profil.getLibelleProfil().equals("INTENDANT"));
        if (isIntendant) {
            throw new RuntimeException("Les intendants ne peuvent pas être archivés via cette fonctionnalité.");
        }
        user.setArchived(1); // Marquer comme archivé
        userRepository.save(user);
    }
    
    // Générer un userId unique à partir de 1000
    private Long generateUserId() {
        Optional<User> lastUser = userRepository.findTopByOrderByUserIdDesc(); // Récupère l'utilisateur avec l'ID le plus élevé
        if (lastUser.isPresent()) {
            return lastUser.get().getUserId() + 1; // Incrémente le dernier ID
        }
        return 1000L; // Si aucun utilisateur n'existe, commence à 1000
    }
    
 // Ajouter un nouvel agent (sans mot de passe)
    public User addAgent(String nom, String prenom, String email, Date dateNaissance, String numeroTelephone) {
        Long userId = generateUserId();
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
        user.setPasswordExpired(true);
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
    /*
 // Récupérer les agents avec accès (ayant un mot de passe)
    public List<User> getAgentsWithAccess() {
        return userRepository.findAll().stream()
                .filter(user -> user.getPassword() != null && !user.getPassword().isEmpty())
                .collect(Collectors.toList());
    }
*/
    
  /*  // Récupérer les agents sans accès (sans mot de passe)
    public List<User> getAgentsWithoutAccess() {
        return userRepository.findAll().stream()
                .filter(user -> user.getPassword() == null || user.getPassword().isEmpty())
                .collect(Collectors.toList());
    }*/
 // Nouvelle méthode pour sauvegarder un utilisateur
    public void save(User user) {
        userRepository.save(user);
    }
    
}