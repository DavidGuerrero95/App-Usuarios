package com.app.usuarios.services;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.app.usuarios.models.Roles;
import com.app.usuarios.models.Usuario;
import com.app.usuarios.models.UsuarioFiles;
import com.app.usuarios.models.UsuarioPw;

public interface IUsuarioService {
	
	public Usuario editUser(Usuario u, Usuario e);

	public String codificar(String password);

	public UsuarioFiles crearUf(String username);

	public List<Roles> obtenerRoles(List<String> roles);

	public UsuarioPw usuarioPasword(String username, String password);

	public UsuarioFiles ponerImagen(String username, MultipartFile file);
}
