package com.example.polls.models;

import org.hibernate.annotations.NaturalId;

import com.example.polls.models.RoleName;

import jakarta.persistence.*;

@Entity
@Table(name="roles")
public class Role {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	public Role(RoleName name) {
		this.name = name;
	}
	public Role() {

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public RoleName getName() {
		return name;
	}

	public void setName(RoleName name) {
		this.name = name;
	}

	@Enumerated(EnumType.STRING)
	@NaturalId
	@Column(length=60)
	private RoleName name;
}


