package com.app.usuarios.controllers;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.app.usuarios.clients.AuthFeignClient;
import com.app.usuarios.clients.EstadisticaFeignClient;
import com.app.usuarios.clients.InterventorFeignClient;
import com.app.usuarios.clients.NotificacionesFeignClient;
import com.app.usuarios.clients.RecomendacionesFeignClient;
import com.app.usuarios.clients.RegistroFeignClient;
import com.app.usuarios.models.Registro;
import com.app.usuarios.models.Roles;
import com.app.usuarios.models.Usuario;
import com.app.usuarios.models.UsuarioFiles;
import com.app.usuarios.models.UsuarioPw;
import com.app.usuarios.repository.UsuarioFilesRepository;
import com.app.usuarios.repository.UsuarioPwRepository;
import com.app.usuarios.repository.UsuarioRepository;
import com.app.usuarios.services.IUsuarioService;

@RestController
public class UsuarioController {

	private final Logger logger = LoggerFactory.getLogger(UsuarioController.class);

	@SuppressWarnings("rawtypes")
	@Autowired
	private CircuitBreakerFactory cbFactory;

	@Autowired
	UsuarioRepository uRepository;

	@Autowired
	UsuarioPwRepository upRepository;

	@Autowired
	UsuarioFilesRepository ufRepository;

	@Autowired
	IUsuarioService uService;

	@Autowired
	AuthFeignClient aClient;

	@Autowired
	RegistroFeignClient rClient;

	@Autowired
	InterventorFeignClient iClient;

	@Autowired
	RecomendacionesFeignClient rmdClient;

	@Autowired
	NotificacionesFeignClient nClient;

	@Autowired
	EstadisticaFeignClient eClient;

	// Listar todos los usuarios
	@GetMapping("/users/listar/")
	@ResponseStatus(code = HttpStatus.CREATED)
	public List<Usuario> listarUsuarios() throws IOException {
		try {
			return uRepository.findAll();
		} catch (Exception e) {
			throw new IOException("Error en lista " + e.getMessage());
		}
	}

	// Listar todos los usuarios
	@GetMapping("/users/listarPw/")
	@ResponseStatus(code = HttpStatus.CREATED)
	public List<UsuarioPw> listarUsuariosPw() throws IOException {
		try {
			return upRepository.findAll();
		} catch (Exception e) {
			throw new IOException("Error listaPw, usuarios: " + e.getMessage());
		}

	}

	// Retorna usuario
	@GetMapping("/users/findUsername/{username}")
	@ResponseStatus(HttpStatus.OK)
	public Usuario findUserByUsername(@PathVariable("username") String username) throws IOException {
		if (existsByUsername(username)) {
			return uRepository.findByUsername(username);
		}
		throw new IOException("Usuario: " + username + " no existe!");
	}

	// Retorna usuarioPw
	@GetMapping("/users/findUsernamePw/{username}")
	@ResponseStatus(HttpStatus.OK)
	public UsuarioPw findUserByUsernamePw(@PathVariable("username") String username) throws IOException {
		if (existsByUsername(username)) {
			return upRepository.findByUsername(username);
		}
		throw new IOException("Usuario: " + username + " no existe!");
	}

	// Buscar Usuario por username, email o cellphone
	@GetMapping("/users/encontrarUsuario/{usuario}")
	@ResponseStatus(HttpStatus.OK)
	public Usuario findByUsernameOrEmailOrPhone(@PathVariable("usuario") String usuario) {
		return uRepository.findByUsernameOrEmailOrCellPhone(usuario, usuario, usuario);
	}

	// Retorna roles de usuario
	@GetMapping("/users/verRoleUsuario/{username}")
	public List<String> verRoleDeUsuario(@PathVariable("username") String username) throws IOException {
		if (existsByUsername(username)) {
			UsuarioPw usuario = upRepository.findByUsername(username);
			List<Roles> roles = usuario.getRoles();
			List<String> rolesList = new ArrayList<String>();
			for (int i = 0; i < roles.size(); i++) {
				rolesList.add(roles.get(i).getName());
			}
			return rolesList;
		}
		throw new IOException("Usuario: " + username + " no existe!");
	}

