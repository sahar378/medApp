package privateApp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import privateApp.models.Notification;
import privateApp.models.User;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

}