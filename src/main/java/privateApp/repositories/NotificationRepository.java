package privateApp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import privateApp.models.Notification;
import privateApp.models.User;

import java.util.Date;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
	List<Notification> findAllByOrderByDateCreationDesc();
	 // Nouvelle méthode de filtrage
    List<Notification> findByDateCreationBetweenOrderByDateCreationDesc(Date startDate, Date endDate);
}