package com.example.polls.Payload;

import java.util.List;

public class PagedResponse<T> {
	
	private List<T> Content;
	private int page;
	private int size;
	private Long totalElements;
	private int totalPages;
	private Boolean last;
	
	public List<T> getContent() {
		return Content;
	}

	public void setContent(List<T> content) {
		Content = content;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public Long getTotalElements() {
		return totalElements;
	}

	public void setTotalElements(Long totalElements) {
		this.totalElements = totalElements;
	}

	public int getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}

	public Boolean getLast() {
		return last;
	}

	public void setLast(Boolean last) {
		this.last = last;
	}

	public PagedResponse() {
		
	}

	public PagedResponse(List<T> content, int page, int size, Long totalElements, int totalPages, Boolean last) {

		Content = content;
		this.page = page;
		this.size = size;
		this.totalElements = totalElements;
		this.totalPages = totalPages;
		this.last = last;
	}


	
	
	

}
