package com.example.polls.models;

import java.time.Instant;

import com.example.polls.models.audit.userDateAudit;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name="Polls")
public class Poll extends userDateAudit {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long Id;
	
	@NotNull
	private Instant expirationDateTime;
	
	@NotBlank
	@Size(max=140)
	private String question;
	
	@OneToMany(
            mappedBy = "poll",
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER,
            orphanRemoval = true
    )
    @Size(min = 2, max = 6)
    @Fetch(FetchMode.SELECT)
    @BatchSize(size = 30)
	private List<Choice> choices=new ArrayList<>();
	
	public void addChoice(Choice choice) {
		choices.add(choice);
		choice.setPoll(this);
	}
	
	public void removeChoice(Choice choice) {
		choices.remove(choice);
		choice.setPoll(null);
	}

	public Long getId() {
		return Id;
	}

	public void setId(Long id) {
		Id = id;
	}

	public Instant getExpirationDateTime() {
		return expirationDateTime;
	}

	public void setExpirationDateTime(Instant expirationDateTime) {
		this.expirationDateTime = expirationDateTime;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public List<Choice> getChoices() {
		return choices;
	}

	public void setChoices(List<Choice> choices) {
		this.choices = choices;
	}
	
	
}

