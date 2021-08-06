package com.blueoptima.apirate.Models;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;

public class ApiError {
	
	private HttpStatus status;
	private int statusCode;
	private String message;
	private List<String> error;
	public HttpStatus getStatus() {
		return status;
	}

	public void setStatus(HttpStatus status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<String> getError() {
		return error;
	}

	public void setError(List<String> error) {
		this.error = error;
	}

	public ApiError(HttpStatus status,int statusCode, String message, List<String> error) {
		this.status = status;
		this.statusCode=statusCode;
		this.message = message;
		this.error = error;
	}
	
	public ApiError(HttpStatus status, int statusCode, String message, String error) {
		this.status = status;
		this.statusCode=statusCode;
		this.message = message;
		this.error = Arrays.asList(error);
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
	
}
