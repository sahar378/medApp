// src/main/java/privateApp/dtos/NotificationDTO.java
package privateApp.dtos;

public class NotificationDTO {
    private String message;

    // Constructeurs
    public NotificationDTO() {}

    public NotificationDTO(String message) {
        this.message = message;
    }

    // Getters et Setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}