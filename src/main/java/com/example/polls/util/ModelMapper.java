package com.example.polls.util;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.polls.Payload.ChoiceResponse;
import com.example.polls.Payload.PollResponse;
import com.example.polls.Payload.UserSummary;
import com.example.polls.models.Poll;
import com.example.polls.models.User;

public class ModelMapper {


	
	public static PollResponse mapPollToPollResponse(Poll poll,Map<Long,Long> ChoiceVoteMap,User creator,Long UserVote) {
	 
		PollResponse pollResponse=new PollResponse();
		pollResponse.setId(poll.getId());
		pollResponse.setQuestion(poll.getQuestion());
		pollResponse.setCreatedBy(new UserSummary(
				                     creator.getId(),creator.getUsername(),creator.getName()));
		
        pollResponse.setCreationDateTime(poll.getCreatedAt());
        pollResponse.setExpirationDataTime(poll.getExpirationDateTime());
        
        Instant now=Instant.now();
        pollResponse.setIsExpired(poll.getExpirationDateTime().isBefore(now));
        
        List<ChoiceResponse> choices=poll.getChoices().stream().map(choice -> {
        	ChoiceResponse choiceResponse=new ChoiceResponse();
        	choiceResponse.setId(choice.getId());
        	choiceResponse.setText(choice.getText());

            choiceResponse.setVoteCount(ChoiceVoteMap.getOrDefault(choice.getId(), 0l));
        		
        	return choiceResponse;
        	
        }).collect(Collectors.toList());
        
        pollResponse.setChoices(choices);
        
        if(UserVote != null) {
        	pollResponse.setSelectedChoice(UserVote);        	
        }
		
		Long TotalVotes= pollResponse.getChoices()
                         .stream().mapToLong(choiceResponse-> choiceResponse.getVoteCount())
                           .sum();

		pollResponse.setTotalVotes(TotalVotes);
		return pollResponse;
		
	}
}
