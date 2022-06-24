package com.app.usuarios.clients;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.app.usuarios.models.Registro;
import com.app.usuarios.models.Roles;
import com.app.usuarios.models.UsuarioPw;

@FeignClient(name = "app-registro")
public interface RegistroFeignClient {

	@DeleteMapping("/registro/eliminar/")
	@ResponseStatus(code = HttpStatus.ACCEPTED)
	public Boolean eliminarUsuario(@RequestBody UsuarioPw usuario);

	@PutMapping("/registro/editar/")
	public Boolean editarUsuarioRegistro(@RequestParam String username, @RequestParam String uEdit,
			@RequestParam String cEdit, @RequestParam String eEdit, @RequestParam String newPassword,
			@RequestParam List<Roles> rolesEdicion);

	@GetMapping("/registro/listar/")
	public List<Registro> listar();

	@PostMapping("/registro/registro/confirmar/{username}")
	public String crearUsuario(@PathVariable("username") String username, @RequestParam("codigo") String codigo);

	@PutMapping("/registro/editar/codigo/")
	public void editarUsuario(@RequestParam String username, @RequestParam String codigo, @RequestParam Long minutos);
	
	@DeleteMapping("/registro/eliminar/all/usuarios/")
	public Boolean eliminarAllUsuario();

}
