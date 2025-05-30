// src/main/java/privateApp/controllers/NotificationController.java
package privateApp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import privateApp.models.Notification;
import privateApp.models.User;
import privateApp.repositories.NotificationRepository;
import privateApp.dtos.NotificationDTO;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
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

 // NotificationController.java (modifié)
    @GetMapping
    @PreAuthorize("hasAnyAuthority('RESPONSABLE_STOCK','INTENDANT')")
    public ResponseEntity<List<Notification>> getNotifications(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        if (date != null) {
            // Conversion en plage de dates pour la journée entière
            ZoneId zone = ZoneId.systemDefault();
            Date startDate = Date.from(date.atStartOfDay(zone).toInstant());
            Date endDate = Date.from(date.plusDays(1).atStartOfDay(zone).toInstant());
            
            return ResponseEntity.ok(
                notificationRepository.findByDateCreationBetweenOrderByDateCreationDesc(startDate, endDate)
            );
        }
        return ResponseEntity.ok(notificationRepository.findAllByOrderByDateCreationDesc());
    }
}