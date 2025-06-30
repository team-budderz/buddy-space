package team.budderz.buddyspace.domain.membership.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import team.budderz.buddyspace.global.response.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum MembershipErrorCode implements ErrorCode {

    MEMBERSHIP_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "M001", "모임 회원 정보를 찾을 수 없습니다."),
    ALREADY_REQUESTED(HttpStatus.BAD_REQUEST.value(), "M002", "이미 가입 요청 중인 모임입니다."),
    ALREADY_JOINED(HttpStatus.CONFLICT.value(), "M003", "이미 가입된 모임입니다."),
    BLOCKED_MEMBER(HttpStatus.FORBIDDEN.value(), "M004", "차단된 회원은 가입할 수 없습니다."),
    CANNOT_CHANGE_OWN_ROLE(HttpStatus.FORBIDDEN.value(), "M005", "자기 자신의 권한은 변경할 수 없습니다."),
    LEADER_ROLE_UNIQUE(HttpStatus.CONFLICT.value(), "M006", "모임에는 리더가 반드시 한 명만 존재해야 합니다."),
    NOT_REQUESTED_MEMBER(HttpStatus.BAD_REQUEST.value(), "M007", "해당 모임에 가입 요청 상태가 아닙니다."),
    NOT_APPROVED_MEMBER(HttpStatus.BAD_REQUEST.value(), "M008", "해당 모임에 가입된 회원이 아닙니다."),
    NOT_BLOCKED_MEMBER(HttpStatus.BAD_REQUEST.value(), "M009", "해당 모임에서 차단된 회원이 아닙니다."),
    LEADER_CANNOT_WITHDRAW(HttpStatus.BAD_REQUEST.value(), "M010", "모임의 리더는 탈퇴할 수 없습니다. 리더를 위임하거나 모임을 삭제해주세요.");


    private final int status;
    private final String code;
    private final String message;
}
