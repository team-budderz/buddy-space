package team.budderz.buddyspace.domain.missionpost.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import team.budderz.buddyspace.api.missionpost.request.MissionPostRequest;
import team.budderz.buddyspace.api.missionpost.response.MissionPostDetailResponse;
import team.budderz.buddyspace.api.missionpost.response.MissionPostResponse;
import team.budderz.buddyspace.domain.group.validator.GroupValidator;
import team.budderz.buddyspace.domain.missionpost.exception.MissionPostErrorCode;
import team.budderz.buddyspace.domain.missionpost.exception.MissionPostException;
import team.budderz.buddyspace.infra.database.mission.entity.Mission;
import team.budderz.buddyspace.infra.database.mission.repository.MissionRepository;
import team.budderz.buddyspace.infra.database.missionpost.entity.MissionPost;
import team.budderz.buddyspace.infra.database.missionpost.repository.MissionPostRepository;
import team.budderz.buddyspace.infra.database.user.entity.User;
import team.budderz.buddyspace.infra.database.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MissionPostService {

    private final MissionPostRepository missionPostRepository;
    private final MissionRepository missionRepository;
    private final UserRepository userRepository;
    private final GroupValidator validator;

    @Transactional
    public void saveMissionPost(Long userId, Long groupId, Long missionId, MissionPostRequest request) {
        validator.validateMember(userId, groupId);

        User user = userRepository.findById(userId).orElseThrow(
                () -> new MissionPostException(MissionPostErrorCode.USER_NOT_FOUND)
        );

        Mission mission = missionRepository.findById(missionId).orElseThrow(
                () -> new MissionPostException(MissionPostErrorCode.MISSION_NOT_FOUND)
        );

        MissionPost missionPost = MissionPost.builder()
                .contents(request.contents())
                .mission(mission)
                .author(user)
                .build();

        missionPostRepository.save(missionPost);
    }

    @Transactional
    public void updateMissionPost(Long userId, Long groupId, Long missionId, Long missionPostId, MissionPostRequest request) {
        MissionPost missionPost = missionPostRepository.findById(missionPostId).orElseThrow(
                () -> new MissionPostException(MissionPostErrorCode.MISSION_POST_NOT_FOUND)
        );

        validator.validateOwner(userId, groupId, missionPost.getAuthor().getId());

        if(!missionPost.getMission().getId().equals(missionId)) {
            throw new MissionPostException(MissionPostErrorCode.MISSION_MISMATCH);
        }

        missionPost.updateMissionPost(request.contents());
    }

    @Transactional
    public void deleteMissionPost(Long userId, Long groupId, Long missionId, Long missionPostId) {
        MissionPost missionPost = missionPostRepository.findById(missionPostId).orElseThrow(
                () -> new MissionPostException(MissionPostErrorCode.MISSION_POST_NOT_FOUND)
        );

        validator.validateOwner(userId, groupId, missionPost.getAuthor().getId());

        if(!missionPost.getMission().getId().equals(missionId)) {
            throw new MissionPostException(MissionPostErrorCode.MISSION_MISMATCH);
        }

        missionPostRepository.deleteById(missionPostId);
    }

    public List<MissionPostResponse> findMissionPosts(Long groupId, Long missionId) {
        Mission mission = missionRepository.findById(missionId).orElseThrow(
                () -> new MissionPostException(MissionPostErrorCode.MISSION_NOT_FOUND)
        );

        return missionPostRepository.findAllByMission_Group_IdAndMission_Id(groupId, missionId)
                .stream()
                .map(MissionPostResponse::from)
                .toList();
    }

    public MissionPostDetailResponse findMissionPostDetail(Long groupId, Long missionId, Long missionPostId) {
        MissionPost missionPost = missionPostRepository.findById(missionPostId).orElseThrow(
                () -> new MissionPostException(MissionPostErrorCode.MISSION_POST_NOT_FOUND)
        );

        if(!missionPost.getMission().getGroup().getId().equals(groupId)) {
            throw new MissionPostException(MissionPostErrorCode.MISSION_POST_GROUP_MISMATCH);
        }

        if(!missionPost.getMission().getId().equals(missionId)) {
            throw new MissionPostException(MissionPostErrorCode.MISSION_MISMATCH);
        }

        return MissionPostDetailResponse.from(missionPost);
    }
}
