package com.app.usuarios.models;

import java.util.Date;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

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
	private String contentType; // file type
	private long size; // file size
	private String suffix;

	public UsuarioFiles() {
	}

	public UsuarioFiles(@Size(max = 20) String username, String name, Date createdtime, Binary content,
			String contentType, long size, String suffix) {
		super();
		this.username = username;
		this.name = name;
		this.createdtime = createdtime;
		this.content = content;
		this.contentType = contentType;
		this.size = size;
		this.suffix = suffix;
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

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

}
