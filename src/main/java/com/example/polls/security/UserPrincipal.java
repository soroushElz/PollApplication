package com.example.polls.security;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.polls.models.User;
import com.fasterxml.jackson.annotation.JsonIgnore;


public class UserPrincipal implements UserDetails {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String id;

    private String name;

    private String Username;
    
    private Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(String id, String name, String username, Collection<? extends GrantedAuthority> authorities,
			String email, String password) {
		this.id = id;
		this.name = name;
		this.Username = username;
		this.authorities = authorities;
		this.email = email;
		this.password = password;
	}

	public static UserPrincipal create(User user) {
    	List<GrantedAuthority> authorities=user.getRoles().stream().map(role -> 
    	               new SimpleGrantedAuthority(role.getName().name())
    			      ).collect(Collectors.toList());
        
	      return new UserPrincipal(user.getId(),
	    		                    user.getName(),
	    		                     user.getUsername(),
	    		                     authorities,
	    		                      user.getEmail(),
	    		                       user.getPassword());
	    		  }
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}
	

	 private String email;

	  @JsonIgnore
	 private String password;
	  

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return Username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	public String getId() {
		return id;
	}


	public String getName() {
		return name;
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
		UserPrincipal other = (UserPrincipal) obj;
		return Objects.equals(id, other.id);
	}

	

}
