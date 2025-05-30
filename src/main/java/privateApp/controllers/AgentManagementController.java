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

 // Ajouter un agent avec userId généré automatiquement
    @PostMapping("/add")
    public ResponseEntity<User> addAgent(@RequestBody AgentRequest request) {
        User agent = userService.addAgent(
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
    
 // Archiver un agent
    @PostMapping("/archive/{userId}")
    public ResponseEntity<String> archiveAgent(@PathVariable Long userId) {
        userService.archiveAgent(userId);
        return ResponseEntity.ok("Agent archivé avec succès");
    }

    // Supprimer un agent
    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<String> deleteAgent(@PathVariable Long userId) {
        userService.deleteAgent(userId);
        return ResponseEntity.ok("Agent supprimé avec succès");
    }

 // Liste de tous les agents 
    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllAgents(Authentication authentication) {
        List<User> agents = userService.findAll();
        return ResponseEntity.ok(agents);
    }
    
 // Liste des agents avec accès (plus d'exclusion de l’intendant connecté)
    @GetMapping("/with-access")
    public ResponseEntity<List<User>> getAgentsWithAccess() {
        List<User> agents = userService.getAgentsWithAccess();
        return ResponseEntity.ok(agents);
    }

 // Liste des agents sans accès 
    @GetMapping("/without-access")
    public ResponseEntity<List<User>> getAgentsWithoutAccess() {
        List<User> agents = userService.getAgentsWithoutAccess();
        return ResponseEntity.ok(agents);
    }

    
 // Nouvelle méthode : Liste des agents archivés
    @GetMapping("/archived")
    public ResponseEntity<List<User>> getArchivedAgents() {
        List<User> agents = userService.getArchivedAgents();
        return ResponseEntity.ok(agents);
    }
    
 // Rechercher des agents par matricule
    @GetMapping("/search")
    public ResponseEntity<List<User>> searchAgentsByMatricule(@RequestParam String matricule) {
        List<User> agents = userService.searchAgentsByMatricule(matricule);
        return ResponseEntity.ok(agents);
    }
}