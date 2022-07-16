package com.app.usuarios.models;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "usuariosPw")
@Data
@NoArgsConstructor
public class UsuarioPw {

	@Id
	@JsonIgnore
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

}
