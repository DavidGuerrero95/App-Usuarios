package com.app.usuarios.models;

import java.util.List;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "usuarios")
@Data
@NoArgsConstructor
public class Usuario {

	@Id
	@JsonIgnore
	private String id;

	@NotNull(message = "Username cannot be null")
	@Size(max = 20)
	@Indexed(unique = true)
	private String username;

	@NotNull(message = "Cell phone cannot be null")
	@Size(max = 50)
	@Indexed(unique = true)
	private String cellPhone;

	@NotNull(message = "Email cannot be null")
	@Size(max = 50)
	@Email(message = "Email should be valid")
	@Indexed(unique = true)
	private String email;

	@Size(max = 15)
	@Indexed(unique = true)
	private String cedula;

	private String name;
	private String lastName;

	@Size(min = 8, max = 12)
	private String birthDate;

	@Size(max = 2)
	private Integer gender;

	private String phone;
	private String economicActivity;
	private List<String> economicData;
	private List<String> interests;
	private List<Double> location;
	private Boolean headFamily;
	private String stakeHolders;

	public Usuario(String username, String cellPhone, String email, String cedula, String name, String lastName,
			String birthDate, Integer gender, String phone, String economicActivity, List<String> economicData,
			List<String> interests, List<Double> location, Boolean headFamily, String stakeHolders) {
		super();
		this.username = username;
		this.cellPhone = cellPhone;
		this.email = email;
		this.cedula = cedula;
		this.name = name;
		this.lastName = lastName;
		this.birthDate = birthDate;
		this.gender = gender;
		this.phone = phone;
		this.economicActivity = economicActivity;
		this.economicData = economicData;
		this.interests = interests;
		this.location = location;
		this.headFamily = headFamily;
		this.stakeHolders = stakeHolders;
	}

}
