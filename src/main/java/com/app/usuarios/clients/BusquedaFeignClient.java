package com.app.usuarios.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "app-busqueda")
public interface BusquedaFeignClient {

	@DeleteMapping("/busqueda/username/eliminar/{username}")
	public Boolean eliminarBusquedasUsername(@PathVariable("username") String username);

}