	// Ver username de un usuario
	@GetMapping("/users/verUsuario/{username}")
	@ResponseStatus(code = HttpStatus.OK)
	public String verUsername(@PathVariable("username") String username) {
		if (usuarioExists(username)) {
			Usuario usuario = uRepository.findByUsernameOrEmailOrCellPhone(username, username, username);
			return usuario.getUsername();
		} else {
			return "Usuario no encontrado: " + username;
		}
	}

	// Crear usuario basado en registro
	@PostMapping("/users/crearRegistro/")
	@ResponseStatus(HttpStatus.CREATED)
	public Boolean crearUsuarios(@RequestBody Usuario u, @RequestParam String password,
			@RequestParam List<String> roles) throws IOException {
		try {
			List<Roles> listaRoles = uService.obtenerRoles(roles);
			UsuarioPw usuarioPw = new UsuarioPw(u.getUsername(), password, true, 0, 0, listaRoles);
			UsuarioFiles uf = uService.crearUf(u.getUsername());
			uRepository.save(u);
			upRepository.save(usuarioPw);
			ufRepository.save(uf);
			if (cbFactory.create("usuario").run(() -> aClient.crearUsuario(usuarioPw), e -> errorConexion(e))) {
				logger.info("Creacion Correcta");
			}
			if (cbFactory.create("usuario").run(
					() -> rmdClient.crearRecomendacion(u.getUsername(), u.getInterests(), u.getLocation()),
					e -> errorConexion(e))) {
				logger.info("Creacion Recomendacion Correcta");
			}
			if (cbFactory.create("usuario").run(() -> nClient.crearNotificaciones(u.getUsername(), u.getEmail()),
					e -> errorConexion(e))) {
				logger.info("Creacion Notificacion Correcta");
			}
			if (cbFactory.create("usuario").run(() -> eClient.crearUsuarioNotificaciones(u.getUsername()),
					e -> errorConexion(e))) {
				logger.info("Creacion Notificacion Correcta");
			}
			return true;
		} catch (Exception e) {
			throw new IOException("Error en la creacion");
		}
	}

	// Peticion para eliminar un usuario
	@PutMapping("/users/eliminarAdmin/{username}")
	@ResponseStatus(code = HttpStatus.OK)
	public void eliminarAdmin(@PathVariable("username") String username) {
		if (existsByUsername(username)) {
			if (cbFactory.create("usuario").run(() -> iClient.peticionEliminarUsuarios(username),
					e -> errorConexion(e))) {
				logger.info("Peticion de eliminacion enviada");
			}
		}
	}

	// Eliminar un usuario
	@DeleteMapping("/users/eliminar/{username}")
	@ResponseStatus(code = HttpStatus.ACCEPTED)
	public Boolean eliminarUsuario(@PathVariable("username") String username) throws IOException {
		if (usuarioExists(username)) {
			Usuario uDelete = uRepository.findByUsernameOrEmailOrCellPhone(username, username, username);
			UsuarioFiles ufDelete = ufRepository.findByUsername(uDelete.getUsername());
			UsuarioPw upDelete = upRepository.findByUsername(uDelete.getUsername());
			uRepository.delete(uDelete);
			ufRepository.delete(ufDelete);
			upRepository.delete(upDelete);
			if (cbFactory.create("usuario").run(() -> aClient.eliminarUsuario(upDelete), e -> errorConexion(e))) {
				logger.info("Eliminacion Autenticacion Correcta");
			}
			if (cbFactory.create("usuario").run(() -> rClient.eliminarUsuario(upDelete), e -> errorConexion(e))) {
				logger.info("Eliminacion Registro Correcta");
			}
			if (cbFactory.create("usuario").run(() -> rmdClient.eliminarRecomendacion(upDelete.getUsername()),
					e -> errorConexion(e))) {
				logger.info("Eliminacion Recomendacion Correcta");
			}
			if (cbFactory.create("usuario").run(() -> nClient.eliminarNotificacion(upDelete.getUsername()),
					e -> errorConexion(e))) {
				logger.info("Eliminacion Notificacion Correcta");
			}
			if (cbFactory.create("usuario").run(() -> eClient.borrarEstadisticasUsuario(upDelete.getUsername()),
					e -> errorConexion(e))) {
				logger.info("Eliminacion Estadistica Correcta");
			}
			return true;
		}
		return false;
	}

