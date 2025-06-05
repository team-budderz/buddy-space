package team.budderz.buddyspace.domain.vote.service;

import static team.budderz.buddyspace.domain.vote.exception.VoteErrorCode.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import team.budderz.buddyspace.api.vote.request.SaveVoteRequest;
import team.budderz.buddyspace.api.vote.request.SubmitVoteRequest;
import team.budderz.buddyspace.api.vote.response.SaveVoteResponse;
import team.budderz.buddyspace.api.vote.response.VoteDetailResponse;
import team.budderz.buddyspace.api.vote.response.VoteResponse;
import team.budderz.buddyspace.domain.vote.exception.VoteException;
import team.budderz.buddyspace.infra.database.group.entity.Group;
import team.budderz.buddyspace.infra.database.group.repository.GroupRepository;
import team.budderz.buddyspace.infra.database.user.entity.User;
import team.budderz.buddyspace.infra.database.user.repository.UserRepository;
import team.budderz.buddyspace.infra.database.vote.entity.Vote;
import team.budderz.buddyspace.infra.database.vote.entity.VoteOption;
import team.budderz.buddyspace.infra.database.vote.entity.VoteSelection;
import team.budderz.buddyspace.infra.database.vote.repository.VoteOptionRepository;
import team.budderz.buddyspace.infra.database.vote.repository.VoteRepository;
import team.budderz.buddyspace.infra.database.vote.repository.VoteSelectionRepository;

@Service
@RequiredArgsConstructor
public class VoteService {
	private final UserRepository userRepository;
	private final GroupRepository groupRepository;
	private final VoteOptionRepository voteOptionRepository;
	private final VoteRepository voteRepository;
	private final VoteSelectionRepository voteSelectionRepository;

	@Transactional
	public SaveVoteResponse saveVote(Long userId, Long groupId, SaveVoteRequest request) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new VoteException(USER_NOT_FOUND));

		Group group = groupRepository.findById(groupId)
			.orElseThrow(() -> new VoteException(GROUP_NOT_FOUND));

		Vote vote = Vote.builder()
			.title(request.title())
			.isAnonymous(request.isAnonymous())
			.options(request.options())
			.author(user)
			.group(group)
			.build();

		voteRepository.save(vote);
		return SaveVoteResponse.from(vote);
	}

	@Transactional
	public SaveVoteResponse updateVote(Long userId, Long groupId, Long voteId, SaveVoteRequest request) {
		groupRepository.findById(groupId)
			.orElseThrow(() -> new VoteException(GROUP_NOT_FOUND));

		Vote vote = voteRepository.findById(voteId)
			.orElseThrow(() -> new VoteException(VOTE_NOT_FOUND));

		if (!vote.getAuthor().getId().equals(userId)) {
			throw new VoteException(VOTE_AUTHOR_MISMATCH);
		}

		if (!vote.getGroup().getId().equals(groupId)) {
			throw new VoteException(VOTE_GROUP_MISMATCH);
		}

		voteOptionRepository.deleteAllByVoteId(vote.getId());
		vote.update(request.title(), request.isAnonymous(), request.options());
		return SaveVoteResponse.from(vote);
	}

	@Transactional
	public void deleteVote(Long userId, Long groupId, Long voteId) {
		groupRepository.findById(groupId)
			.orElseThrow(() -> new VoteException(GROUP_NOT_FOUND));

		Vote vote = voteRepository.findById(voteId)
			.orElseThrow(() -> new VoteException(VOTE_NOT_FOUND));

		if (!vote.getAuthor().getId().equals(userId)) {
			throw new VoteException(VOTE_AUTHOR_MISMATCH);
		}

		if (!vote.getGroup().getId().equals(groupId)) {
			throw new VoteException(VOTE_GROUP_MISMATCH);
		}

		voteSelectionRepository.deleteAllByVoteOptionIn(voteId);
		voteOptionRepository.deleteAllByVoteId(voteId);
		voteRepository.deleteById(voteId);
	}

	public List<VoteResponse> findVote(Long groupId) {
		groupRepository.findById(groupId)
			.orElseThrow(() -> new VoteException(GROUP_NOT_FOUND));

		return voteRepository.findByGroupIdOrderByCreatedAtDesc(groupId)
			.stream()
			.map(VoteResponse::from)
			.toList();
	}

	public VoteDetailResponse findVote(Long groupId, Long voteId) {
		groupRepository.findById(groupId)
			.orElseThrow(() -> new VoteException(GROUP_NOT_FOUND));

		Vote vote = voteRepository.findById(voteId)
			.orElseThrow(() -> new VoteException(VOTE_NOT_FOUND));

		if (!vote.getGroup().getId().equals(groupId)) {
			throw new VoteException(VOTE_GROUP_MISMATCH);
		}

		// optionId 기준으로 유저 리스트 매핑
		Map<Long, List<String>> voterMap = voteSelectionRepository.findVoterNamesGroupedByOptionId(voteId);
		Map<Long, Integer> countMap = voterMap.entrySet().stream()
			.collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().size()));

		List<VoteDetailResponse.OptionDetailResponse> optionDetailResponses;

		if (!vote.isAnonymous()) {
			optionDetailResponses = vote.getOptions().stream()
				.map(option -> new VoteDetailResponse.OptionDetailResponse(
					option.getId(),
					option.getContent(),
					countMap.getOrDefault(option.getId(), 0),
					voterMap.getOrDefault(option.getId(), List.of())
				)).toList();
		} else {
			optionDetailResponses = vote.getOptions().stream()
				.map(option -> new VoteDetailResponse.OptionDetailResponse(
					option.getId(),
					option.getContent(),
					voterMap.getOrDefault(option.getId(), List.of()).size(),
					List.of()
				))
				.toList();
		}

		return VoteDetailResponse.from(vote, optionDetailResponses);
	}

	@Transactional
	public void sumbitVote(Long userId, Long groupId, Long voteId, SubmitVoteRequest request) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new VoteException(USER_NOT_FOUND));

		groupRepository.findById(groupId)
			.orElseThrow(() -> new VoteException(GROUP_NOT_FOUND));

		Vote vote = voteRepository.findById(voteId)
			.orElseThrow(() -> new VoteException(VOTE_NOT_FOUND));

		if (!vote.getGroup().getId().equals(groupId)) {
			throw new VoteException(VOTE_GROUP_MISMATCH);
		}

		// voteId에 대해 해당 유저가 한 이전 투표 삭제
		voteSelectionRepository.deleteByUserIdAndVoteId(userId, voteId);

		List<VoteOption> selectedOptions = voteOptionRepository.findAllById(request.voteOptionIds());
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
}
