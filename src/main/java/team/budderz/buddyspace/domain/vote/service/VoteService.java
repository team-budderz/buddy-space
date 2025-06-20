package team.budderz.buddyspace.domain.vote.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.budderz.buddyspace.api.vote.request.SaveVoteRequest;
import team.budderz.buddyspace.api.vote.request.SubmitVoteRequest;
import team.budderz.buddyspace.api.vote.response.SaveVoteResponse;
import team.budderz.buddyspace.api.vote.response.VoteDetailResponse;
import team.budderz.buddyspace.api.vote.response.VoteResponse;
import team.budderz.buddyspace.domain.group.validator.GroupValidator;
import team.budderz.buddyspace.domain.user.provider.UserProfileImageProvider;
import team.budderz.buddyspace.domain.vote.exception.VoteException;
import team.budderz.buddyspace.infra.database.group.entity.Group;
import team.budderz.buddyspace.infra.database.group.entity.PermissionType;
import team.budderz.buddyspace.infra.database.group.repository.GroupRepository;
import team.budderz.buddyspace.infra.database.user.entity.User;
import team.budderz.buddyspace.infra.database.user.repository.UserRepository;
import team.budderz.buddyspace.infra.database.vote.entity.Vote;
import team.budderz.buddyspace.infra.database.vote.entity.VoteOption;
import team.budderz.buddyspace.infra.database.vote.entity.VoteSelection;
import team.budderz.buddyspace.infra.database.vote.repository.VoteOptionRepository;
import team.budderz.buddyspace.infra.database.vote.repository.VoteRepository;
import team.budderz.buddyspace.infra.database.vote.repository.VoteSelectionRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static team.budderz.buddyspace.domain.vote.exception.VoteErrorCode.*;

@Service
@RequiredArgsConstructor
public class VoteService {
	private final UserRepository userRepository;
	private final GroupRepository groupRepository;
	private final VoteOptionRepository voteOptionRepository;
	private final VoteRepository voteRepository;
	private final VoteSelectionRepository voteSelectionRepository;
	private final GroupValidator validator;
	private final UserProfileImageProvider profileImageProvider;

	@Transactional
	public SaveVoteResponse saveVote(Long userId, Long groupId, SaveVoteRequest request) {
		validator.validatePermission(userId, groupId, PermissionType.CREATE_VOTE);
		Group group = validator.findGroupOrThrow(groupId);
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new VoteException(USER_NOT_FOUND));
		String profileImageUrl = profileImageProvider.getProfileImageUrl(user);

		Vote vote = Vote.builder()
			.title(request.title())
			.isAnonymous(request.isAnonymous())
			.options(request.options())
			.author(user)
			.group(group)
			.build();

