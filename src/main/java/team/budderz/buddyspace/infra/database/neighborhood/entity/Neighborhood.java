package team.budderz.buddyspace.infra.database.neighborhood.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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

    @Column(nullable = true)
    private String cityName;

    @Column(nullable = true)
    private String districtName;

    @Column(nullable = true)
    private String wardName;

    @Column(precision = 9, scale = 6)
    private BigDecimal lat;

    @Column(precision = 9, scale = 6)
    private BigDecimal lng;

    @Builder
    public Neighborhood(String cityName, String districtName, String wardName, BigDecimal lat, BigDecimal lng) {
        this.cityName = cityName;
        this.districtName = districtName;
        this.wardName = wardName;
        this.lat = lat;
        this.lng = lng;
    }
}
