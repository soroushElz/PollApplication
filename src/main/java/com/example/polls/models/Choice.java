package com.example.polls.models;

import java.util.Objects;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name="Choices")
public class Choice {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 40)
    private String text;

    @ManyToOne(fetch=FetchType.LAZY,optional=false)
    @JoinColumn(name="poll_Id",nullable=false)
    private Poll poll;
    
    public Choice() {
    	
    }
    
    public Choice(String Text) {
    	this.text=Text;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Poll getPoll() {
		return poll;
	}

	public void setPoll(Poll poll) {
		this.poll = poll;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Choice other = (Choice) obj;
		return Objects.equals(id, other.id);
	}
    
    
    
}