	// Enviar codigo para verificar edicion
	@PutMapping("/users/enviarCodigo/{username}")
	@ResponseStatus(code = HttpStatus.OK)
	public void enviarCodigo(@PathVariable("username") String username) {
		if (existsByUsername(username)) {
			Integer codigo = (int) (100000 * Math.random() + 99999);
			UsuarioPw usuario = upRepository.findByUsername(username);
			usuario.setCode(codigo);
			upRepository.save(usuario);
			nClient.enviarCodigoEditUsuario(username, codigo);
		}
	}

	// Verificar codigo
	@GetMapping("/users/verificarCodigo/{username}")
	@ResponseStatus(code = HttpStatus.OK)
	public Boolean verificarEdicion(@PathVariable("username") String username,
			@RequestParam(value = "code") Integer codigo) throws IOException {
		if (existsByUsername(username)) {
			UsuarioPw usuario = upRepository.findByUsername(username);
			if (usuario.getCode().compareTo(codigo) == 0) {
				usuario.setCode(0);
				upRepository.save(usuario);
				return true;
			} else {
				return false;
			}
		}
		throw new IOException("Usuario no existe");
	}

	// Editar un usuario
	@PutMapping("/users/editar/{username}")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<?> editarUsuario(@PathVariable("username") String username, @RequestBody Usuario usuario) {
		if (existsByUsername(username)) {
			Usuario uDb = uRepository.findByUsername(username);
			uDb = uService.editUser(uDb, usuario);
			try {
				uRepository.save(uDb);
				return ResponseEntity.ok("Edicion Exitosa");
			} catch (Exception e) {
				return ResponseEntity.badRequest().body("Error en la edicion" + e.getMessage());
			}
		}
		return ResponseEntity.badRequest().body("Usuario no existe");
	}

	// Editar Username
	@PutMapping("/users/editarUsername/{username}")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<?> editarUsuarioUsername(@PathVariable("username") String username,
			@RequestParam("nuevoUsername") String nuevoUsername) {
		if (existsByUsername(username) && !existsByUsername(nuevoUsername)) {
			Usuario uDb = uRepository.findByUsername(username);
			UsuarioPw uPwDb = upRepository.findByUsername(username);
			UsuarioFiles uFilesDb = ufRepository.findByUsername(username);
			uDb.setUsername(nuevoUsername);
			uPwDb.setUsername(nuevoUsername);
			uFilesDb.setUsername(nuevoUsername);
			try {
				uRepository.save(uDb);
				upRepository.save(uPwDb);
				ufRepository.save(uFilesDb);
				if (cbFactory.create("usuario").run(
						() -> aClient.editarUsuarioAuth(username, nuevoUsername, "", new ArrayList<Roles>()),
						e -> errorConexion(e))) {
					logger.info("Edicion Autenticacion Correcta");
				}
				if (cbFactory.create("usuario").run(() -> rClient.editarUsuarioRegistro(username, nuevoUsername, "", "",
						"", new ArrayList<Roles>()), e -> errorConexion(e))) {
					logger.info("Edicion Registro Correcta");
				}
				if (cbFactory.create("usuario").run(() -> nClient.editUser(username, nuevoUsername, ""),
						e -> errorConexion(e))) {
					logger.info("Edicion Notificaciones Correcta");
				}
				if (cbFactory.create("usuario").run(() -> eClient.editUser(username, nuevoUsername),
						e -> errorConexion(e))) {
					logger.info("Edicion Notificaciones Correcta");
				}
				if (cbFactory.create("usuario").run(
						() -> rmdClient.editUser(username, nuevoUsername, new ArrayList<String>()),
						e -> errorConexion(e))) {
					logger.info("Edicion Notificaciones Correcta");
				}
				return ResponseEntity.badRequest().body("Username cambiado correctamente");
			} catch (Exception e) {
				return ResponseEntity.badRequest().body("Error en la edicion" + e.getMessage());
			}
		}
		return ResponseEntity.badRequest().body("Usuario no existe");
	}

