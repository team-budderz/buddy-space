package team.budderz.buddyspace.domain.vote.service;

import static team.budderz.buddyspace.domain.vote.exception.VoteErrorCode.*;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import team.budderz.buddyspace.api.vote.request.SaveVoteRequest;
import team.budderz.buddyspace.api.vote.response.SaveVoteResponse;
import team.budderz.buddyspace.domain.vote.exception.VoteException;
import team.budderz.buddyspace.infra.database.group.entity.Group;
import team.budderz.buddyspace.infra.database.group.repository.GroupRepository;
import team.budderz.buddyspace.infra.database.user.entity.User;
import team.budderz.buddyspace.infra.database.user.repository.UserRepository;
import team.budderz.buddyspace.infra.database.vote.entity.Vote;
import team.budderz.buddyspace.infra.database.vote.repository.VoteOptionRepository;
import team.budderz.buddyspace.infra.database.vote.repository.VoteRepository;

@Service
@RequiredArgsConstructor
public class VoteService {
	private final UserRepository userRepository;
	private final GroupRepository groupRepository;
	private final VoteOptionRepository voteOptionRepository;
	private final VoteRepository voteRepository;

	public SaveVoteResponse saveVote(Long userId, Long groupId, SaveVoteRequest request) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new VoteException(USER_NOT_FOUND));

		Group group = groupRepository.findById(groupId)
			.orElseThrow(() -> new VoteException(GROUP_NOT_FOUND));

		Vote vote = Vote.builder()
			.title(request.title())
			.isAnonymous(request.isAnonymous())
			.author(user)
			.group(group)
			.build();

		for (String optionName : request.options()) {
			vote.addOption(optionName);
		}

		voteRepository.save(vote);
		return SaveVoteResponse.from(vote);
	}
}
