package team.budderz.buddyspace.infra.database.schedule.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import team.budderz.buddyspace.infra.database.schedule.entity.Schedule;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
	@Query(""" 
		SELECT s
		FROM Schedule s JOIN FETCH s.author
		WHERE s.startAt <= :monthEnd
		AND s.endAt >= :monthStart
		AND s.group.id = :groupId
		ORDER BY s.startAt, s.id DESC
		"""
	)
	List<Schedule> findAllByMonth(Long groupId, LocalDateTime monthStart, LocalDateTime monthEnd);

	void deleteAllByGroup_Id(Long groupId);

	@Query("""
		SELECT s
		FROM Schedule s
		JOIN FETCH s.author
		WHERE s.id = :scheduleId
	""")
	Optional<Schedule> findById(Long scheduleId);
}
