package team.budderz.buddyspace.domain.neighborhood.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import team.budderz.buddyspace.api.neighborhood.request.NeighborhoodRequest;
import team.budderz.buddyspace.api.neighborhood.response.NeighborhoodResponse;
import team.budderz.buddyspace.domain.neighborhood.exception.NeighborhoodErrorCode;
import team.budderz.buddyspace.domain.neighborhood.exception.NeighborhoodException;
import team.budderz.buddyspace.infra.database.neighborhood.entity.Neighborhood;
import team.budderz.buddyspace.infra.database.neighborhood.repository.NeighborhoodRepository;
import team.budderz.buddyspace.infra.database.user.entity.User;
import team.budderz.buddyspace.infra.database.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class NeighborhoodService {

    @Value("${kakao.map.host}")
    private String kakaoHost;

    @Value("${kakao.map.coord2address-uri}")
    private String kakaoUri;

    @Value("${KAKAO_REST_API_KEY}")
    private String kakaoApiKey;

    private final NeighborhoodRepository neighborhoodRepository;
    private final UserRepository userRepository;

    @Transactional
    public NeighborhoodResponse saveNeighborhood(Long userId, NeighborhoodRequest request) {
        User user = getUser(userId);

        // 카카오 API 호출 (위경도 → 주소)
        String url = kakaoHost + kakaoUri + "?x=" + request.longitude() + "&y=" + request.latitude();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoApiKey);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // Spring에서 외부 API 호출용 HTTP 클라이언트
        RestTemplate restTemplate = new RestTemplate();

        // API 호출 (GET 요청 보내기)
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        // JSON 응답 바디에서 address 부분 추출
        JSONObject address = new JSONObject(response.getBody())
                .getJSONArray("documents")
                .getJSONObject(0)
                .getJSONObject("address");

        String city = address.getString("region_1depth_name");
        String district = address.getString("region_2depth_name");
        String ward = address.getString("region_3depth_name");

        Neighborhood neighborhood = neighborhoodRepository
                .findByCityNameAndDistrictNameAndWardName(city, district, ward)
                .orElseGet(
                        () -> {
                            Neighborhood newNeighborhood = Neighborhood.builder()
                                    .cityName(city)
                                    .districtName(district)
                                    .wardName(ward)
                                    .lat(request.latitude())
                                    .lng(request.longitude())
                                    .build();
                            return neighborhoodRepository.save(newNeighborhood);
                        }
                );

        String newAddress = neighborhood.getCityName() + " " + neighborhood.getDistrictName() + " " + neighborhood.getWardName();
        user.updateUserAddress(newAddress, neighborhood);
        userRepository.save(user);

        return NeighborhoodResponse.from(neighborhood);
    }

    public NeighborhoodResponse findNeighborhood(Long userId, Long neighborhoodId) {
        User user = getUser(userId);
        Neighborhood neighborhood = getNeighborhood(neighborhoodId);

        if(!user.getNeighborhood().getId().equals(neighborhood.getId())) {
            throw new NeighborhoodException(NeighborhoodErrorCode.USER_NEIGHBORHOOD_MISS_MATCH);
        }

        return NeighborhoodResponse.from(neighborhood);
    }

    @Transactional
    public void deleteNeighborhood(Long userId, Long neighborhoodId) {
        User user = getUser(userId);
        Neighborhood neighborhood = getNeighborhood(neighborhoodId);

        if(!user.getNeighborhood().getId().equals(neighborhood.getId())) {
            throw new NeighborhoodException(NeighborhoodErrorCode.USER_NEIGHBORHOOD_MISS_MATCH);
        }

        user.updateUserAddress("", null);
        userRepository.save(user);
    }

    public User getUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NeighborhoodException(NeighborhoodErrorCode.USER_NOT_FOUND)
        );

        return user;
    }

    public Neighborhood getNeighborhood(Long neighborhoodId) {
        Neighborhood neighborhood = neighborhoodRepository.findById(neighborhoodId).orElseThrow(
                () -> new NeighborhoodException(NeighborhoodErrorCode.NEIGHBORHOOD_NOT_FOUND)
        );

        return neighborhood;
    }
}
