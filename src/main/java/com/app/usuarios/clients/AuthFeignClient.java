package com.app.usuarios.clients;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.app.usuarios.models.Roles;
import com.app.usuarios.models.UsuarioPw;

@FeignClient(name = "app-autenticacion")
public interface AuthFeignClient {

	@PostMapping("/autenticacion/crear")
	public Boolean crearUsuario(@RequestBody UsuarioPw usuarioPw);

	@DeleteMapping("/autenticacion/eliminar")
	public Boolean eliminarUsuario(@RequestBody UsuarioPw usuarioPw);

	@PutMapping("/autenticacion/editar")
	public Boolean editarUsuarioAuth(@RequestParam String username, @RequestParam String uEdit,
			@RequestParam String newPassword, @RequestParam List<Roles> roles);

}