	// Editar Celular
	@PutMapping("/users/editarCellPhone/{username}")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<?> editarUsuarioCellPhone(@PathVariable("username") String username,
			@RequestParam("nuevoCellPhone") String nuevoCellPhone) {
		if (existsByUsername(username) && !existsByCellPhone(nuevoCellPhone)) {
			Usuario uDb = uRepository.findByUsername(username);
			uDb.setCellPhone(nuevoCellPhone);
			try {
				uRepository.save(uDb);
				if (cbFactory.create("usuario").run(() -> rClient.editarUsuarioRegistro(username, "", nuevoCellPhone,
						"", "", new ArrayList<Roles>()), e -> errorConexion(e))) {
					logger.info("Edicion Registro Correcta");
				}
				return ResponseEntity.badRequest().body("Celular cambiado correctamente");
			} catch (Exception e) {
				return ResponseEntity.badRequest().body("Error en la edicion" + e.getMessage());
			}
		}
		return ResponseEntity.badRequest().body("Usuario no existe");
	}

	// Editar Email
	@PutMapping("/users/editarEmail/{username}")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<?> editarUsuarioEmail(@PathVariable("username") String username,
			@RequestParam("nuevoEmail") String nuevoEmail) {
		if (existsByUsername(username) && !existsByCellPhone(nuevoEmail)) {
			Usuario uDb = uRepository.findByUsername(username);
			uDb.setCellPhone(nuevoEmail);
			try {
				uRepository.save(uDb);
				if (cbFactory.create("usuario").run(
						() -> rClient.editarUsuarioRegistro(username, "", "", nuevoEmail, "", new ArrayList<Roles>()),
						e -> errorConexion(e))) {
					logger.info("Edicion Registro Correcta");
				}
				if (cbFactory.create("usuario").run(() -> nClient.editUser(username, "", nuevoEmail),
						e -> errorConexion(e))) {
					logger.info("Edicion Notificaciones Correcta");
				}
				return ResponseEntity.badRequest().body("Email cambiado correctamente");
			} catch (Exception e) {
				return ResponseEntity.badRequest().body("Error en la edicion" + e.getMessage());
			}
		}
		return ResponseEntity.badRequest().body("Usuario no existe");
	}

	// Editar ubicacion de usuario
	@PutMapping("/users/editarUbicacion/{username}")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<?> eUbicacion(@PathVariable("username") String username,
			@RequestParam(value = "location") List<Double> usuarioLocation) {
		if (existsByUsername(username)) {
			try {
				Usuario uDb = uRepository.findByUsernameOrEmailOrCellPhone(username, username, username);
				uDb.setLocation(new ArrayList<Double>(Arrays.asList(
						(new BigDecimal(usuarioLocation.get(0)).setScale(5, RoundingMode.HALF_UP)).doubleValue(),
						(new BigDecimal(usuarioLocation.get(1)).setScale(5, RoundingMode.HALF_UP).doubleValue()))));
				uRepository.save(uDb);
				if (cbFactory.create("usuario").run(() -> rmdClient.editarUbicacion(username, uDb.getLocation()),
						e -> errorConexion(e))) {
					logger.info("Edicion Registro Correcta");
				}
				return ResponseEntity.ok("Ubicacion actulizada");
			} catch (Exception e) {
				return ResponseEntity.badRequest()
						.body("Error en la edicion:" + e.getMessage() + "E -->" + e.getLocalizedMessage());
			}
		}
		return ResponseEntity.badRequest().body("Usuario: " + username + " No existe");
	}

