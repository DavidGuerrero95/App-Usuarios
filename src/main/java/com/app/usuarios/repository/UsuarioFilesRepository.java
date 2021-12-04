package com.app.usuarios.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

import com.app.usuarios.models.UsuarioFiles;

public interface UsuarioFilesRepository extends MongoRepository<UsuarioFiles, String> {

	@RestResource(path = "buscarUsername")
	public UsuarioFiles findByUsername(@Param("username") String username);

	public UsuarioFiles findImageById(String id, Class<UsuarioFiles> class1);
	
	@RestResource(path = "exist-user")
	public Boolean existsByUsername(@Param("username") String username);

}