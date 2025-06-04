package team.budderz.buddyspace.infra.database.membership.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "memberships")
public class Membership { // 상태 값으로 가입 경로 관리

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
