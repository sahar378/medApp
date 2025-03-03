package privateApp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import privateApp.dtos.UpdateProfileRequest;
import privateApp.models.User;
import privateApp.services.UserService;

@RestController
@RequestMapping("/api/agent")
@PreAuthorize("isAuthenticated()") // Accessible à tous les utilisateurs authentifiés
public class AgentController {

    @Autowired
    private UserService userService;

    // Récupérer les informations du profil
    @GetMapping("/profile")
    public ResponseEntity<User> getProfile(@RequestParam Long userId) {
        User user = userService.findUserById(userId);
        if (user == null) {
            return ResponseEntity.status(404).body(null);
        }
        return ResponseEntity.ok(user);
    }

    // Mettre à jour les informations du profil
    @PutMapping("/profile")
    public ResponseEntity<String> updateProfile(@RequestBody UpdateProfileRequest request, Authentication authentication) {
        String currentUserId = authentication.getName(); // userId depuis le token
        if (!currentUserId.equals(String.valueOf(request.getUserId()))) {
            return ResponseEntity.status(403).body("Vous ne pouvez modifier que votre propre profil");
        }
        userService.updateUserProfile(
            request.getUserId(),
            request.getNom(),
            request.getPrenom(),
            request.getEmail(),
            request.getDateNaissance(),
            request.getNumeroTelephone()
        );
        return ResponseEntity.ok("Profil mis à jour avec succès");
    }
}