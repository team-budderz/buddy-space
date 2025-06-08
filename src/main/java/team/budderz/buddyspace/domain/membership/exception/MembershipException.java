package team.budderz.buddyspace.domain.membership.exception;

import team.budderz.buddyspace.global.exception.BaseException;

public class MembershipException extends BaseException {
  public MembershipException(MembershipErrorCode errorCode) {
    super(errorCode);
  }
}