	// Editar Contraseña
	@PutMapping("/users/editarContrasena/{username}")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<?> eContrasena(@PathVariable("username") String username,
			@RequestParam(value = "password") String password) {
		if (existsByUsername(username)) {
			try {
				UsuarioPw uDb = upRepository.findByUsername(username);
				String newPassword = uService.codificar(password);
				uDb.setPassword(newPassword);
				upRepository.save(uDb);
				if (cbFactory.create("usuario").run(
						() -> aClient.editarUsuarioAuth(username, "", newPassword, new ArrayList<Roles>()),
						e -> errorConexion(e))) {
					logger.info("Edicion Contrasena Autenticacion Correcta");
				}
				if (cbFactory.create("usuario").run(
						() -> rClient.editarUsuarioRegistro(username, "", "", "", newPassword, new ArrayList<Roles>()),
						e -> errorConexion(e))) {
					logger.info("Edicion Contrasena Registro Correcta");
				}
				return ResponseEntity.ok("Contrasena actulizada");
			} catch (Exception e) {
				return ResponseEntity.badRequest()
						.body("Error en la edicion:" + e.getMessage() + "E -->" + e.getLocalizedMessage());
			}
		}
		return ResponseEntity.badRequest().body("Usuario: " + username + " No existe");
	}

	// Asignar role Moderador
	@PutMapping("/users/roleModerator/{username}")
	@ResponseStatus(code = HttpStatus.OK)
	public ResponseEntity<?> asignarModerator(@PathVariable("username") String username) {
		if (existsByUsername(username)) {
			UsuarioPw usuario = upRepository.findByUsername(username);
			Roles userRole1 = new Roles("2", "ROLE_MODERATOR");
			List<Roles> roles = usuario.getRoles();
			if (!roles.contains(userRole1)) {
				roles.add(userRole1);
				usuario.setRoles(roles);
				upRepository.save(usuario);
				if (cbFactory.create("usuario").run(() -> aClient.editarUsuarioAuth(username, "", "", roles),
						e -> errorConexion(e))) {
					logger.info("Edicion roles Autenticacion Correcta");
				}
				if (cbFactory.create("usuario").run(
						() -> rClient.editarUsuarioRegistro(username, "", "", "", "", roles), e -> errorConexion(e))) {
					logger.info("Edicion roles Registro Correcta");
				}
				return ResponseEntity.ok("Role Moderator asignado");
			} else {
				return ResponseEntity.badRequest().body("Usuario: " + username + " ya tiene Role Moderator");
			}
		} else {
			return ResponseEntity.badRequest().body("Usuario: " + username + " No existe");
		}
	}

	// Asignar role Admin
	@PutMapping("/users/roleAdmin/{username}")
	@ResponseStatus(code = HttpStatus.OK)
	public ResponseEntity<?> asignarAdmin(@PathVariable("username") String username) {
		if (existsByUsername(username)) {
			UsuarioPw usuario = upRepository.findByUsername(username);
			Roles userRole1 = new Roles("1", "ROLE_ADMIN");
			List<Roles> roles = usuario.getRoles();
			if (!roles.contains(userRole1)) {
				roles.add(userRole1);
				usuario.setRoles(roles);
				upRepository.save(usuario);
				if (cbFactory.create("usuario").run(() -> aClient.editarUsuarioAuth(username, "", "", roles),
						e -> errorConexion(e))) {
					logger.info("Edicion roles Autenticacion Correcta");
				}
				if (cbFactory.create("usuario").run(
						() -> rClient.editarUsuarioRegistro(username, "", "", "", "", roles), e -> errorConexion(e))) {
					logger.info("Edicion Contrasena Registro Correcta");
				}
				return ResponseEntity.ok("Role Admin asignado");
			} else {
				return ResponseEntity.badRequest().body("Usuario: " + username + " ya tiene Role Admin");
			}
		} else {
			return ResponseEntity.badRequest().body("Usuario: " + username + " No existe");
		}
	}

	// Cambiar imagen
	@PutMapping("/users/file/uploadImage/{username}")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<?> uploadImage(@PathVariable("username") String username,
			@RequestParam(value = "image") MultipartFile file) {
		if (existsByUsername(username)) {
			UsuarioFiles uploadFile = uService.ponerImagen(username, file);
			ufRepository.save(uploadFile);
			return ResponseEntity.ok("Imagen añadida");
		} else {
			return ResponseEntity.badRequest().body("Error: " + username + " no existe");
		}
	}

