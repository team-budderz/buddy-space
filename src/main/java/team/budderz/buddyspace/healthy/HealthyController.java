package team.budderz.buddyspace.healthy;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import team.budderz.buddyspace.global.response.BaseResponse;
import team.budderz.buddyspace.infra.database.schedule.entity.Schedule;
import team.budderz.buddyspace.infra.database.schedule.repository.ScheduleRepository;

@RestController
@RequiredArgsConstructor
public class HealthyController {
	public final ScheduleRepository scheduleRepository;

	@GetMapping("/api/healthy-check")
	public BaseResponse<List<Schedule>> healthyCheck() {
		List<Schedule> healthyList = scheduleRepository.findAll();
		return new BaseResponse<>(healthyList);
	}
}
