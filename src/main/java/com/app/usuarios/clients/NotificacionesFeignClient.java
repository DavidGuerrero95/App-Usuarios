package com.app.usuarios.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "app-notificaciones")
public interface NotificacionesFeignClient {

	@PostMapping("/notificaciones/crear/")
	public Boolean crearNotificaciones(@RequestParam("username") String username, @RequestParam("email") String email);

	@DeleteMapping("/notificaciones/eliminar/")
	public Boolean eliminarNotificacion(@RequestParam String nombre);

	@GetMapping("/notificaciones/usuario/enviar/{username}")
	public void enviarCodigoEditUsuario(@PathVariable("username") String username,
			@RequestParam(value = "codigo") Integer codigo);

	@PutMapping("/notificaciones/usuario/editar/{username}")
	public Boolean editUser(@PathVariable("username") String username, @RequestParam("newUsername") String newUsername,
			@RequestParam("email") String email);
	
	@DeleteMapping("/notificacioneso/eliminar/all/usuarios/")
	public Boolean eliminarAllUsuario();	
	
}
