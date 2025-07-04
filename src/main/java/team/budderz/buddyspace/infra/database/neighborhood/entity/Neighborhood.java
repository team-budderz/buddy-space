package team.budderz.buddyspace.infra.database.neighborhood.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team.budderz.buddyspace.global.entity.BaseEntity;

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
    private String cityName;

    @Column(nullable = false)
    private String districtName;

    @Column(nullable = false)
    private String wardName;

    @Column(nullable = false)
    private String verifiedAddress;

    @Column(precision = 9, scale = 6)
    private BigDecimal lat;

    @Column(precision = 9, scale = 6)
    private BigDecimal lng;

    @Builder
    public Neighborhood(String cityName, String districtName, String wardName, String verifiedAddress, BigDecimal lat, BigDecimal lng) {
        this.cityName = cityName;
        this.districtName = districtName;
        this.wardName = wardName;
        this.verifiedAddress = verifiedAddress;
        this.lat = lat;
        this.lng = lng;
    }
}
