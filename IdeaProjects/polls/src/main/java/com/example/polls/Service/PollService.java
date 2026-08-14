package com.example.polls.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.example.polls.Payload.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.data.domain.Sort;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;

import com.example.polls.Exception.BadRequestException;
import com.example.polls.Exception.ResourceNotFoundException;
import com.example.polls.models.*;
import com.example.polls.repository.PollRepository;
import com.example.polls.repository.UserRepository;
import com.example.polls.repository.VoteRepository;
import com.example.polls.util.AppConstants;
import com.example.polls.util.ModelMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Service
public class PollService {
	
	@Autowired 
	PollRepository pollRepository;

	@Autowired 
	UserRepository userRepository;
	
	@Autowired 
	VoteRepository voteRepository;
	
    private static final Logger logger = LoggerFactory.getLogger(PollService.class);
	private final Sinks.Many<VoteResponse> votesStream= Sinks.many()
			.multicast()
			.onBackpressureBuffer();
    
	public PagedResponse<PollResponse> getAllPolls(String authenticatedUserId, int page, int size) {
		validatePageNumberAndSize(page,size);
		Pageable  pageable=PageRequest.of(page, size, Sort.Direction.DESC,"createdAt");
		Page<Poll> pagedPoll=pollRepository.findAll(pageable);
		if(pagedPoll.getNumberOfElements()==0) {
			return new PagedResponse<>(Collections.emptyList(),pagedPoll.getNumber(),pagedPoll.getSize(),pagedPoll.getTotalElements(),
					                                         pagedPoll.getTotalPages(),pagedPoll.isLast());
		}
		List<Long> PollIds=pagedPoll.map(Poll::getId).getContent();
        Map<Long,Long> ChoiceVoteCountMap=getChoiceVotecountMap(PollIds);
        Map<Long,Long> PollUserVoteMap=getPolllUservoteMap(authenticatedUserId,PollIds);
        Map<String,User> CreatorMap=getPollCreatorMap(pagedPoll.getContent());

		List<PollResponse> pollResponse=pagedPoll.map(poll -> {
			return ModelMapper.mapPollToPollResponse(poll, 
					ChoiceVoteCountMap, 
					CreatorMap.get(poll.getCreatedBy()), 
					PollUserVoteMap == null ? null:PollUserVoteMap.getOrDefault(poll.getId(), null));					
		}).getContent();
		return new PagedResponse<>(pollResponse,pagedPoll.getNumber(),pagedPoll.getSize(),pagedPoll.getTotalElements(),
                pagedPoll.getTotalPages(),pagedPoll.isLast());
	}

	private Map<String, User> getPollCreatorMap(List<Poll> polls) {
		   List<String> CreatorIds= polls.stream()
				   .map(Poll::getCreatedBy)
				   .toList();

	  List<User> creatorList= userRepository.findUsersByIdIn(CreatorIds);

		return creatorList.stream()
				         .collect(Collectors.toMap(User::getId, Function.identity())
						 );
	}

	private Map<Long, Long> getPolllUservoteMap(String authenticatedUserId, List<Long> pollIds)
	{
		Map<Long,Long> pollUserVoteMap=null;
		if(authenticatedUserId!=null) {
			List<Vote> votes=voteRepository.findByUserIdAndpollIdIn(authenticatedUserId,pollIds);
			pollUserVoteMap= votes.stream()
					                .collect(Collectors.toMap(vote -> vote.getPoll().getId(), 
					                           vote -> vote.getChoice().getId())
									);
		}
		return pollUserVoteMap;
	}

	private Map<Long, Long> getChoiceVotecountMap(List<Long> pollIds) {
		List<ChoiceVoteCount> voteCountList=voteRepository.countByPollIdInGroupByChoiceId(pollIds);
		return voteCountList.stream()
				.collect(Collectors.
						toMap(ChoiceVoteCount :: getChoiceId, ChoiceVoteCount::getVoteCount)
				);
	}