	@GetMapping("/users/file/binary/{username}")
	@ResponseStatus(HttpStatus.OK)
	public String binaryToStringFile(@PathVariable("username") String username) {
		if (ufRepository.existsByUsername(username)) {
			UsuarioFiles uf = ufRepository.findByUsername(username);
			byte[] data = null;
			UsuarioFiles file = ufRepository.findImageById(uf.getId(), UsuarioFiles.class);
			if (file != null) {
				data = file.getContent().getData();
			}
			return Base64.getEncoder().encodeToString(data);
		}
		return "Usuario no encontrado";
	}

	// Descargar imagen
	@GetMapping(value = "/users/file/downloadImage/{username}", produces = { MediaType.IMAGE_JPEG_VALUE,
			MediaType.IMAGE_PNG_VALUE })
	@ResponseStatus(HttpStatus.OK)
	public byte[] image(@PathVariable String username) {
		UsuarioFiles usuario = ufRepository.findByUsername(username);
		byte[] data = null;
		UsuarioFiles file = ufRepository.findImageById(usuario.getId(), UsuarioFiles.class);
		if (file != null) {
			data = file.getContent().getData();
		}
		return data;
	}

	// Preguntar si usuario existe por username
	@GetMapping("/users/existUsuario/{username}")
	@ResponseStatus(HttpStatus.FOUND)
	public Boolean usuarioExists(@PathVariable("username") String username) {
		return uRepository.existsByUsernameOrEmailOrCellPhone(username, username, username);
	}

	// Preguntar si usuario existe por los 3 metodos
	@GetMapping("/users/usuarioExisteDatos/")
	@ResponseStatus(HttpStatus.FOUND)
	public Boolean preguntarUsuarioExiste(@RequestParam(value = "username") String username,
			@RequestParam(value = "email") String email, @RequestParam(value = "cellPhone") String cellPhone)
			throws InterruptedException {
		return uRepository.existsByUsernameOrEmailOrCellPhone(username, email, cellPhone);
	}

	// Preguntar si usuario existe por username
	@GetMapping("/users/existUsername/{username}")
	@ResponseStatus(HttpStatus.FOUND)
	public Boolean existsByUsername(@PathVariable("username") String username) {
		return uRepository.existsByUsername(username);
	}

	// Preguntar si usuario existe por email
	@GetMapping("/users/existEmail/{email}")
	@ResponseStatus(HttpStatus.FOUND)
	public Boolean existsByEmail(@PathVariable("email") String email) {
		return uRepository.existsByEmail(email);
	}

	// Preguntar si usuario existe por email
	@GetMapping("/users/existCellPhone/{cellPhone}")
	@ResponseStatus(HttpStatus.FOUND)
	public Boolean existsByCellPhone(@PathVariable("cellPhone") String cellPhone) {
		return uRepository.existsByCellPhone(cellPhone);
	}

	@GetMapping("/users/existCedula/{cedula}")
	@ResponseStatus(HttpStatus.FOUND)
	public Boolean existsByCedula(@PathVariable("cedula") String cedula) {
		return uRepository.existsByCedula(cedula);
	}

	// Obtener edad
	@GetMapping("/users/obtenerEdad/{username}")
	public ResponseEntity<?> edadUsurio(@PathVariable("username") String username) {
		if (existsByUsername(username)) {
			Usuario usuario = uRepository.findByUsername(username);
			DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
			LocalDate fechaNac = LocalDate.parse(usuario.getBirthDate(), fmt);
			LocalDate ahora = LocalDate.now();
			Period periodo = Period.between(fechaNac, ahora);
			return ResponseEntity.ok(periodo.getYears());
		}
		return ResponseEntity.badRequest().body("Usuario No existe");
	}

