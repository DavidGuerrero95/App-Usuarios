package com.app.usuarios.models;

import java.util.List;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "registro")
public class Registro {

	@Id
	private String id;

	@NotBlank(message = "Username cannot be null")
	@Size(max = 20)
	@Indexed(unique = true)
	@Pattern(regexp = "[A-Za-z0-9_.-]+", message = "Solo se permite:'_' o '.' o '-'")
	private String username;

	@NotBlank(message = "Password cannot be null")
	@Pattern(regexp = "[^ ]*+", message = "Caracter: ' ' (Espacio en blanco) invalido")
	@Size(min = 6, max = 20, message = "About Me must be between 6 and 20 characters")
	private String password;

	@NotBlank(message = "Cell phone cannot be null")
	@Pattern(regexp = "[0-9]+", message = "Solo numeros")
	@Size(max = 50)
	@Indexed(unique = true)
	private String cellPhone;

	@NotBlank(message = "Email cannot be null")
	@Size(max = 50)
	@Pattern(regexp = "[^ ]*+", message = "Caracter: ' ' (Espacio en blanco) invalido")
	@Email(message = "Email should be valid")
	@Indexed(unique = true)
	private String email;

	private String codigo;
	private Long minutos;
	private List<String> roles;

	public Registro() {
	}

	public Registro(String username, String password, String cellPhone, String email, String codigo, Long minutos,
			List<String> roles) {
		super();
		this.username = username;
		this.password = password;
		this.cellPhone = cellPhone;
		this.email = email;
		this.codigo = codigo;
		this.minutos = minutos;
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

	public String getCellPhone() {
		return cellPhone;
	}

	public void setCellPhone(String cellPhone) {
		this.cellPhone = cellPhone;
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

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public Long getMinutos() {
		return minutos;
	}

	public void setMinutos(Long minutos) {
		this.minutos = minutos;
	}

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}
}
