package team.budderz.buddyspace.domain.schedule.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import team.budderz.buddyspace.global.response.ErrorCode;

@Getter
@AllArgsConstructor
public enum ScheduleErrorCode implements ErrorCode {
	GROUP_NOT_FOUND(HttpStatus.NOT_FOUND.value() ,"S001","존재하지 않는 그룹입니다."),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND.value() ,"S002","존재하지 않는 유저입니다."),
	SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND.value() ,"S003","존재하지 않는 일정입니다."),
	SCHEDULE_GROUP_MISMATCH(HttpStatus.FORBIDDEN.value(), "S004", "해당 그룹에서 생성된 일정이 아닙니다."),
	SCHEDULE_AUTHOR_MISMATCH(HttpStatus.FORBIDDEN.value(), "S005", "일정 작성자만 수정할 수 있습니다.");

	private final int status;
	private final String code;
	private final String message;
}
