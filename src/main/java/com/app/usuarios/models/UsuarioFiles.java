package com.app.usuarios.models;

import java.util.Date;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "usuariosFiles")
@Data
@NoArgsConstructor
public class UsuarioFiles {

	@Id
	@JsonIgnore
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

}