	// Crear Usuario Cero
	@PostMapping("/users/crearUsuarioMod/")
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<?> nuevoUsuarioMod(@RequestParam("username") String username,
			@RequestParam("cellPhone") String cellPhone, @RequestParam("email") String email,
			@RequestParam("cedula") String cedula, @RequestParam("name") String name,
			@RequestParam("lastName") String lastName, @RequestParam("birthDate") String birthDate,
			@RequestParam("gender") Integer gender, @RequestParam("phone") String phone,
			@RequestParam("economicActivity") String economicActivity,
			@RequestParam("economicData") List<String> economicData, @RequestParam("interests") List<String> interests,
			@RequestParam("location") List<Double> location, @RequestParam("headFamily") Boolean headFamily,
			@RequestParam("stakeHolders") String stakeHolders, @RequestParam("contrasenia") String password,
			@RequestParam(value = "image") MultipartFile file) {
		if (!existsByUsername(username)) {
			Usuario usuario = new Usuario(username, cellPhone, email, cedula, name, lastName, birthDate, gender, phone,
					economicActivity, economicData, interests, location, headFamily, stakeHolders);
			UsuarioPw uPw = uService.usuarioPasword(usuario.getUsername(), password);
			UsuarioFiles uploadFile = uService.ponerImagen(usuario.getUsername(), file);
			uRepository.save(usuario);
			upRepository.save(uPw);
			ufRepository.save(uploadFile);
			if (cbFactory.create("usuario").run(() -> aClient.crearUsuario(uPw), e -> errorConexion(e))) {
				logger.info("Creacion Autenticacion Correcta");
			}
			if (cbFactory.create("usuario").run(() -> rmdClient.crearRecomendacion(usuario.getUsername(),
					usuario.getInterests(), usuario.getLocation()), e -> errorConexion(e))) {
				logger.info("Creacion Recomendacion Correcta");
			}
			if (cbFactory.create("usuario").run(
					() -> nClient.crearNotificaciones(usuario.getUsername(), usuario.getEmail()),
					e -> errorConexion(e))) {
				logger.info("Creacion Notificaciones Correcta");
			}
			if (cbFactory.create("usuario").run(() -> eClient.crearUsuarioNotificaciones(usuario.getUsername()),
					e -> errorConexion(e))) {
				logger.info("Creacion Estadistica Correcta");
			}
			return ResponseEntity.ok("Usuario Creado");
		}
		return ResponseEntity.badRequest().body("Usuario ya existe");
	}

	// Metodo para iniciar sesion
	@GetMapping("/users/iniciarSesion/{username}")
	public UsuarioPw autenticacion(@PathVariable String username) throws InterruptedException {
		if (usuarioExists(username)) {
			username = verUsername(username);
			return upRepository.findByUsername(username);
		}
		return null;
	}

	// Metodo para registro
	@GetMapping("/users/registroExistencia/")
	public Boolean registroExistenciaUsuarios(@RequestParam(value = "username") String username,
			@RequestParam(value = "email") String email, @RequestParam(value = "cellPhone") String cellPhone)
			throws InterruptedException {
		Boolean bandera = preguntarUsuarioExiste(username, email, cellPhone);
		return bandera;
	}

	@GetMapping("/users/registroCedula/")
	public Boolean registroCedula(@RequestParam(value = "cedula") String cedula) throws InterruptedException {
		Boolean bandera = existsByCedula(cedula);
		return bandera;
	}

	public Boolean errorConexion(Throwable e) {
		logger.info(e.getMessage());
		return false;
	}

	@PutMapping("/users/arreglar/")
	public String arreglarUsuarios() throws IOException {
		List<Registro> lReg = cbFactory.create("usuario").run(() -> rClient.listar(), e -> errorArreglarReg(e));
		if (lReg != null) {
			if (lReg.size() != listarUsuarios().size()) {
				List<String> uReg = new ArrayList<String>();
				List<String> cReg = new ArrayList<String>();
				List<String> usuarios = new ArrayList<String>();
				lReg.forEach(l -> {
					uReg.add(l.getUsername());
					cReg.add(l.getCodigo());
				});

				listarUsuarios().forEach(l -> usuarios.add(l.getUsername()));
				uReg.forEach(u -> {
					if (!usuarios.contains(u)) {
						rClient.editarUsuario(u, "123456", new Date().getTime());
						rClient.crearUsuario(u, "123456");
					}
				});
				return "ok";
			}
			return "sin errores";
		}
		return null;
	}

	private List<Registro> errorArreglarReg(Throwable e) {
		logger.info(e.getMessage());
		return null;
	}
}
