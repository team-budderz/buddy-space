package team.budderz.buddyspace.infra.database.user.entity;


import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import team.budderz.buddyspace.global.entity.BaseEntity;
import team.budderz.buddyspace.infra.database.neighborhood.entity.Neighborhood;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "users")
@SQLDelete(sql = "update users set is_deleted = true where id = ?")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserGender gender;

    @Column(nullable = false)
    private String address;
    
    @Column(nullable = false)
    private String phone;

    @Setter
    @Column(columnDefinition = "TEXT")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private UserProvider provider; //이넘 LOCAL,KAKAO

    @Setter
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @OneToOne
    @JoinColumn(name = "neighborhood_id", nullable = true)
    private Neighborhood neighborhood;

    @Column(columnDefinition = "BOOLEAN DEFAULT false", nullable = false)
    private boolean isDeleted;

    @Column(nullable = true)
    private LocalDateTime deletedAt;

    @Builder
    public User(String name, String email, String password, LocalDate birthDate, UserGender gender, String address, String phone, UserProvider provider, UserRole role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.birthDate = birthDate;
        this.gender = gender;
        this.address = address;
        this.phone = phone;
        this.provider = provider;
        this.role = role;
    }

    public void updateUser(String address, String phone, String imageUrl) {
        this.address = address;
        this.phone = phone;
        this.imageUrl = imageUrl;
    }

    public void updateUserPassword(String password) {
        this.password = password;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}
