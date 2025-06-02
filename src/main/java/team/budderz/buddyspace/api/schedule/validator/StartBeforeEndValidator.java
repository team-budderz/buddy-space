package team.budderz.buddyspace.api.schedule.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import team.budderz.buddyspace.api.schedule.request.SaveScheduleRequest;

public class StartBeforeEndValidator implements ConstraintValidator<ValidScheduleTime, SaveScheduleRequest> {

	@Override
	public boolean isValid(SaveScheduleRequest request, ConstraintValidatorContext context) {
		if (request.startAt() == null || request.endAt() == null) {
			return true; // @NotNull에서 이미 처리
		}
		return request.startAt().isBefore(request.endAt());
	}
}