	private void validatePageNumberAndSize(int page, int size) {
		 if(page < 0) {
	            throw new BadRequestException("Page number cannot be less than zero.");
	        }
	        if(size > AppConstants.MAX_PAGE_SIZE) {
	            throw new BadRequestException("Page size must not be greater than " + AppConstants.MAX_PAGE_SIZE);
	        }
	}

	public Poll createPoll( PollRequest pollRequest) {
		Poll poll=new Poll();
		poll.setQuestion(pollRequest.getQuestion());
		pollRequest.getChoices().forEach(choiceRequest -> {
			var choice=new Choice();
			choice.setText(choiceRequest.getText());
			poll.addChoice(choice);
				}
		);
		Instant now=Instant.now();
		Instant expirationDataTime=now.plus(Duration. ofDays(pollRequest.getPollLength().getDays()))
				                              .plus(Duration.ofHours(pollRequest.getPollLength().getHours())
											  );
		poll.setExpirationDateTime(expirationDataTime);
		return pollRepository.save(poll);
	}

	public PollResponse getPollById(Long pollId, String authenticatedUserId) {
		Poll poll=pollRepository.findById(pollId).orElseThrow(() ->
		new ResourceNotFoundException("Poll", "Id", pollId)
		  );
	   List<ChoiceVoteCount> votes=voteRepository.countByPollIdGroupByChoiceId(pollId);
	   Map<Long,Long> ChoiceVoteCountMap=votes.stream().
			               collect(Collectors.toMap(ChoiceVoteCount::getChoiceId, ChoiceVoteCount::getVoteCount)
						   );
	   User creator=userRepository.findById(poll.getCreatedBy())
			       .orElseThrow(() -> new ResourceNotFoundException("User", "id", poll.getCreatedBy())
				   );
	   Vote userVote=null;
	   if(authenticatedUserId != null) {
		   userVote=voteRepository.findByUserIdAndPollId(authenticatedUserId,pollId);
	   }
	   return ModelMapper.mapPollToPollResponse(poll 
			     , ChoiceVoteCountMap 
			       , creator 
			         , userVote != null ? userVote.getChoice().getId():null);
	}

	public PollResponse castVote(Long pollId, String authenticatedUserId,
								 VoteRequest voteRequest) {
		Poll poll=pollRepository.findById(pollId).orElseThrow(() -> 
		new ResourceNotFoundException("Poll", "Id", pollId)
		 );
		if(poll.getExpirationDateTime().isBefore(Instant.now())) {
			throw new BadRequestException("Sorry! This Poll has already expired");
		}
		User user=userRepository.findById(authenticatedUserId).get();
		Choice selectedChoice=poll.getChoices()
				     .stream()
				      .filter(choice -> choice.getId().equals(voteRequest.getChoiceId()))
				        .findFirst()
			             	.orElseThrow(() -> new ResourceNotFoundException("Choice", "id", voteRequest.getChoiceId())
						);
		Vote vote=new Vote();
		vote.setPoll(poll);
		vote.setChoice(selectedChoice);
		vote.setUser(user);
		try {
			vote = voteRepository.save(vote);
		} catch(DataIntegrityViolationException  ex) {
			logger.info("User {} has already voted in Poll {}",  authenticatedUserId, pollId);
			throw new BadRequestException("Sorry! You have already cast your vote in this poll");
        }
		votesStream.tryEmitNext(
				new VoteResponse(vote.getId(),pollId,new UserSummary(user.getId(),user.getUsername(),user.getName()),selectedChoice.getText())
				).orThrow();

	 List<ChoiceVoteCount> choiceVoteCount= voteRepository.countByPollIdGroupByChoiceId(pollId);
	 Map<Long,Long> ChoiceVoteCountMap=choiceVoteCount.stream()
			                           .collect(Collectors.
										  toMap(ChoiceVoteCount::getChoiceId, ChoiceVoteCount::getVoteCount)
									   );
	  User Creator = userRepository.findById(poll.getCreatedBy())
			          .orElseThrow(() -> new ResourceNotFoundException("User", "id", poll.getCreatedBy())
					  );
	 return ModelMapper.mapPollToPollResponse(poll, ChoiceVoteCountMap, Creator, vote.getChoice().getId());
						
	}

