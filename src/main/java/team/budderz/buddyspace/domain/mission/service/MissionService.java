package team.budderz.buddyspace.domain.mission.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import team.budderz.buddyspace.api.mission.request.SaveMissionRequest;
import team.budderz.buddyspace.api.mission.request.UpdateMissionRequest;
import team.budderz.buddyspace.api.mission.response.MissionDetailResponse;
import team.budderz.buddyspace.api.mission.response.MissionResponse;
import team.budderz.buddyspace.api.mission.response.SaveMissionResponse;
import team.budderz.buddyspace.api.mission.response.UpdateMissionResponse;
import team.budderz.buddyspace.domain.group.validator.GroupValidator;
import team.budderz.buddyspace.domain.mission.exception.MissionErrorCode;
import team.budderz.buddyspace.domain.mission.exception.MissionException;
import team.budderz.buddyspace.infra.database.group.entity.Group;
import team.budderz.buddyspace.infra.database.group.entity.PermissionType;
import team.budderz.buddyspace.infra.database.mission.entity.Mission;
import team.budderz.buddyspace.infra.database.mission.repository.MissionRepository;
import team.budderz.buddyspace.infra.database.user.entity.User;
import team.budderz.buddyspace.infra.database.user.repository.UserRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MissionService {

    private final MissionRepository missionRepository;
    private final UserRepository userRepository;
    private final GroupValidator validator;

    @Transactional
    public SaveMissionResponse saveMission(Long userId, Long groupId, SaveMissionRequest request) {
       validator.validatePermission(userId, groupId, PermissionType.CREATE_MISSION);
       Group group = validator.findGroupOrThrow(groupId);

       long missionCount = missionRepository.countMissionsByGroup_Id(groupId);
       if(missionCount > 1000) {
           throw new MissionException(MissionErrorCode.MISSION_LIMIT_EXCEEDED);
       }

       User user =  userRepository.findById(userId).orElseThrow(
               () -> new MissionException(MissionErrorCode.USER_NOT_FOUND)
       );

       if(request.startedAt().isAfter(request.endedAt())) {
           throw new MissionException(MissionErrorCode.INVALID_DATE_RANGE);
       }

       Mission mission = Mission.builder()
               .title(request.title())
               .description(request.description())
               .startedAt(request.startedAt())
               .endedAt(request.endedAt())
               .frequency(request.frequency())
               .author(user)
               .group(group)
               .build();

       missionRepository.save(mission);
       return SaveMissionResponse.from(mission);
    }

    @Transactional
    public UpdateMissionResponse updateMission(Long userId, Long groupId, Long missionId, UpdateMissionRequest request) {
        Mission mission = missionRepository.findById(missionId).orElseThrow(
                () -> new MissionException(MissionErrorCode.MISSION_NOT_FOUND)
        );

        validator.validateOwner(userId, groupId, mission.getAuthor().getId());

        if(!mission.getGroup().getId().equals(groupId)) {
            throw new MissionException(MissionErrorCode.MISSION_GROUP_MISMATCH);
        }

        mission.updateMission(
                request.title(),
                request.description()
        );

        return UpdateMissionResponse.from(mission);
    }

    @Transactional
    public void deleteMission(Long userId, Long groupId, Long missionId) {
        Mission mission = missionRepository.findById(missionId).orElseThrow(
                () -> new MissionException(MissionErrorCode.MISSION_NOT_FOUND)
        );

        validator.validatePermission(userId, groupId, PermissionType.DELETE_MISSION, mission.getAuthor().getId());

        if(!mission.getGroup().getId().equals(groupId)) {
            throw new MissionException(MissionErrorCode.MISSION_GROUP_MISMATCH);
        }

        missionRepository.deleteById(missionId);
    }

    public List<MissionResponse> findMissions(Long groupId) {
        validator.findGroupOrThrow(groupId);

        return missionRepository.findAllByGroup_Id(groupId)
                .stream()
                .map(MissionResponse::from)
                .toList();
    }

    public MissionDetailResponse findMissionDetail(Long groupId, Long missionId) {
        validator.findGroupOrThrow(groupId);

        Mission mission = missionRepository.findById(missionId).orElseThrow(
                () -> new MissionException(MissionErrorCode.MISSION_NOT_FOUND)
        );

        if(!mission.getGroup().getId().equals(groupId)) {
            throw new MissionException(MissionErrorCode.MISSION_GROUP_MISMATCH);
        }

        return MissionDetailResponse.from(mission);
    }
}
