package team.budderz.buddyspace.domain.post.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import team.budderz.buddyspace.global.response.ErrorCode;

@Getter
@AllArgsConstructor
public enum PostErrorCode implements ErrorCode {
    GROUP_ID_NOT_FOUND(HttpStatus.NOT_FOUND.value() ,"P001","존재하지 않는 그룹 입니다.")
    , USER_ID_NOT_FOUND(HttpStatus.NOT_FOUND.value() ,"P002","존재하지 않는 유저 입니다.")
    , POST_ID_NOT_FOUND(HttpStatus.NOT_FOUND.value() ,"P003","존재하지 않는 게시글 입니다.")
    , UNAUTHORIZED_POST_UPDATE(HttpStatus.FORBIDDEN.value(), "P004", "게시글 수정 권한이 없습니다.")
    , UNAUTHORIZED_POST_DELETE(HttpStatus.FORBIDDEN.value(), "P005", "게시글 삭제 권한이 없습니다.")
    , NOTICE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST.value(), "P006", "공지사항은 최대 5개까지만 등록할 수 있습니다.")
    , NOTICE_POST_ONLY_ALLOWED_BY_LEADER(HttpStatus.FORBIDDEN.value(), "P007", "공지글은 리더만 설정 가능합니다.")
    , NOT_ALLOWED_TO_EDIT_RESERVE_TIME(HttpStatus.BAD_REQUEST.value(), "P008", "예약 글이 아닌 게시글은 예약 시간을 수정할 수 없습니다.");
    ;



    private final int status;
    private final String code;
    private final String message;
}
