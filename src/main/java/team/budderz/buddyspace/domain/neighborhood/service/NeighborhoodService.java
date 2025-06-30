package team.budderz.buddyspace.domain.neighborhood.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import team.budderz.buddyspace.api.neighborhood.request.NeighborhoodRequest;
import team.budderz.buddyspace.api.neighborhood.response.NeighborhoodResponse;
import team.budderz.buddyspace.domain.neighborhood.exception.NeighborhoodErrorCode;
import team.budderz.buddyspace.domain.neighborhood.exception.NeighborhoodException;
import team.budderz.buddyspace.global.util.AddressNormalizer;
import team.budderz.buddyspace.infra.database.neighborhood.entity.Neighborhood;
import team.budderz.buddyspace.infra.database.neighborhood.repository.NeighborhoodRepository;
import team.budderz.buddyspace.infra.database.user.entity.User;
import team.budderz.buddyspace.infra.database.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class NeighborhoodService {

    @Value("${kakao.map.coord2address-uri}")
    private String kakaoUri;

    @Value("${KAKAO_REST_API_KEY}")
    private String kakaoApiKey;

    private final NeighborhoodRepository neighborhoodRepository;
    private final UserRepository userRepository;
    private final WebClient webClient;

    @Transactional
    public NeighborhoodResponse saveNeighborhood(Long userId, NeighborhoodRequest request) {
        User user = getUser(userId);

        // 카카오 API 호출 (위경도 → 주소)
        String url = kakaoUri + "?x=" + request.longitude() + "&y=" + request.latitude();

        // WebClient로 요청
        String responseBody = webClient.get()
                .uri(url)
                .header("Authorization", "KakaoAK " + kakaoApiKey)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // JSON 응답 바디에서 address 부분 추출
        JSONObject address = new JSONObject(responseBody)
                .getJSONArray("documents")
                .getJSONObject(0)
                .getJSONObject("address");

        if(address == null || address.isEmpty()) {
            throw new NeighborhoodException(NeighborhoodErrorCode.ADDRESS_NOT_FOUND);
        }

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
        String normalizeAddress = AddressNormalizer.normalizeAddress(newAddress); // 주소 정제
        user.updateUserAddress(normalizeAddress, neighborhood);
        userRepository.save(user);

        return NeighborhoodResponse.from(neighborhood);
    }

    public NeighborhoodResponse findNeighborhood(Long userId, Long neighborhoodId) {
        User user = getUser(userId);
        Neighborhood neighborhood = getNeighborhood(neighborhoodId);

        validateUserNeighborhood(user, neighborhood);

        return NeighborhoodResponse.from(neighborhood);
    }

    @Transactional
    public void deleteNeighborhood(Long userId, Long neighborhoodId) {
        User user = getUser(userId);
        Neighborhood neighborhood = getNeighborhood(neighborhoodId);

        validateUserNeighborhood(user, neighborhood);

        user.updateUserAddress("", null);
        userRepository.save(user);
    }

    private User getUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NeighborhoodException(NeighborhoodErrorCode.USER_NOT_FOUND)
        );

        return user;
    }

    private Neighborhood getNeighborhood(Long neighborhoodId) {
        Neighborhood neighborhood = neighborhoodRepository.findById(neighborhoodId).orElseThrow(
                () -> new NeighborhoodException(NeighborhoodErrorCode.NEIGHBORHOOD_NOT_FOUND)
        );

        return neighborhood;
    }

    private void validateUserNeighborhood(User user, Neighborhood neighborhood) {
        if(user.getNeighborhood() == null || user.getNeighborhood().getId() == null) {
            throw new NeighborhoodException(NeighborhoodErrorCode.USER_NEIGHBORHOOD_NOT_FOUND);
        }

        if(!user.getNeighborhood().getId().equals(neighborhood.getId())) {
            throw new NeighborhoodException(NeighborhoodErrorCode.USER_NEIGHBORHOOD_MISS_MATCH);
        }
    }
}
