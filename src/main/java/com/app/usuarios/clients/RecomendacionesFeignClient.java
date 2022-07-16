package com.app.usuarios.clients;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "app-recomendacion")
public interface RecomendacionesFeignClient {

	@PostMapping("/recomendaciones/crear/")
	public Boolean crearRecomendacion(@RequestParam("username") String username,
			@RequestParam("listInterests") List<String> listInterests,
			@RequestParam("listLocation") List<Double> listLocation);

	@DeleteMapping("/recomendaciones/eliminar/{username}")
	public Boolean eliminarRecomendacion(@PathVariable("username") String username);

	@PutMapping("/recomendaciones/usuario/editar/ubicacion/{username}")
	public Boolean editarUbicacion(@PathVariable("username") String username,
			@RequestParam("listaLocation") List<Double> listaLocation);

	@PutMapping("/recomendaciones/editar/{username}")
	public Boolean editUser(@PathVariable("username") String username, @RequestParam("newUsername") String newUsername,
			@RequestParam("listaInterests") List<String> listaInterests);

	@DeleteMapping("/recomendaciones/eliminar/all/usuarios/")
	public Boolean eliminarAllUsuario();
}
