package team.budderz.buddyspace.domain.schedule.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import team.budderz.buddyspace.global.response.ErrorCode;

@Getter
@AllArgsConstructor
public enum ScheduleErrorCode implements ErrorCode {
	GROUP_NOT_FOUND(HttpStatus.NOT_FOUND.value() ,"S001","존재하지 않는 그룹 입니다."),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND.value() ,"S002","존재하지 않는 유저 입니다.");

	private final int status;
	private final String code;
	private final String message;
}
