package privateApp.auth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import privateApp.dtos.ChangePasswordRequest;
import privateApp.dtos.LoginRequest;
import privateApp.models.Profil;
import privateApp.models.User;
import privateApp.services.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            String token = userService.authenticate(request.getUserId(), request.getPassword());
            User user = userService.findUserById(request.getUserId());
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("message", user.isPasswordExpired() 
                ? "Mot de passe expiré, veuillez le changer." 
                : "Connexion réussie");

            // Créer une liste de profils avec rôle, URL et descriptif
            List<Map<String, String>> profiles = user.getProfils().stream().map(profil -> {
                Map<String, String> profileData = new HashMap<>();
                profileData.put("role", profil.getLibelleProfil());
                profileData.put("url", profil.getUrl());
                profileData.put("descriptif", profil.getDescriptifAffiche());
                return profileData;
            }).collect(Collectors.toList());

            response.put("profiles", profiles);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body("Identifiants incorrects");
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest().body("Les mots de passe ne correspondent pas");
        }
        userService.updatePassword(request.getUserId(), request.getNewPassword());
        return ResponseEntity.ok("Mot de passe mis à jour, reconnectez-vous.");
    }
}