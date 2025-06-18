package team.budderz.buddyspace.infra.database.notification.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import team.budderz.buddyspace.infra.database.notification.entity.Notification;
import team.budderz.buddyspace.infra.database.user.entity.User;

public interface NotificationRepository extends JpaRepository<Notification, Long>{
    Page<Notification> findAllByUserOrderByCreatedAtDesc(User user, Pageable pageable);
}
