// src/main/java/privateApp/services/EmailService.java
package privateApp.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail; // Récupère l'email depuis application.properties

    public void sendTemporaryPasswordEmail(String toEmail, String userId, String tempPassword, String nom, String prenom) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Vos identifiants pour PrivateApp");
        message.setText(
            "Bonjour " + prenom + " " + nom + ",\n\n" +
            "Vos identifiants pour accéder à l'application PrivateApp ont été créés :\n" +
            "Matricule (userId) : " + userId + "\n" +
            "Mot de passe temporaire : " + tempPassword + "\n\n" +
            "Veuillez vous connecter à l'application et changer votre mot de passe dès que possible.\n" +
            "Cordialement,\nL'équipe de support"
        );
        message.setFrom(fromEmail); // Utilise la valeur injectée
        mailSender.send(message);
    }
}