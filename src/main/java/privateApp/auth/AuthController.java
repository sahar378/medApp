package privateApp.auth;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

//Gère l’authentification et le changement de mot de passe.
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder; // Import requis
import org.springframework.web.bind.annotation.*;

import privateApp.dtos.AuthenticationResponse;
import privateApp.dtos.ChangePasswordRequest;
import privateApp.dtos.LoginRequest;
import privateApp.models.User;
import privateApp.services.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder; // Injection de PasswordEncoder
//Vérifie les identifiants. Si le mot de passe est expiré, demande un changement ; sinon, redirige vers l’URL du profil.
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
      try {
        String token = userService.authenticate(request.getUserId(), request.getPassword());
        User user = userService.findUserById(request.getUserId());
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("message", user.isPasswordExpired() 
          ? "Mot de passe expiré, veuillez le changer." 
          : "Connexion réussie, redirection vers : " + user.getProfil().getUrl());
        response.put("authorities", user.getAuthorities().stream()
          .map(auth -> Map.of("authority", auth.getAuthority()))
          .collect(Collectors.toList()));
        System.out.println("Réponse login: " + response); // Log pour débogage
        return ResponseEntity.ok(response);
      } catch (RuntimeException e) {
        System.out.println("Erreur login: " + e.getMessage()); // Log pour débogage
        return ResponseEntity.status(401).body("Identifiants incorrects");
      }
    }
//Met à jour le mot de passe après vérification de confirmation.
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest().body("Les mots de passe ne correspondent pas");
        }
        userService.updatePassword(request.getUserId(), request.getNewPassword());
        return ResponseEntity.ok("Mot de passe mis à jour, reconnectez-vous.");
    }
}