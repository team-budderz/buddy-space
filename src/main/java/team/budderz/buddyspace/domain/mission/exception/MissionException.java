package team.budderz.buddyspace.domain.mission.exception;

import team.budderz.buddyspace.global.exception.BaseException;

public class MissionException extends BaseException {
    public MissionException(MissionErrorCode errorCode) {
        super(errorCode);
    }
}
