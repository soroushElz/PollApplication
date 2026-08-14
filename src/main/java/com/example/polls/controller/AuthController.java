package com.example.polls.controller;

import java.net.URI;
import java.util.Collections;

import com.example.polls.security.KeycloackAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.polls.Exception.AppException;
import com.example.polls.Payload.ApiResponse;
import com.example.polls.Payload.SignupRequest;
import com.example.polls.models.*;
import com.example.polls.repository.RoleRepository;
import com.example.polls.repository.UserRepository;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	  @Autowired
	  UserRepository userRepository;

	  @Autowired
	  RoleRepository roleRepository;

	  @Autowired
	KeycloackAdminService keycloackAdminService;
	


	@PostMapping("/register")
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupReq ){
		
		if(userRepository.existsByUsername(signupReq.getUsername())) {
			 return new ResponseEntity<>(new ApiResponse(false, "Username is already taken!"),
	                    HttpStatus.BAD_REQUEST);
		}
		if(userRepository.existsByEmail(signupReq.getEmail())) {
			 return new ResponseEntity<>(new ApiResponse(false, "Email is already taken!"),
	                    HttpStatus.BAD_REQUEST);	
	}


		String createdUserId=keycloackAdminService.createUser(signupReq.getUsername(),signupReq.getEmail(),signupReq.getPassword());

		keycloackAdminService.assignRole(createdUserId,"user");

		User user = new User(createdUserId,signupReq.getName(), signupReq.getUsername(),
				signupReq.getEmail(), signupReq.getPassword());
        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new AppException("User Role not set."));

        user.setRoles(Collections.singleton(userRole));

        User result = userRepository.save(user);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/api/users/{username}")
                .buildAndExpand(result.getUsername()).toUri();

        return ResponseEntity.created(location).body(new ApiResponse(true, "User registered successfully"));
    }
	
	

}
