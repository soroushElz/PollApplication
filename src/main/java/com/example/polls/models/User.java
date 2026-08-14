package com.example.polls.models;

import com.example.polls.models.audit.DateAudit;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.NaturalId;
import org.hibernate.validator.constraints.UUID;

@Entity
@Table(name="users",uniqueConstraints= {
		@UniqueConstraint(columnNames= {
				"username"
		}),
		@UniqueConstraint(columnNames= {
				"email"
		})
})
public class User extends DateAudit {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@UUID
	private String Id;
	
	@NotBlank
	@Size(max=40)
	private String name;
	
	@NotBlank
	@Size(max=15)
	private String username;
	
	@NaturalId
	@NotBlank
	@Size(max=40)
	@Email
	private String email;
	
	public User(String id,String name, String username,String email,String password) {

		this.name = name;
		this.username = username;
		this.email = email;
		this.password = password;
		this.Id=id;

	}
	
	public User() {}

	public String getId() {
		return Id;
	}

	public void setId(String id) {
		Id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	@NotBlank
	@Size(max=100)
	private String password;
	
	@ManyToMany(fetch=FetchType.LAZY)
	@JoinTable(name="user_roles",
	           joinColumns=@JoinColumn(name="user_id"),
	           inverseJoinColumns=@JoinColumn(name="role_id"))
	private Set<Role> roles=new HashSet<>();
	

}
