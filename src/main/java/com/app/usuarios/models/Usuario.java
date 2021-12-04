package com.app.usuarios.models;

import java.util.List;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "usuarios")
public class Usuario {

	@Id
	private String id;

	@NotBlank(message = "Username cannot be null")
	@Size(max = 20)
	@Indexed(unique = true)
	private String username;

	@NotBlank(message = "Cell phone cannot be null")
	@Size(max = 50)
	@Indexed(unique = true)
	private String cellPhone;

	@NotBlank(message = "Email cannot be null")
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

	public Usuario() {
	}

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

	public String getCedula() {
		return cedula;
	}

	public void setCedula(String cedula) {
		this.cedula = cedula;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(String birthDate) {
		this.birthDate = birthDate;
	}

	public Integer getGender() {
		return gender;
	}

	public void setGender(Integer gender) {
		this.gender = gender;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEconomicActivity() {
		return economicActivity;
	}

	public void setEconomicActivity(String economicActivity) {
		this.economicActivity = economicActivity;
	}

	public List<String> getEconomicData() {
		return economicData;
	}

	public void setEconomicData(List<String> economicData) {
		this.economicData = economicData;
	}

	public List<String> getInterests() {
		return interests;
	}

	public void setInterests(List<String> interests) {
		this.interests = interests;
	}

	public List<Double> getLocation() {
		return location;
	}

	public void setLocation(List<Double> location) {
		this.location = location;
	}

	public Boolean getHeadFamily() {
		return headFamily;
	}

	public void setHeadFamily(Boolean headFamily) {
		this.headFamily = headFamily;
	}

	public String getStakeHolders() {
		return stakeHolders;
	}

	public void setStakeHolders(String stakeHolders) {
		this.stakeHolders = stakeHolders;
	}

}
