package com.app.usuarios.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

import com.app.usuarios.models.Roles;

public interface RoleRepository extends MongoRepository<Roles, String> {

	@RestResource(path = "role")
	public Optional<Roles> findByName(@Param("role") String name);
}
