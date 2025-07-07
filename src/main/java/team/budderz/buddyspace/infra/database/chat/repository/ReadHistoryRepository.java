package team.budderz.buddyspace.infra.database.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import team.budderz.buddyspace.infra.database.chat.entity.ReadHistory;

@Repository
public interface ReadHistoryRepository extends JpaRepository<ReadHistory, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM ReadHistory rh WHERE rh.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ReadHistory rh WHERE rh.user.id = :userId AND rh.chatRoom.group.id = :groupId")
    void deleteByUserIdAndGroupId(@Param("userId") Long userId, @Param("groupId") Long groupId);
}
