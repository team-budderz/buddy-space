package team.budderz.buddyspace.domain.group.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import team.budderz.buddyspace.global.response.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum GroupErrorCode implements ErrorCode {

    GROUP_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "G001", "해당 모임을 찾을 수 없습니다."),
    NEIGHBORHOOD_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "N001", "해당 사용자의 동네 정보를 찾을 수 없습니다."),
    GROUP_PERMISSION_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "G002", "모임 권한 정보를 찾을 수 없습니다."),
    CONTENT_OWNER_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "G003", "콘텐츠가 존재하지 않거나 작성자 정보를 찾을 수 없습니다."),

    GROUP_TYPE_NOT_FOUND(HttpStatus.BAD_REQUEST.value(), "G004", "유효하지 않은 모임 유형입니다."),
    INTEREST_NOT_FOUND(HttpStatus.BAD_REQUEST.value(), "G005", "유효하지 않은 모임 관심사입니다."),
    INVALID_GROUP_SORT_TYPE(HttpStatus.BAD_REQUEST.value(), "G006", "지원하지 않는 정렬 방식입니다."),
    MEMBERS_EXIST_IN_GROUP(HttpStatus.BAD_REQUEST.value(), "G007", "멤버가 존재하는 모임은 삭제할 수 없습니다. 리더를 위임하거나 모든 멤버를 탈퇴시켜야 합니다."),
    INVALID_PERMISSION_SETTING(HttpStatus.BAD_REQUEST.value(), "G008", "삭제 권한은 리더와 부리더만 설정 가능합니다."),
    PERMISSION_TYPE_NOT_SUPPORTED(HttpStatus.BAD_REQUEST.value(), "G009", "지원하지 않는 모임 권한 유형입니다."),
    UNSUPPORTED_PERMISSION_VALIDATION(HttpStatus.BAD_REQUEST.value(), "G010", "권한 검증을 지원하지 않는 기능입니다."),
    MISSING_PERMISSION_TYPE(HttpStatus.BAD_REQUEST.value(), "G011", "모든 권한 유형에 대한 설정이 필요합니다."),
    INVALID_IMAGE_TYPE(HttpStatus.BAD_REQUEST.value(), "G012", "모임 커버는 이미지 형식만 설정 가능합니다."),
    INVALID_GROUP_TYPE(HttpStatus.BAD_REQUEST.value(), "G013", "온라인 모임은 동네 관련 설정을 할 수 없습니다."),
    LEADER_ADDRESS_NOT_FOUND(HttpStatus.BAD_REQUEST.value(), "G014", "모임 리더의 동네 정보를 찾을 수 없습니다."),
    LEADER_VERIFICATION_NOT_FOUND(HttpStatus.BAD_REQUEST.value(), "G015", "모임 리더의 동네 인증 정보를 찾을 수 없습니다."),
    NEIGHBOR_VERIFICATION_REQUIRED(HttpStatus.FORBIDDEN.value(), "G016", "동네 인증 사용자만 가입 요청할 수 있는 모임입니다."),
    NOT_A_NEIGHBOR_GROUP(HttpStatus.BAD_REQUEST.value(), "G017", "동네 모임이 아닙니다. 동네를 변경하거나 초대 링크를 통해 가입하세요."),

    FUNCTION_ACCESS_DENIED(HttpStatus.FORBIDDEN.value(), "G018", "해당 기능에 대한 접근 권한이 없습니다.");

    private final int status;
    private final String code;
    private final String message;
}
