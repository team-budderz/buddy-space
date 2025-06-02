package team.budderz.buddyspace.domain.comment.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import team.budderz.buddyspace.global.response.ErrorCode;

@Getter
@AllArgsConstructor
public enum CommentErrorCode implements ErrorCode {
    GROUP_ID_NOT_FOUND(HttpStatus.NOT_FOUND.value() ,"P001","존재하지 않는 그룹 입니다.")
    , USER_ID_NOT_FOUND(HttpStatus.NOT_FOUND.value() ,"P002","존재하지 않는 유저 입니다.")
    , POST_ID_NOT_FOUND(HttpStatus.NOT_FOUND.value() ,"P003","존재하지 않는 게시글 입니다.")
    , COMMENT_ID_NOT_FOUND(HttpStatus.NOT_FOUND.value() ,"P004","존재하지 않는 댓글 입니다.")
    , UNAUTHORIZED_COMMENT_DELETE(HttpStatus.FORBIDDEN.value(), "P005", "댓글 삭제 권한이 없습니다.")

    ;


    private final int status;
    private final String code;
    private final String message;
}
