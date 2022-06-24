package com.app.usuarios.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.Binary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.app.usuarios.clients.RecomendacionesFeignClient;
import com.app.usuarios.models.Roles;
import com.app.usuarios.models.Usuario;
import com.app.usuarios.models.UsuarioFiles;
import com.app.usuarios.models.UsuarioPw;
import com.app.usuarios.repository.UsuarioFilesRepository;

@Service
public class UsuarioService implements IUsuarioService {

	private final Logger logger = LoggerFactory.getLogger(UsuarioService.class);

	@SuppressWarnings("rawtypes")
	@Autowired
	private CircuitBreakerFactory cbFactory;

	@Autowired
	PasswordEncoder encoder;

	@Autowired
	UsuarioFilesRepository ufRepository;

	@Autowired
	RecomendacionesFeignClient rmdClient;

	@Override
	public Usuario editUser(Usuario uDb, Usuario usuario) {
		if(usuario.getCedula() != null) uDb.setCedula(usuario.getCedula());
		if (usuario.getName() != null) uDb.setName(usuario.getName());
		if (usuario.getLastName() != null) uDb.setLastName(usuario.getLastName());
		if (usuario.getPhone() != null)	uDb.setPhone(usuario.getPhone());
		if (usuario.getEconomicActivity() != null)	uDb.setEconomicActivity(usuario.getEconomicActivity());
		if (usuario.getEconomicData() != null) uDb.setEconomicData(usuario.getEconomicData());
		if (usuario.getInterests() != null) {
			uDb.setInterests(usuario.getInterests());
			if (cbFactory.create("usuario").run(
					() -> rmdClient.editUser(usuario.getUsername(), "", usuario.getInterests()),
					e -> errorConexion(e))) {
				logger.info("Edicion Notificaciones Correcta");
			}
		}
		if (usuario.getStakeHolders() != null) uDb.setStakeHolders(usuario.getStakeHolders());
		return uDb;
	}

	@Override
	public UsuarioFiles crearUf(String username) {
		UsuarioFiles f = ufRepository.findByUsername("admin");
		UsuarioFiles uf = new UsuarioFiles();
		uf.setUsername(username);
		uf.setName(f.getName());
		uf.setCreatedtime(new Date());
		uf.setContent(f.getContent());
		uf.setContentType(f.getContentType());
		uf.setSize(f.getSize());
		uf.setSuffix("");
		return uf;
	}

	@Override
	public String codificar(String password) {
		return encoder.encode(password);
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Override
	public List<Roles> obtenerRoles(List<String> roles) {
		List<Roles> rLista = new ArrayList<Roles>();
		roles.forEach(r -> {
			Roles role = new Roles();
			switch (r) {
			case "admin":
				role = new Roles("1", "ROLE_ADMIN");
				rLista.add(role);
				break;
			case "mod":
				role = new Roles("2", "ROLE_MODERATOR");
				rLista.add(role);
				break;
			default:
				role = new Roles("4", "ROLE_USER");
				rLista.add(role);
				break;
			}
		});
		return rLista;
	}

	@Override
	public UsuarioPw usuarioPasword(String username, String password) {
		Roles admin = new Roles("1", "ROLE_ADMIN");
		Roles mod = new Roles("2", "ROLE_MODERATOR");
		Roles intrvnt = new Roles("3", "ROLE_INTERVENTOR");
		Roles user = new Roles("1", "ROLE_USER");
		List<Roles> roles = new ArrayList<Roles>();
		roles.add(admin);
		roles.add(mod);
		roles.add(intrvnt);
		roles.add(user);
		UsuarioPw uPw = new UsuarioPw(username, passwordEncoder().encode(password), true, 0, 0, roles);
		return uPw;
	}

	@Override
	public UsuarioFiles ponerImagen(String username, MultipartFile file) {
		UsuarioFiles uf = new UsuarioFiles();
		if (ufRepository.existsByUsername(username)) {
			uf = ufRepository.findByUsername(username);
		} else {
			uf.setUsername(username);
		}
		try {
			String fileName = file.getOriginalFilename();
			uf.setName(fileName);
			uf.setCreatedtime(new Date());
			uf.setContent(new Binary(file.getBytes()));
			uf.setContentType(file.getContentType());
			uf.setSize(file.getSize());
			String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
			uf.setSuffix(suffix);
			return uf;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

	}

	public Boolean errorConexion(Throwable e) {
		logger.info(e.getMessage());
		return false;
	}

}
