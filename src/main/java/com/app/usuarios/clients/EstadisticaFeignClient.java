package com.app.usuarios.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "app-estadistica")
public interface EstadisticaFeignClient {
	
	@PostMapping("/estadisticas/usuarios/crear/")
	public Boolean crearUsuarioNotificaciones(@RequestParam("username") String username);
	
	@PutMapping("/estadisticas/usuarios/editar/{username}")
	public Boolean editUser(@PathVariable("username") String username, @RequestParam("newUsername") String newUsername);

	@DeleteMapping("/estadisticas/usuarios/borrarEstadisticas/")
	public Boolean borrarEstadisticasUsuario(@RequestParam("username") String username);
	
	@DeleteMapping("/estadisticas/eliminar/all/usuarios/")
	public Boolean eliminarAllUsuario();
	
}
