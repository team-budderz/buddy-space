package team.budderz.buddyspace.api.user.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import team.budderz.buddyspace.infra.database.user.entity.User;
import team.budderz.buddyspace.infra.database.user.entity.UserGender;
import team.budderz.buddyspace.infra.database.user.entity.UserProvider;

import java.time.LocalDate;

public record UserDetailResponse(
        Long id,
        String name,
        String email,
        LocalDate birthDate,
        UserGender gender,
        String address,
        String phone,
        UserProvider provider,
        Boolean hasNeighborhood,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String cityName,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String districtName,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String wardName,
        Long profileAttachmentId,
        String profileImageUrl
) {
    public static UserDetailResponse from(User user, String profileImageUrl) {
        Long profileAttachmentId = null;
        if (user.getProfileAttachment() != null) profileAttachmentId = user.getProfileAttachment().getId();

        Boolean hasNeighborhood = false;
        String cityName = null;
        String districtName = null;
        String wardName = null;

        if (user.getNeighborhood() != null) {
            hasNeighborhood = true;
            cityName = user.getNeighborhood().getCityName();
            districtName = user.getNeighborhood().getDistrictName();
            wardName = user.getNeighborhood().getWardName();
        }

        return new UserDetailResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getBirthDate(),
                user.getGender(),
                user.getAddress(),
                user.getPhone(),
                user.getProvider(),
                hasNeighborhood,
                cityName,
                districtName,
                wardName,
                profileAttachmentId,
                profileImageUrl
        );
    }
}
