package team.budderz.buddyspace.domain.schedule.service;

import static team.budderz.buddyspace.domain.schedule.exception.ScheduleErrorCode.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import team.budderz.buddyspace.api.schedule.request.SaveScheduleRequest;
import team.budderz.buddyspace.api.schedule.response.SaveScheduleResponse;
import team.budderz.buddyspace.api.schedule.response.ScheduleDetailResponse;
import team.budderz.buddyspace.api.schedule.response.ScheduleResponse;
import team.budderz.buddyspace.domain.group.validator.GroupValidator;
import team.budderz.buddyspace.domain.schedule.exception.ScheduleException;
import team.budderz.buddyspace.infra.database.group.entity.Group;
import team.budderz.buddyspace.infra.database.group.entity.PermissionType;
import team.budderz.buddyspace.infra.database.schedule.entity.Schedule;
import team.budderz.buddyspace.infra.database.schedule.repository.ScheduleRepository;
import team.budderz.buddyspace.infra.database.user.entity.User;
import team.budderz.buddyspace.infra.database.user.repository.UserRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class ScheduleService {
	private final ScheduleRepository scheduleRepository;
	private final UserRepository userRepository;
	private final GroupValidator validator;

	public SaveScheduleResponse saveSchedule(Long userId, Long groupId, SaveScheduleRequest request) {
		validator.validatePermission(userId, groupId, PermissionType.CREATE_SCHEDULE);
		Group group = validator.findGroupOrThrow(groupId);
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ScheduleException(USER_NOT_FOUND));

		Schedule schedule = Schedule.builder()
			.title(request.title())
			.content(request.content())
			.startAt(request.startAt())
			.endAt(request.endAt())
			.author(user)
			.group(group)
			.build();

		scheduleRepository.save(schedule);
		return SaveScheduleResponse.from(schedule);
	}

	public void updateSchedule(Long userId, Long groupId, Long scheduleId, SaveScheduleRequest request) {
		Schedule schedule = scheduleRepository.findById(scheduleId)
			.orElseThrow(() -> new ScheduleException(SCHEDULE_NOT_FOUND));
		validator.validateOwner(userId, groupId, schedule.getAuthor().getId());

		if (!Objects.equals(schedule.getGroup().getId(), groupId)) {
			throw new ScheduleException(SCHEDULE_GROUP_MISMATCH);
		}

		schedule.updateSchedule(request.title(), request.content(), request.startAt(), request.endAt());
	}

	public void deleteSchedule(Long userId, Long groupId, Long scheduleId) {
		Schedule schedule = scheduleRepository.findById(scheduleId)
			.orElseThrow(() -> new ScheduleException(SCHEDULE_NOT_FOUND));
		validator.validatePermission(userId, groupId, PermissionType.DELETE_SCHEDULE, schedule.getAuthor().getId());

		if (!Objects.equals(schedule.getGroup().getId(), groupId)) {
			throw new ScheduleException(SCHEDULE_GROUP_MISMATCH);
		}

		scheduleRepository.deleteById(scheduleId);
	}

	public List<ScheduleResponse> findSchedulesByMonth(Long groupId, int year, int month) {
		validator.findGroupOrThrow(groupId);

		LocalDate monthStartDate = LocalDate.of(year, month, 1);
		LocalDateTime monthStart = monthStartDate.atStartOfDay();
		LocalDateTime monthEnd = monthStartDate
			.withDayOfMonth(monthStartDate.lengthOfMonth())
			.atTime(23, 59, 59);

		return scheduleRepository.findAllByMonth(groupId, monthStart, monthEnd)
				.stream()
				.map(ScheduleResponse::from)
				.toList();
	}

	public ScheduleDetailResponse findSchedule(Long groupId, Long scheduleId) {
		validator.findGroupOrThrow(groupId);

		Schedule schedule = scheduleRepository.findById(scheduleId)
			.orElseThrow(() -> new ScheduleException(SCHEDULE_NOT_FOUND));

		if (!Objects.equals(schedule.getGroup().getId(), groupId)) {
			throw new ScheduleException(SCHEDULE_GROUP_MISMATCH);
		}

		return ScheduleDetailResponse.from(schedule);
	}
}
