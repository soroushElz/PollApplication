package com.example.polls.Payload;

public class ChoiceResponse {

	private Long Id;
	private String Text;
	private Long VoteCount;
	
	public Long getId() {
		return Id;
	}
	public void setId(Long id) {
		Id = id;
	}
	public String getText() {
		return Text;
	}
	public void setText(String text) {
		Text = text;
	}
	public Long getVoteCount() {
		return VoteCount;
	}
	public void setVoteCount(Long voteCount) {
		VoteCount = voteCount;
	}
	
	
}
