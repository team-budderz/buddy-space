package team.budderz.buddyspace.domain.schedule.exception;

import team.budderz.buddyspace.global.exception.BaseException;

public class ScheduleException extends BaseException {
	public ScheduleException(ScheduleErrorCode errorCode) {
		super(errorCode);
	}
}
