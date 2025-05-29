package team.budderz.buddyspace.infra.database.neighborhood.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team.budderz.buddyspace.global.entity.BaseEntity;
import team.budderz.buddyspace.infra.database.user.entity.User;

import java.math.BigDecimal;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "neighborhoods")
public class Neighborhood extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String city_name;

    @Column(nullable = false)
    private String district_name;

    @Column(nullable = false)
    private String ward_name;

    @Column(precision = 9, scale = 6)
    private BigDecimal lat;

    @Column(precision = 9, scale = 6)
    private BigDecimal lng;

    @Column(nullable = false)
    private boolean isVerified;

    public Neighborhood(String city_name, String district_name, String ward_name, BigDecimal lat, BigDecimal lng, boolean isVerified) {
        this.city_name = city_name;
        this.district_name = district_name;
        this.ward_name = ward_name;
        this.lat = lat;
        this.lng = lng;
        this.isVerified = isVerified;
    }

}
