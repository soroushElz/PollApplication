package com.example.polls.Payload;

public class ApiResponse {
	
	private Boolean success;
	private String message;

	public ApiResponse(Boolean Success, String Message) {
		this.message=Message;
		this.success=Success;
	}

	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	
}
