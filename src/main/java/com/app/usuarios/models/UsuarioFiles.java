package com.app.usuarios.models;

import java.util.Date;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

@Document(collection = "usuariosFiles")
public class UsuarioFiles {

	@Id
	private String id;

	@NotBlank(message = "Username cannot be null")
	@Size(max = 20)
	@Indexed(unique = true)
	private String username;
	
	private String name; // file name
	private Date createdtime; // upload time
	private Binary content; // file content
	private String contenttype; // file type
	private long size; // file size

	public UsuarioFiles() {
	}

	public UsuarioFiles(String username, String name, Date createdtime, Binary content, String contenttype, long size) {
		super();
		this.username = username;
		this.name = name;
		this.createdtime = createdtime;
		this.content = content;
		this.contenttype = contenttype;
		this.size = size;
	}

	public String getId() {
		return id;
	}

	@JsonProperty(access = Access.WRITE_ONLY)
	public void setId(String id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getCreatedtime() {
		return createdtime;
	}

	public void setCreatedtime(Date createdtime) {
		this.createdtime = createdtime;
	}

	public Binary getContent() {
		return content;
	}

	public void setContent(Binary content) {
		this.content = content;
	}

	public String getContenttype() {
		return contenttype;
	}

	public void setContenttype(String contenttype) {
		this.contenttype = contenttype;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

}
