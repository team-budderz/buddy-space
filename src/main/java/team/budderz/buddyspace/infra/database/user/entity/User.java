package team.budderz.buddyspace.infra.database.user.entity;


import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import team.budderz.buddyspace.global.entity.BaseEntity;
import team.budderz.buddyspace.infra.database.attachment.entity.Attachment;
import team.budderz.buddyspace.infra.database.neighborhood.entity.Neighborhood;

import java.time.LocalDate;

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

    @Enumerated(EnumType.STRING)
    private UserProvider provider; //이넘 LOCAL,GOOGLE

    @Setter
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(columnDefinition = "BOOLEAN DEFAULT false", nullable = false)
    private boolean isDeleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_attachment_id")
    private Attachment profileAttachment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "neighborhood_id")
    private Neighborhood neighborhood;

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

    public void updateUser(String address, Neighborhood neighborhood, String phone) {
        this.address = address;
        this.neighborhood = neighborhood;
        this.phone = phone;
    }

    public void updateUserPassword(String password) {
        this.password = password;
    }

    public void updateUserAddress(String address, Neighborhood neighborhood) {
        this.address = address;
        this.neighborhood = neighborhood;
    }

    public void updateProfileAttachment(Attachment profileAttachment) {
        this.profileAttachment = profileAttachment;
    }
}
