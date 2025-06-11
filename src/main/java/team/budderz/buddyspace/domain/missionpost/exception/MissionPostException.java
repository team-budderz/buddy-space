package team.budderz.buddyspace.domain.missionpost.exception;

import team.budderz.buddyspace.global.exception.BaseException;

public class MissionPostException extends BaseException {
    public MissionPostException(MissionPostErrorCode errorCode) {
        super(errorCode);
    }
}
