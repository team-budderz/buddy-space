package team.budderz.buddyspace.domain.schedule.service;

import static team.budderz.buddyspace.domain.schedule.exception.ScheduleErrorCode.*;

import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import team.budderz.buddyspace.api.schedule.request.SaveScheduleRequest;
import team.budderz.buddyspace.api.schedule.response.SaveScheduleResponse;
import team.budderz.buddyspace.domain.schedule.exception.ScheduleException;
import team.budderz.buddyspace.infra.database.group.entity.Group;
import team.budderz.buddyspace.infra.database.group.repository.GroupRepository;
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
	private final GroupRepository groupRepository;

	public SaveScheduleResponse saveSchedule(Long userId, Long groupId, SaveScheduleRequest request) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ScheduleException(USER_NOT_FOUND));

		Group group = groupRepository.findById(groupId)
			.orElseThrow(() -> new ScheduleException(GROUP_NOT_FOUND));

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
		userRepository.findById(userId)
			.orElseThrow(() -> new ScheduleException(USER_NOT_FOUND));

		groupRepository.findById(groupId)
			.orElseThrow(() -> new ScheduleException(GROUP_NOT_FOUND));

		Schedule schedule = scheduleRepository.findById(scheduleId)
			.orElseThrow(() -> new ScheduleException(SCHEDULE_NOT_FOUND));

		if (!Objects.equals(schedule.getGroup().getId(), groupId)) {
			throw new ScheduleException(SCHEDULE_GROUP_MISMATCH);
		}

		if (!Objects.equals(schedule.getAuthor().getId(), userId)) {
			throw new ScheduleException(SCHEDULE_AUTHOR_MISMATCH);
		}

		// 일정 수정
		schedule.updateSchedule(request.title(), request.content(), request.startAt(), request.endAt());
	}

	public void deleteSchedule(Long userId, Long groupId, Long scheduleId) {
		userRepository.findById(userId)
			.orElseThrow(() -> new ScheduleException(USER_NOT_FOUND));

		groupRepository.findById(groupId)
			.orElseThrow(() -> new ScheduleException(GROUP_NOT_FOUND));

		Schedule schedule = scheduleRepository.findById(scheduleId)
			.orElseThrow(() -> new ScheduleException(SCHEDULE_NOT_FOUND));

		if (!Objects.equals(schedule.getGroup().getId(), groupId)) {
			throw new ScheduleException(SCHEDULE_GROUP_MISMATCH);
		}

		if (!Objects.equals(schedule.getAuthor().getId(), userId)) {
			throw new ScheduleException(SCHEDULE_AUTHOR_MISMATCH);
		}

		scheduleRepository.deleteById(scheduleId);
	}
}
