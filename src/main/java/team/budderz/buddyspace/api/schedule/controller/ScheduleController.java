package team.budderz.buddyspace.api.schedule.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import team.budderz.buddyspace.api.schedule.request.SaveScheduleRequest;
import team.budderz.buddyspace.api.schedule.response.SaveScheduleResponse;
import team.budderz.buddyspace.api.schedule.response.ScheduleDetailResponse;
import team.budderz.buddyspace.api.schedule.response.ScheduleResponse;
import team.budderz.buddyspace.domain.schedule.service.ScheduleService;
import team.budderz.buddyspace.global.response.BaseResponse;
import team.budderz.buddyspace.global.security.UserAuth;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ScheduleController {
	private final ScheduleService scheduleService;

	@PostMapping("/groups/{groupId}/schedules")
	public BaseResponse<SaveScheduleResponse> saveSchedule(
		@AuthenticationPrincipal UserAuth userAuth,
		@PathVariable Long groupId,
		@Valid @RequestBody SaveScheduleRequest request
	) {
		return new BaseResponse<>(scheduleService.saveSchedule(userAuth.getUserId(), groupId, request));
	}

	@PutMapping("/groups/{groupId}/schedules/{scheduleId}")
	public BaseResponse<Void> updateSchedule(
		@AuthenticationPrincipal UserAuth userAuth,
		@PathVariable Long groupId,
		@PathVariable Long scheduleId,
		@Valid @RequestBody SaveScheduleRequest request
	) {
		scheduleService.updateSchedule(userAuth.getUserId(), groupId, scheduleId, request);
		return new BaseResponse<>(null);
	}

	@DeleteMapping("/groups/{groupId}/schedules/{scheduleId}")
	public BaseResponse<Void> deleteSchedule(
		@AuthenticationPrincipal UserAuth userAuth,
		@PathVariable Long groupId,
		@PathVariable Long scheduleId
	) {
		scheduleService.deleteSchedule(userAuth.getUserId(), groupId, scheduleId);
		return new BaseResponse<>(null);
	}

	@GetMapping("/groups/{groupId}/schedules")
	public BaseResponse<List<ScheduleResponse>> findSchedulesByMonth(
		@PathVariable Long groupId,
		@RequestParam("year") int year,
		@RequestParam("month") int month
	) {
		return new BaseResponse<>(scheduleService.findSchedulesByMonth(groupId, year, month));
	}

	@GetMapping("/groups/{groupId}/schedules/{scheduleId}")
	public BaseResponse<ScheduleDetailResponse> findSchedule(
		@PathVariable Long groupId,
		@PathVariable Long scheduleId
	) {
		return new BaseResponse<>(scheduleService.findSchedule(groupId, scheduleId));
	}
}
