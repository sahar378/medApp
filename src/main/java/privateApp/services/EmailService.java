// src/main/java/privateApp/services/EmailService.java
package privateApp.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import privateApp.models.BonCommande;
import privateApp.models.LigneCommande;

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
        StringBuilder emailBody = new StringBuilder();
        emailBody.append("Bonjour ").append(prenom).append(" ").append(nom).append(",\n\n")
                 .append("Voici vos identifiants pour accéder à l'application PrivateApp :\n")
                 .append("Matricule (userId) : ").append(userId).append("\n")
                 .append("Mot de passe temporaire : ").append(tempPassword).append("\n\n")
                 .append("Ce mot de passe est temporaire. Veuillez le modifier après votre première connexion à l'application.\n")
                 .append("Cordialement,\nL'équipe de support");
        message.setText(emailBody.toString());
        message.setFrom(fromEmail);
        mailSender.send(message);
    }
    public void sendStatusChangeEmail(String toEmail, String subject, String body, String nom, String prenom) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(
            "Bonjour " + prenom + " " + nom + ",\n\n" +
            body + "\n\n" +
            "Cordialement,\nL'équipe de support"
        );
        message.setFrom(fromEmail);
        mailSender.send(message);
    }
    public void sendBonCommandeEmail(BonCommande bonCommande) throws MailSendException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(bonCommande.getFournisseur().getEmail());
        message.setSubject("Nouveau Bon de Commande #" + bonCommande.getIdBonCommande());
        
        StringBuilder emailBody = new StringBuilder();
        emailBody.append("Bonjour ").append(bonCommande.getFournisseur().getNom()).append(",\n\n");
        emailBody.append("Nous vous envoyons un nouveau bon de commande :\n");
        emailBody.append("Numéro : #").append(bonCommande.getIdBonCommande()).append("\n");
        emailBody.append("Date : ").append(bonCommande.getDate().toString()).append("\n\n");
        emailBody.append("Détails des produits commandés :\n");
        
        for (LigneCommande ligne : bonCommande.getLignesCommande()) {
            emailBody.append("- ").append(ligne.getProduit().getNom())
                     .append(" : ").append(ligne.getQuantite()).append(" unités\n");
        }
        
        emailBody.append("\nVeuillez confirmer la réception de ce bon et nous informer des délais de livraison.\n");
        emailBody.append("Cordialement,\nL’équipe de PrivateApp");

        message.setText(emailBody.toString());
        message.setFrom(fromEmail);
        mailSender.send(message); // Lève une exception si pas de connexion
    }
}