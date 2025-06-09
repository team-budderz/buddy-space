package team.budderz.buddyspace.domain.vote.exception;

import team.budderz.buddyspace.global.exception.BaseException;

public class VoteException extends BaseException {
	public VoteException(VoteErrorCode errorCode) {
		super(errorCode);
	}
}