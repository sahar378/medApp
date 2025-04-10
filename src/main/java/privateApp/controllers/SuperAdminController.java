package privateApp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import privateApp.dtos.AgentRequest;
import privateApp.dtos.ResetPasswordRequest;
import privateApp.models.User;
import privateApp.services.UserService;
import privateApp.services.EmailService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/super-admin")
@PreAuthorize("hasAuthority('SUPER_ADMIN')")
public class SuperAdminController {
    
    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @GetMapping("/intendants")
    public List<User> getAllIntendants() {
        return userService.findAll().stream()
            .filter(user -> user.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("INTENDANT")))
            .collect(Collectors.toList());
    }

    @PostMapping("/intendants/create")
    public ResponseEntity<User> createIntendant(@RequestBody AgentRequest request) {
             
        User intendant = userService.addAgent(
            request.getNom(),
            request.getPrenom(),
            request.getEmail(),
            request.getDateNaissance(),
            request.getNumeroTelephone()
        );
        intendant.setStatut(false); // Désactivé par défaut
        userService.assignRoles(intendant.getUserId(), Set.of(3L)); // 3 = ID du rôle INTENDANT
        userService.save(intendant);

        return ResponseEntity.ok(intendant);
    }

    @PostMapping("/intendants/reset-password")
    public ResponseEntity<?> resetIntendantPassword(@RequestBody ResetPasswordRequest request) {
        try {
            User user = userService.findUserById(request.getUserId());
            if (!user.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("INTENDANT"))) {
                return ResponseEntity.badRequest().body("Cet utilisateur n'est pas un intendant");
            }
            userService.resetPassword(request.getUserId(), request.getTempPassword());
            user.setPasswordExpired(true);
            userService.save(user);

            // Envoyer l'email avec les identifiants
            emailService.sendTemporaryPasswordEmail(
                user.getEmail(),
                user.getUserId().toString(),
                request.getTempPassword(),
                user.getNom(),
                user.getPrenom()
            );

            return ResponseEntity.ok("Mot de passe réinitialisé avec succès");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/intendants/toggle-status/{userId}")
    public ResponseEntity<?> toggleIntendantStatus(@PathVariable Long userId) {
        try {
            User user = userService.findUserById(userId);
            if (!user.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("INTENDANT"))) {
                return ResponseEntity.badRequest().body("Cet utilisateur n'est pas un intendant");
            }
            boolean newStatut = !user.isStatut();
            user.setStatut(newStatut);
            userService.save(user);

            if (newStatut) {
                // Email d'activation sans identifiants
                String subject = "Compte activé - PrivateApp";
                String message = "Votre compte est activé. Vous pouvez maintenant vous connecter à l'application.";
                emailService.sendStatusChangeEmail(user.getEmail(), subject, message, user.getNom(), user.getPrenom());
            } else {
                // Email de désactivation sans identifiants
                String subject = "Compte désactivé - PrivateApp";
                String message = "Votre compte intendant n'est plus valide, il est désactivé. Vous ne pouvez plus vous connecter en tant qu'intendant.";
                emailService.sendStatusChangeEmail(user.getEmail(), subject, message, user.getNom(), user.getPrenom());
            }

            return ResponseEntity.ok("Statut du compte modifié avec succès");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}