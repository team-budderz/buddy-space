package team.budderz.buddyspace.api.schedule.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import team.budderz.buddyspace.api.schedule.request.SaveScheduleRequest;
import team.budderz.buddyspace.api.schedule.response.SaveScheduleResponse;
import team.budderz.buddyspace.api.schedule.response.ScheduleDetailResponse;
import team.budderz.buddyspace.api.schedule.response.ScheduleResponse;
import team.budderz.buddyspace.domain.schedule.service.ScheduleService;
import team.budderz.buddyspace.global.response.BaseResponse;
import team.budderz.buddyspace.global.security.UserAuth;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "일정 관리", description = "일정 관련 API")
public class ScheduleController {
    private final ScheduleService scheduleService;

    @Operation(
            summary = "일정 생성",
            description = "새로운 일정을 생성합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "일정 생성 성공")
            }
    )
    @PostMapping("/groups/{groupId}/schedules")
    public BaseResponse<SaveScheduleResponse> saveSchedule(
            @AuthenticationPrincipal UserAuth userAuth,
            @PathVariable Long groupId,
            @Valid @RequestBody SaveScheduleRequest request
    ) {
        return new BaseResponse<>(scheduleService.saveSchedule(userAuth.getUserId(), groupId, request));
    }

    @Operation(
            summary = "일정 수정",
            description = "일정 정보를 수정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "일정 수정 성공")
            }
    )
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

    @Operation(
            summary = "일정 삭제",
            description = "일정을 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "일정 삭제 성공")
            }
    )
    @DeleteMapping("/groups/{groupId}/schedules/{scheduleId}")
    public BaseResponse<Void> deleteSchedule(
            @AuthenticationPrincipal UserAuth userAuth,
            @PathVariable Long groupId,
            @PathVariable Long scheduleId
    ) {
        scheduleService.deleteSchedule(userAuth.getUserId(), groupId, scheduleId);
        return new BaseResponse<>(null);
    }

    @Operation(
            summary = "일정 목록 조회",
            description = "일정 목록을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "일정 목록 조회 성공")
            }
    )
    @GetMapping("/groups/{groupId}/schedules")
    public BaseResponse<List<ScheduleResponse>> findSchedulesByMonth(
            @PathVariable Long groupId,
            @RequestParam("year") int year,
            @RequestParam("month") int month
    ) {
        return new BaseResponse<>(scheduleService.findSchedulesByMonth(groupId, year, month));
    }

    @Operation(
            summary = "일정 상세 조회",
            description = "일정 상세 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "일정 상세 조회 성공")
            }
    )
    @GetMapping("/groups/{groupId}/schedules/{scheduleId}")
    public BaseResponse<ScheduleDetailResponse> findSchedule(
            @PathVariable Long groupId,
            @PathVariable Long scheduleId
    ) {
        return new BaseResponse<>(scheduleService.findSchedule(groupId, scheduleId));
    }
}
