package team.budderz.buddyspace.infra.database.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.budderz.buddyspace.infra.database.notification.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long>{
}
