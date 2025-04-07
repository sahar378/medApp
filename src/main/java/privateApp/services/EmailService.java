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