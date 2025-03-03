package privateApp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import privateApp.services.UserService;

@RestController
@RequestMapping("/api/test")
public class TestController {
    @Autowired
    private UserService userService;

    @GetMapping("/generate-hash")
    public ResponseEntity<String> generateHash() {
        userService.generateHashForIntendant();
        return ResponseEntity.ok("Hachage généré, vérifiez les logs ou la console.");
    }
}