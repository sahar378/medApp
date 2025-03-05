package privateApp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import privateApp.dtos.AgentRequest;
import privateApp.models.User;
import privateApp.services.UserService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/intendant/agents")
@PreAuthorize("hasAuthority('INTENDANT')") // Réservé à l’intendant
public class AgentManagementController {

    @Autowired
    private UserService userService;

    // Ajouter un agent
    @PostMapping("/add")
    public ResponseEntity<User> addAgent(@RequestBody AgentRequest request) {
        User agent = userService.addAgent(
            request.getUserId(),
            request.getNom(),
            request.getPrenom(),
            request.getEmail(),
            request.getDateNaissance(),
            request.getNumeroTelephone()
        );
        return ResponseEntity.ok(agent);
    }
    
 // Récupérer un agent par son userId
    @GetMapping("/{userId}")
    public ResponseEntity<User> getAgentById(@PathVariable Long userId) {
        User agent = userService.findUserById(userId);
        if (agent == null) {
            return ResponseEntity.status(404).body(null);
        }
        return ResponseEntity.ok(agent);
    }

    // Modifier un agent
    @PutMapping("/update/{userId}")
    public ResponseEntity<String> updateAgent(@PathVariable Long userId, @RequestBody AgentRequest request) {
        userService.updateAgent(
            userId,
            request.getNom(),
            request.getPrenom(),
            request.getEmail(),
            request.getDateNaissance(),
            request.getNumeroTelephone()
        );
        return ResponseEntity.ok("Agent mis à jour avec succès");
    }

    // Supprimer un agent
    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<String> deleteAgent(@PathVariable Long userId) {
        userService.deleteAgent(userId);
        return ResponseEntity.ok("Agent supprimé avec succès");
    }

 // Liste de tous les agents (excluant l’intendant connecté)
    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllAgents(Authentication authentication) {
        String currentUserId = authentication.getName(); // userId de l’intendant connecté (String)
        List<User> agents = userService.findAll().stream()
            .filter(user -> !user.getUserId().toString().equals(currentUserId)) // Exclure l’intendant
            .collect(Collectors.toList());
        return ResponseEntity.ok(agents);
    }

    // Liste des agents avec accès (excluant l’intendant connecté)
    @GetMapping("/with-access")
    public ResponseEntity<List<User>> getAgentsWithAccess(Authentication authentication) {
        String currentUserId = authentication.getName(); // userId de l’intendant connecté (String)
        List<User> agents = userService.getAgentsWithAccess().stream()
            .filter(user -> !user.getUserId().toString().equals(currentUserId)) // Exclure l’intendant
            .collect(Collectors.toList());
        return ResponseEntity.ok(agents);
    }

    // Liste des agents sans accès (excluant l’intendant connecté)
    @GetMapping("/without-access")
    public ResponseEntity<List<User>> getAgentsWithoutAccess(Authentication authentication) {
        String currentUserId = authentication.getName(); // userId de l’intendant connecté (String)
        List<User> agents = userService.getAgentsWithoutAccess().stream()
            .filter(user -> !user.getUserId().toString().equals(currentUserId)) // Exclure l’intendant
            .collect(Collectors.toList());
        return ResponseEntity.ok(agents);
    }
}