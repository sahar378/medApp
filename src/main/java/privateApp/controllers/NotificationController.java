// src/main/java/privateApp/controllers/NotificationController.java
package privateApp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import privateApp.models.Notification;
import privateApp.models.User;
import privateApp.repositories.NotificationRepository;
import privateApp.dtos.NotificationDTO;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @PostMapping("/creer")
    @PreAuthorize("hasAuthority('INTENDANT')")
    public ResponseEntity<String> creerNotification(@RequestBody NotificationDTO notificationDTO) {
        User emetteur = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(); // Intendant connecté
        Notification notification = new Notification(notificationDTO.getMessage(), emetteur);
        notificationRepository.save(notification); // Une seule notification pour tous les Responsables de Stock
        return ResponseEntity.ok("Notification envoyée avec succès.");
    }

    @GetMapping
    @PreAuthorize("hasAuthority('RESPONSABLE_STOCK')")
    public ResponseEntity<List<Notification>> getNotifications() {
        return ResponseEntity.ok(notificationRepository.findAll()); // Toutes les notifications sont accessibles
    }
}