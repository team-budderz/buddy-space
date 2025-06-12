package team.budderz.buddyspace.domain.neighborhood.exception;

import team.budderz.buddyspace.global.exception.BaseException;

public class NeighborhoodException extends BaseException {
    public NeighborhoodException(NeighborhoodErrorCode errorCode) {
        super(errorCode);
    }
}
