package team.budderz.buddyspace.domain.vote.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import team.budderz.buddyspace.global.response.ErrorCode;

@Getter
@AllArgsConstructor
public enum VoteErrorCode implements ErrorCode {
	GROUP_NOT_FOUND(HttpStatus.NOT_FOUND.value() ,"V001","존재하지 않는 그룹입니다."),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND.value() ,"V002","존재하지 않는 유저입니다."),
	VOTE_NOT_FOUND(HttpStatus.NOT_FOUND.value() ,"V003","존재하지 않는 투표입니다."),
	VOTE_GROUP_MISMATCH(HttpStatus.FORBIDDEN.value(), "V004", "해당 그룹에서 생성된 투표가 아닙니다."),
	VOTE_AUTHOR_MISMATCH(HttpStatus.FORBIDDEN.value(), "V005", "투표 작성자만 수정/삭제할 수 있습니다."),
	VOTE_OPTION_NOT_FOUND(HttpStatus.NOT_FOUND.value() ,"V006","존재하지 않는 투표옵션이 있습니다."),
	VOTE_OPTION_MISMATCH(HttpStatus.FORBIDDEN.value(), "V007", "해당 투표의 투표옵션이 아닙니다."),
	ALREADY_CLOSED_VOTE(HttpStatus.CONFLICT.value(), "V008", "이미 종료된 투표입니다");

	private final int status;
	private final String code;
	private final String message;
}