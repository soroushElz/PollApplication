package com.example.polls.Payload;

import java.time.Instant;

public class UserProfile {
	
	private String Id;
	private String username;
	private String name;
	private Instant JoinedAt;
	private Long pollCount;
	private Long voteCount;
	
	public UserProfile(String id, String username, String name, Instant joinedAt, Long pollCount, Long voteCount) {
		Id = id;
		this.username = username;
		this.name = name;
		JoinedAt = joinedAt;
		this.pollCount = pollCount;
		this.voteCount = voteCount;
	}

	public String getId() {
		return Id;
	}

	public void setId(String id) {
		Id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Instant getJoinedAt() {
		return JoinedAt;
	}

	public void setJoinedAt(Instant joinedAt) {
		JoinedAt = joinedAt;
	}

	public Long getPollCount() {
		return pollCount;
	}

	public void setPollCount(Long pollCount) {
		this.pollCount = pollCount;
	}

	public Long getVoteCount() {
		return voteCount;
	}

	public void setVoteCount(Long voteCount) {
		this.voteCount = voteCount;
	}
	
	
	

}
