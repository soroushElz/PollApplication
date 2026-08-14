package com.example.polls.Payload;

public class UserSummary {
	
	private String Id;
	private String Username;
	private  String name;

	
	public UserSummary(String id, String username, String name) {
		Id = id;
		Username = username;
		this.name = name;
	}

	public String getId() {
		return Id;
	}

	public void setId(String id) {
		Id = id;
	}

	public String getUsername() {
		return Username;
	}

	public void setUsername(String username) {
		Username = username;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
	

}
