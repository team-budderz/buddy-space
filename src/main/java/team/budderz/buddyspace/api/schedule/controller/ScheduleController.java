package team.budderz.buddyspace.api.schedule.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import team.budderz.buddyspace.api.schedule.request.SaveScheduleRequest;
import team.budderz.buddyspace.api.schedule.response.SaveScheduleResponse;
import team.budderz.buddyspace.domain.schedule.service.ScheduleService;
import team.budderz.buddyspace.global.response.BaseResponse;

@RestController
@RequiredArgsConstructor
public class ScheduleController {
	private final ScheduleService scheduleService;

	@PostMapping("/groups/{groupId}/schedules")
	public BaseResponse<SaveScheduleResponse> saveSchedule(
		// TODO: userId
		@PathVariable Long groupId,
		@Valid @RequestBody SaveScheduleRequest request
	) {
		Long userId = 1L;
		return new BaseResponse<>(scheduleService.saveSchedule(userId, groupId, request));
	}

	@PostMapping("/groups/{groupId}/schedules/{scheduleId}")
	public BaseResponse<Void> updateSchedule(
		// TODO: userId
		@PathVariable Long groupId,
		@PathVariable Long scheduleId,
		@Valid @RequestBody SaveScheduleRequest request
	) {
		Long userId = 1L;
		scheduleService.updateSchedule(userId, groupId, scheduleId, request);
		return new BaseResponse<>(null);
	}

	@DeleteMapping("/groups/{groupId}/schedules/{scheduleId}")
	public BaseResponse<Void> deleteSchedule(
		// TODO: userId
		@PathVariable Long groupId,
		@PathVariable Long scheduleId
	) {
		Long userId = 1L;
		scheduleService.deleteSchedule(userId, groupId, scheduleId);
		return new BaseResponse<>(null);
	}
}
