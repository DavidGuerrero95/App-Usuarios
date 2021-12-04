package com.app.usuarios.models;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "usuariosPw")
public class UsuarioPw {

	@Id
	private String id;

	@NotBlank(message = "Username cannot be null")
	@Size(max = 20)
	@Indexed(unique = true)
	private String username;

	@NotBlank(message = "Password cannot be null")
	@Size(min = 6, max = 20, message = "About Me must be between 6 and 20 characters")
	private String password;

	private Boolean enabled;
	private Integer attempts;
	private Integer code;
	private List<Roles> roles;

	public UsuarioPw() {
	}

	public UsuarioPw(String username, String password, Boolean enabled, Integer attempts, Integer code,
			List<Roles> roles) {
		super();
		this.username = username;
		this.password = password;
		this.enabled = enabled;
		this.attempts = attempts;
		this.code = code;
		this.roles = roles;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public Integer getAttempts() {
		return attempts;
	}

	public void setAttempts(Integer attempts) {
		this.attempts = attempts;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public List<Roles> getRoles() {
		return roles;
	}

	public void setRoles(List<Roles> roles) {
		this.roles = roles;
	}

}
