package team.budderz.buddyspace.api.vote.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SaveVoteRequest(
	@NotBlank(message = "투표 제목은 필수입니다")
	@Size(max = 100, message = "투표 제목은 100자를 초과할 수 없습니다")
	String title,
	@NotNull(message = "투표 옵션은 필수입니다")
 	@Size(min = 2, message = "투표 옵션은 최소 2개 이상이어야 합니다")
	List<String> options,
	boolean isAnonymous
) {
}
