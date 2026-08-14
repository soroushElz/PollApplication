package com.example.polls.Payload;

public record VoteResponse(Long voteId,Long PollId,UserSummary voter,String choice) {
}
