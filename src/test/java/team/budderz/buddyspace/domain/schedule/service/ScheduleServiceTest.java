package team.budderz.buddyspace.domain.schedule.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import team.budderz.buddyspace.api.schedule.request.SaveScheduleRequest;
import team.budderz.buddyspace.domain.schedule.exception.ScheduleErrorCode;
import team.budderz.buddyspace.domain.schedule.exception.ScheduleException;
import team.budderz.buddyspace.infra.database.group.entity.Group;
import team.budderz.buddyspace.infra.database.group.repository.GroupRepository;
import team.budderz.buddyspace.infra.database.schedule.entity.Schedule;
import team.budderz.buddyspace.infra.database.schedule.repository.ScheduleRepository;
import team.budderz.buddyspace.infra.database.user.entity.User;
import team.budderz.buddyspace.infra.database.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {
	@InjectMocks
	private ScheduleService scheduleService; // saveSchedule 메서드가 있는 서비스 클래스명

	@Mock
	private UserRepository userRepository;

	@Mock
	private GroupRepository groupRepository;

	@Mock
	private ScheduleRepository scheduleRepository;
	private Long userId;
	private Long groupId;
	private SaveScheduleRequest saveScheduleRequest;

	@BeforeEach
	void setUp() {
		userId = 1L;
		groupId = 2L;
		saveScheduleRequest = new SaveScheduleRequest("제목", "내용",
			LocalDateTime.of(2025,5,30,10,0),
			LocalDateTime.of(2025,5,30,12,0));
	}

	@Test
	void saveSchedule_Success() {
		// given
		User user = mock(User.class);
		Group group = mock(Group.class);
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
		when(scheduleRepository.save(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ArgumentCaptor<Schedule> scheduleCaptor = ArgumentCaptor.forClass(Schedule.class);

		// when
		scheduleService.saveSchedule(userId, groupId, saveScheduleRequest);

		// then
		verify(scheduleRepository, times(1)).save(scheduleCaptor.capture());
		Schedule savedSchedule = scheduleCaptor.getValue();
		assertThat(savedSchedule.getAuthor()).isEqualTo(user);
		assertThat(savedSchedule.getGroup()).isEqualTo(group);
	}

	@Test
	void saveSchedule_UserNotFound_ThrowsException() {
		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		ScheduleException ex = assertThrows(ScheduleException.class,
			() -> scheduleService.saveSchedule(userId, groupId, saveScheduleRequest));

		assertEquals(ScheduleErrorCode.USER_NOT_FOUND, ex.getErrorCode());
		verify(scheduleRepository, never()).save(any());
	}

	@Test
	void saveSchedule_GroupNotFound_ThrowsException() {
		User user = mock(User.class);
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

		ScheduleException ex = assertThrows(ScheduleException.class,
			() -> scheduleService.saveSchedule(userId, groupId, saveScheduleRequest));

		assertEquals(ScheduleErrorCode.GROUP_NOT_FOUND, ex.getErrorCode());
		verify(scheduleRepository, never()).save(any());
	}

}