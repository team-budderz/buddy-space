package team.budderz.buddyspace.global.response;

public interface ErrorCode {
    int getStatus();
    String getCode();
    String getMessage();
}
