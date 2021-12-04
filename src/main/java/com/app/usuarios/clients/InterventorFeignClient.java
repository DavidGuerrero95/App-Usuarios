package com.app.usuarios.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "app-interventor")
public interface InterventorFeignClient {

	@PostMapping("/interventor/usuariosEliminar")
	public Boolean peticionEliminarUsuarios(@RequestParam("username") String username);

}
