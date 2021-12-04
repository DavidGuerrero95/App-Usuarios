package com.app.usuarios.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

import com.app.usuarios.models.UsuarioPw;

public interface UsuarioPwRepository extends MongoRepository<UsuarioPw, String> {

	@RestResource(path = "find-user")
	public UsuarioPw findByUsername(@Param("username") String username);

}
