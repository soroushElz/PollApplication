package com.example.polls.controller;

import com.example.polls.Payload.*;
import com.example.polls.models.Vote;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.polls.Service.PollService;
import com.example.polls.repository.PollRepository;
import com.example.polls.repository.UserRepository;
import com.example.polls.repository.VoteRepository;
import com.example.polls.security.CurrentUser;
import com.example.polls.security.UserPrincipal;
import com.example.polls.util.AppConstants;
import com.example.polls.models.Poll;

import jakarta.validation.Valid;

import java.net.URI;

import org.slf4j.Logger;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/polls")
public class pollController {
	
	private static final Logger logger=LoggerFactory.getLogger(pollController.class);
	
	@Autowired
	private PollService pollService;

	@PreAuthorize("hasRole('USER')")
	@GetMapping
	public PagedResponse<PollResponse> getPolls(@AuthenticationPrincipal Jwt jwt,
			                                       @RequestParam(value="page",defaultValue=AppConstants.DEFAULT_PAGE_NUMBER) int page,
			                                       @RequestParam(value="size",defaultValue=AppConstants.DEFAULT_PAGE_SIZE) int size){

        return pollService.getAllPolls(jwt.getSubject(), page, size);
	}
	
	@PostMapping("/new")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<?> createPoll(@Valid @RequestBody PollRequest pollRequest ){
	      
		Poll poll=pollService.createPoll(pollRequest);
		 URI location=ServletUriComponentsBuilder
                        .fromCurrentRequest().path("/{pollId}")
                        .buildAndExpand(poll.getId()).toUri();
		
		return ResponseEntity.created(location)
				             .body(new ApiResponse(true,"Polll Created Successfully"));
		
	}
	
	@GetMapping("/{pollId}")
	public PollResponse getPollById(@AuthenticationPrincipal Jwt jwt,
			                                     @PathVariable Long pollId) {
        return pollService.getPollById(pollId,jwt.getSubject());

	}
	
	@PostMapping("/{pollId}/votes")
	@PreAuthorize("hasRole('USER')")
	public PollResponse castVote(@AuthenticationPrincipal Jwt jwt ,
			                       @PathVariable Long pollId ,
			                        @Valid @RequestBody VoteRequest voteRequest) {
		return pollService.castVote(pollId,jwt.getSubject(),voteRequest);
	}

	@GetMapping(value = "/votes/stream",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<ServerSentEvent<VoteResponse>> subscribeToVote(@AuthenticationPrincipal Jwt jwt){
		return pollService.getCastedVotesStream();
	}

}
