package com.app.usuarios.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

import com.app.usuarios.models.Usuario;

public interface UsuarioRepository extends MongoRepository<Usuario, String> {

	@RestResource(path = "find-CellPhone")
	public Usuario findByCellPhone(@Param("cellPhone") String cellPhone);

	@RestResource(path = "find-user")
	public Usuario findByUsername(@Param("username") String username);

	@RestResource(path = "find-user-email-cellPhone")
	public Usuario findByUsernameOrEmailOrCellPhone(@Param("username") String username, @Param("username") String email,
			@Param("username") String cellPhone);

	@RestResource(path = "exist-user")
	public Boolean existsByUsername(@Param("username") String username);

	@RestResource(path = "exist-email")
	public Boolean existsByEmail(@Param("email") String email);

	@RestResource(path = "exist-phone")
	public Boolean existsByCellPhone(@Param("phone") String phone);

	@RestResource(path = "exist-cedula")
	public Boolean existsByCedula(@Param("cedula") String cedula);

	@RestResource(path = "existe-user-email-cellPhone")
	public Boolean existsByUsernameOrEmailOrCellPhone(@Param("username") String username,
			@Param("username") String email, @Param("username") String cellPhone);

}