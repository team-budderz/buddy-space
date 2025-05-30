package team.budderz.buddyspace.api.schedule.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SaveScheduleRequest (
	@NotBlank
	String title,
	@NotBlank
	String content,
	@NotNull
	LocalDateTime startAt,
	@NotNull
	LocalDateTime endAt
) {
}
