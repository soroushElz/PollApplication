package com.example.polls.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.polls.Exception.ResourceNotFoundException;
import com.example.polls.Payload.PagedResponse;
import com.example.polls.Payload.PollResponse;
import com.example.polls.Payload.UserProfile;
import com.example.polls.Service.PollService;
import com.example.polls.models.User;
import com.example.polls.repository.PollRepository;
import com.example.polls.repository.UserRepository;
import com.example.polls.repository.VoteRepository;
import com.example.polls.util.AppConstants;

@RestController
@RequestMapping("/api")
public class UserController {
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	VoteRepository voteRepository;
	
	@Autowired
	PollRepository pollRepository;
	
	@Autowired
	PollService pollService;
	
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);


    @GetMapping("/users/{username}")
    public UserProfile getUserProfile (@PathVariable(value="username") String username) {
    	User user=userRepository.findByUsername(username)
    			.orElseThrow(() ->new  ResourceNotFoundException("User","username",username)
                );
    long pollCount=pollRepository.countByCreatedBy(user.getId());
    long VoteCount=voteRepository.countByUserId(user.getId());
    return new UserProfile(user.getId() ,
    		                user.getUsername() ,
    		                 user.getName() ,
    		                   user.getCreatedAt() ,
    		                    pollCount ,
    		                     VoteCount );
    
    }


    @GetMapping("/users/{username}/polls")
    public PagedResponse<PollResponse> getPollsCreatedBy(@PathVariable(value="username") String username ,
                                                          @AuthenticationPrincipal Jwt jwt,
                                                           @RequestParam(value="page",defaultValue=AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                              @RequestParam(value="size",defaultValue=AppConstants.DEFAULT_PAGE_SIZE) int size)
    {
                return pollService.getPollsCreatedBy(username,jwt.getSubject(),page,size);
      }
                                                              
      @GetMapping("/users/{username}/votes")
     public PagedResponse<PollResponse> getPollsVotedBy(@PathVariable(value="username") String username ,
                                                            @AuthenticationPrincipal Jwt jwt,
                                                                 @RequestParam(value="page",defaultValue=AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                                     @RequestParam(value="size",defaultValue=AppConstants.DEFAULT_PAGE_SIZE) int size)
      {
      return pollService.getPollsVotedBy(username,jwt.getSubject(),page,size);
         }                                                              
    
    
}