	public PagedResponse<PollResponse> getPollsCreatedBy(String username, String authenticatedUserId, int page,int size) {
          validatePageNumberAndSize(page,size);
          User user = userRepository.findByUsername(username)
	                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username)
					);
		Pageable  pageable=PageRequest.of(page, size, Sort.Direction.DESC,"createdAt");
		Page<Poll> pagedPoll=pollRepository.findByCreatedBy(user.getId(), pageable);
		if(pagedPoll.getNumberOfElements()==0) {
			return new PagedResponse<>(Collections.emptyList(),pagedPoll.getNumber(),pagedPoll.getSize(),pagedPoll.getTotalElements(),
					                                         pagedPoll.getTotalPages(),pagedPoll.isLast());
		}
		List<Long> PollIds=pagedPoll.map(Poll::getId).getContent();
        Map<Long,Long> ChoiceVoteCountMap=getChoiceVotecountMap(PollIds);
        Map<Long,Long> PollUserVoteMap=getPolllUservoteMap(authenticatedUserId,PollIds);
		List<PollResponse> pollResponse=pagedPoll.map(poll -> {
			return ModelMapper.mapPollToPollResponse(poll, 
					ChoiceVoteCountMap, 
					user, 
					PollUserVoteMap == null ? null:PollUserVoteMap.getOrDefault(poll.getId(), null));					
		}).getContent();
		return new PagedResponse<>(pollResponse,pagedPoll.getNumber(),pagedPoll.getSize(),pagedPoll.getTotalElements(),
                pagedPoll.getTotalPages(),pagedPoll.isLast());
	}

	public PagedResponse<PollResponse> getPollsVotedBy(String username, String authenticatedUserId, int page, int size) {
		validatePageNumberAndSize(page,size);
		User user=userRepository.findByUsername(username) 
				.orElseThrow(() -> new ResourceNotFoundException("User", "username", username)
				);
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
		Page<Long> userVotedPollId = voteRepository.findVotedPollIdByUserId(user.getId(),pageable);
		if(userVotedPollId.getNumberOfElements()==0) {
			return new PagedResponse<>(Collections.emptyList(),userVotedPollId.getNumber(),userVotedPollId.getSize(),userVotedPollId.getTotalElements(),
					userVotedPollId.getTotalPages(),userVotedPollId.isLast());
		}
     List<Long> PollIds=userVotedPollId.getContent();
     Sort sort=Sort.by(Sort.Direction.DESC, "createdBy");

     List<Poll> Polls= pollRepository.findByIdIn(PollIds,sort);
        Map<Long,Long> ChoiceVoteCountMap=getChoiceVotecountMap(PollIds);
        Map<Long,Long> PollUserVoteMap=getPolllUservoteMap(authenticatedUserId,PollIds);
        Map<String,User> creatorMap=getPollCreatorMap(Polls);
		List<PollResponse> pollResponse=Polls.stream().map(poll -> {

			return ModelMapper.mapPollToPollResponse(poll,
					ChoiceVoteCountMap, 
					creatorMap.get(poll.getCreatedBy()), 
					PollUserVoteMap == null ? null:PollUserVoteMap.getOrDefault(poll.getId(), null)
			);
		}).collect(Collectors.toList()
		);
		return new PagedResponse<>(pollResponse,userVotedPollId.getNumber(),userVotedPollId.getSize(),userVotedPollId.getTotalElements(),
				userVotedPollId.getTotalPages(),userVotedPollId.isLast());
	}


	public Flux<ServerSentEvent<VoteResponse>> getCastedVotesStream() {
       return votesStream
			   .asFlux()
			    .map(voteResponse->ServerSentEvent.builder(voteResponse).build());
	}
}
