package team.budderz.buddyspace.domain.vote.service;

import static team.budderz.buddyspace.domain.vote.exception.VoteErrorCode.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import team.budderz.buddyspace.api.vote.request.SaveVoteRequest;
import team.budderz.buddyspace.api.vote.response.SaveVoteResponse;
import team.budderz.buddyspace.api.vote.response.VoteDetailResponse;
import team.budderz.buddyspace.api.vote.response.VoteResponse;
import team.budderz.buddyspace.domain.vote.exception.VoteException;
import team.budderz.buddyspace.infra.database.group.entity.Group;
import team.budderz.buddyspace.infra.database.group.repository.GroupRepository;
import team.budderz.buddyspace.infra.database.user.entity.User;
import team.budderz.buddyspace.infra.database.user.repository.UserRepository;
import team.budderz.buddyspace.infra.database.vote.entity.Vote;
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

		return VoteDetailResponse.from(vote);
	}
}
