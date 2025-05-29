package team.budderz.buddyspace.infra.database.user.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import team.budderz.buddyspace.global.entity.BaseEntity;
import team.budderz.buddyspace.infra.database.neighborhood.entity.Neighborhood;
import java.time.LocalDate;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "users")
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

    @Column(nullable = false)
    private UserRole role;

    @OneToOne
    @JoinColumn(name = "neighborhood_id", nullable = true)
    private Neighborhood neighborhood;

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
}
