package com.app.usuarios.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "roles")
public class Roles {

	@Id
	private String id;

	private String name;

	public Roles() {
	}

	public Roles(String id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public Roles(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}