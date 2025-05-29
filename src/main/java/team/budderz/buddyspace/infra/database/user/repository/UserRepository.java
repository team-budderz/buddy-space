package team.budderz.buddyspace.infra.database.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.budderz.buddyspace.infra.database.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
