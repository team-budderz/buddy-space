package team.budderz.buddyspace.domain.comment.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import team.budderz.buddyspace.global.response.ErrorCode;

@Getter
@AllArgsConstructor
public enum CommentErrorCode implements ErrorCode {
    GROUP_ID_NOT_FOUND(HttpStatus.NOT_FOUND.value() ,"C001","존재하지 않는 그룹 입니다.")
    , USER_ID_NOT_FOUND(HttpStatus.NOT_FOUND.value() ,"C002","존재하지 않는 유저 입니다.")
    , POST_ID_NOT_FOUND(HttpStatus.NOT_FOUND.value() ,"C003","존재하지 않는 게시글 입니다.")
    , COMMENT_ID_NOT_FOUND(HttpStatus.NOT_FOUND.value() ,"C004","존재하지 않는 댓글 입니다.")
    , UNAUTHORIZED_COMMENT_DELETE(HttpStatus.FORBIDDEN.value(), "C005", "댓글 삭제 권한이 없습니다.")
    , UNAUTHORIZED_COMMENT_UPDATE(HttpStatus.FORBIDDEN.value(), "C006", "댓글 수정 권한이 없습니다.")
    , COMMENT_NOT_BELONG_TO_POST( HttpStatus.BAD_REQUEST.value(), "C007", "해당 게시글에 속한 댓글이 아닙니다.")

    ;


    private final int status;
    private final String code;
    private final String message;
}
