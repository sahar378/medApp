package privateApp.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import privateApp.dtos.AssignRoleRequest;
import privateApp.dtos.AssignRolesRequest;
import privateApp.dtos.ResetPasswordRequest;
import privateApp.models.Profil;
import privateApp.models.User;
import privateApp.services.UserService;

@RestController
@RequestMapping("/api/intendant")
@PreAuthorize("hasAuthority('INTENDANT')")
public class IntendantController {
	@Autowired
    private UserService userService;
//Liste tous les utilisateurs.
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userService.findAll();
    }
//Réinitialise le mot de passe d’un utilisateur.
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            userService.resetPassword(request.getUserId(), request.getTempPassword());
            return ResponseEntity.ok("Mot de passe réinitialisé pour l'utilisateur " + request.getUserId() + ". Mot de passe temporaire : " + request.getTempPassword());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("/assign-roles")
    public ResponseEntity<?> assignRoles(@RequestBody AssignRolesRequest request) {
        userService.assignRoles(request.getUserId(), request.getProfilIds());
        return ResponseEntity.ok("Rôles assignés avec succès");
    }

   /* @GetMapping("/profils")
    public ResponseEntity<List<Profil>> getAllProfils() {
        return ResponseEntity.ok(userService.getAllProfils());
    }*/
    @GetMapping("/profils")
    public ResponseEntity<List<Profil>> getAllProfils() {
        return ResponseEntity.ok(userService.getAvailableProfilsForHabilitation());
    }
 // Nouvelle méthode pour récupérer tous les profils (y compris MEDECIN et INFIRMIER)
    @GetMapping("/all-profils")
    public ResponseEntity<List<Profil>> getAllProfilsIncludingRestricted() {
        return ResponseEntity.ok(userService.getAllProfils());
    }
}
/*Logique globale
Création d’un utilisateur :
L’intendant crée un utilisateur avec un matricule (userId) et un mot de passe temporaire via /api/intendant/reset-password.
Il assigne un rôle via /api/intendant/assign-role.
Première connexion :
L’agent se connecte via /api/auth/login avec son matricule et mot de passe temporaire.
Si isPasswordExpired est vrai, il doit changer son mot de passe via /api/auth/change-password.
Connexion suivante :
Avec un mot de passe non expiré, l’agent est redirigé vers l’URL de son profil (ex. "/stock" pour un responsable de stock).
Réinitialisation :
En cas d’oubli, l’intendant réinitialise le mot de passe, et le cycle recommence.
*/