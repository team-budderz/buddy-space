package team.budderz.buddyspace.api.vote.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "투표 생성 요청 DTO")
public record SaveVoteRequest(
	@NotBlank(message = "투표 제목은 필수입니다")
	@Size(max = 100, message = "투표 제목은 100자를 초과할 수 없습니다")
	@Schema(description = "투표 제목", example = "오늘 저녁 메뉴")
	String title,

	@NotNull(message = "투표 옵션은 필수입니다")
 	@Size(min = 2, message = "투표 옵션은 최소 2개 이상이어야 합니다")
	@Schema(description = "투표 옵션", example = "[\"햄버거\", \"떡볶이\"]")
	List<String> options,

	@Schema(description = "익명 투표 여부", example = "false")
	boolean isAnonymous
) {
}