		voteRepository.save(vote);
		return SaveVoteResponse.from(vote, profileImageUrl);
	}

	@Transactional
	public SaveVoteResponse updateVote(Long userId, Long groupId, Long voteId, SaveVoteRequest request) {
		Vote vote = voteRepository.findById(voteId)
			.orElseThrow(() -> new VoteException(VOTE_NOT_FOUND));
		validator.validateOwner(userId, groupId, vote.getAuthor().getId());

		if (!vote.getGroup().getId().equals(groupId)) {
			throw new VoteException(VOTE_GROUP_MISMATCH);
		}

		voteSelectionRepository.deleteAllByVoteOptionIn(voteId);
		voteOptionRepository.deleteAllByVoteId(vote.getId());
		vote.update(request.title(), request.isAnonymous(), request.options());

		String profileImageUrl = profileImageProvider.getProfileImageUrl(vote.getAuthor());
		return SaveVoteResponse.from(vote, profileImageUrl);
	}

	@Transactional
	public void deleteVote(Long userId, Long groupId, Long voteId) {
		Vote vote = voteRepository.findById(voteId)
			.orElseThrow(() -> new VoteException(VOTE_NOT_FOUND));
		validator.validatePermission(userId, groupId, PermissionType.DELETE_VOTE, vote.getAuthor().getId());

		if (!vote.getGroup().getId().equals(groupId)) {
			throw new VoteException(VOTE_GROUP_MISMATCH);
		}

		voteSelectionRepository.deleteAllByVoteOptionIn(voteId);
		voteOptionRepository.deleteAllByVoteId(voteId);
		voteRepository.deleteById(voteId);
	}

	@Transactional(readOnly = true)
	public List<VoteResponse> findVote(Long groupId) {
		validator.findGroupOrThrow(groupId);

		return voteRepository.findByGroupIdOrderByCreatedAtDesc(groupId)
			.stream()
			.map(VoteResponse::from)
			.toList();
	}

	@Transactional(readOnly = true)
	public VoteDetailResponse findVote(Long groupId, Long voteId) {
		validator.findGroupOrThrow(groupId);

		Vote vote = voteRepository.findById(voteId)
			.orElseThrow(() -> new VoteException(VOTE_NOT_FOUND));

		if (!vote.getGroup().getId().equals(groupId)) {
			throw new VoteException(VOTE_GROUP_MISMATCH);
		}

		String profileImageUrl = profileImageProvider.getProfileImageUrl(vote.getAuthor());

		List<VoteDetailResponse.OptionDetailResponse> optionDetailResponses = getOptionDetailResponses(vote);
		return VoteDetailResponse.from(vote, optionDetailResponses, profileImageUrl);
	}

	private List<VoteDetailResponse.OptionDetailResponse> getOptionDetailResponses(Vote vote) {
		Map<Long, List<String>> voterMap = voteSelectionRepository.findVoterNamesGroupedByOptionId(vote.getId());
		Map<Long, Integer> countMap = voterMap.entrySet().stream()
			.collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().size()));

		if (!vote.isAnonymous()) {
			return vote.getOptions().stream()
				.map(option -> new VoteDetailResponse.OptionDetailResponse(
					option.getId(),
					option.getContent(),
					countMap.getOrDefault(option.getId(), 0),
					voterMap.getOrDefault(option.getId(), List.of())
				))
				.sorted(Comparator.comparingInt(VoteDetailResponse.OptionDetailResponse::voteCount).reversed())
				.toList();
		} else {
			return vote.getOptions().stream()
				.map(option -> new VoteDetailResponse.OptionDetailResponse(
					option.getId(),
					option.getContent(),
					voterMap.getOrDefault(option.getId(), List.of()).size(),
					List.of()
				))
				.sorted(Comparator.comparingInt(VoteDetailResponse.OptionDetailResponse::voteCount).reversed())
				.toList();
		}
	}

	@Transactional
	public void submitVote(Long userId, Long groupId, Long voteId, SubmitVoteRequest request) {
		validator.validateMember(userId, groupId);

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new VoteException(USER_NOT_FOUND));

		Vote vote = voteRepository.findById(voteId)
			.orElseThrow(() -> new VoteException(VOTE_NOT_FOUND));

		if (!vote.getGroup().getId().equals(groupId)) {
			throw new VoteException(VOTE_GROUP_MISMATCH);
		}

		// voteId에 대해 해당 유저가 한 이전 투표 삭제
		voteSelectionRepository.deleteByUserIdAndVoteId(userId, voteId);
		saveVoteOptions(voteId, request.voteOptionIds(), user);
	}

	private void saveVoteOptions(Long voteId, List<Long> voteOptionIds, User user) {
		List<VoteOption> selectedOptions = voteOptionRepository.findAllById(voteOptionIds);
		if (selectedOptions.size() != voteOptionIds.size()) {
			throw new VoteException(VOTE_OPTION_NOT_FOUND);
		}
		for (VoteOption option : selectedOptions) {
			// 요청된 voteOptionId가 해당 vote에 속하는지 검증
			if (!option.getVote().getId().equals(voteId)) {
				throw new VoteException(VOTE_OPTION_MISMATCH);
			}

			// 새로운 투표 저장
			VoteSelection voteSelection = VoteSelection.builder()
				.user(user)
				.voteOption(option)
				.build();
			voteSelectionRepository.save(voteSelection);
		}
	}

	@Transactional
	public void closeVote(Long userId, Long groupId, Long voteId) {
		Vote vote = voteRepository.findById(voteId)
			.orElseThrow(() -> new VoteException(VOTE_NOT_FOUND));
		validator.validateOwner(userId, groupId, vote.getAuthor().getId());

		if (!vote.getGroup().getId().equals(groupId)) {
			throw new VoteException(VOTE_GROUP_MISMATCH);
		}

		if (vote.isClosed()) {
			throw new VoteException(ALREADY_CLOSED_VOTE);
		}

		vote.close();
	}
}
