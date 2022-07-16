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
import java.util.List;

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
import org.springframework.web.server.ResponseStatusException;

import com.app.usuarios.clients.BusquedaFeignClient;
import com.app.usuarios.clients.EstadisticaFeignClient;
import com.app.usuarios.clients.InterventorFeignClient;
import com.app.usuarios.clients.NotificacionesFeignClient;
import com.app.usuarios.clients.RecomendacionesFeignClient;
import com.app.usuarios.models.Roles;
import com.app.usuarios.models.Usuario;
import com.app.usuarios.models.UsuarioFiles;
import com.app.usuarios.models.UsuarioPw;
import com.app.usuarios.repository.UsuarioFilesRepository;
import com.app.usuarios.repository.UsuarioPwRepository;
import com.app.usuarios.repository.UsuarioRepository;
import com.app.usuarios.services.IUsuarioService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class UsuarioController {

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
	InterventorFeignClient iClient;

	@Autowired
	RecomendacionesFeignClient rmdClient;

	@Autowired
	NotificacionesFeignClient nClient;

	@Autowired
	EstadisticaFeignClient eClient;

	@Autowired
	BusquedaFeignClient bClient;

//  ****************************	USUARIOS 	***********************************  //

	// LISTAS USUARIOS
	@GetMapping("/users/listar/")
	@ResponseStatus(code = HttpStatus.OK)
	public List<Usuario> listarUsuarios() throws IOException {
		try {
			return uRepository.findAll();
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error en listar usuarios: " + e.getMessage());
		}
	}

	// LISTAR DATOS SENSIBLES USUARIO
	@GetMapping("/users/listarPw/")
	public List<UsuarioPw> listarUsuariosPw() throws IOException {
		try {
			return upRepository.findAll();
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error en listar usuarios pw: " + e.getMessage());
		}
	}

	// VER USUARIO
	@GetMapping("/users/findUsername/{username}")
	@ResponseStatus(HttpStatus.OK)
	public Usuario findUserByUsername(@PathVariable("username") String username) throws IOException {
		if (existsByUsername(username)) {
			return uRepository.findByUsername(username);
		}
		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario " + username + " no existe");
	}

	// BUSCAR USUARIO POR USERNAME, EMAIL O CELULAR
	@GetMapping("/users/encontrarUsuario/{usuario}")
	@ResponseStatus(HttpStatus.OK)
	public Usuario findByUsernameOrEmailOrPhone(@PathVariable("usuario") String usuario) {
		return uRepository.findByUsernameOrEmailOrCellPhone(usuario, usuario, usuario);
	}

	// VER ROLE DEL USUARIO
	@GetMapping("/users/verRoleUsuario/{username}")
	@ResponseStatus(HttpStatus.OK)
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
		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario " + username + " no existe");
	}

	// VER USERNAME DE USUARIO
	@GetMapping("/users/verUsuario/{username}")
	@ResponseStatus(code = HttpStatus.OK)
	public String verUsername(@PathVariable("username") String username) {
		if (usuarioExists(username)) {
			Usuario usuario = uRepository.findByUsernameOrEmailOrCellPhone(username, username, username);
			return usuario.getUsername();
		}
		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario " + username + " no existe");
	}

	// MICROSERVICIO REGISTRO -> CREAR USUARIO
	@PostMapping("/users/crearRegistro/")
	public Boolean crearUsuarios(@RequestBody Usuario u, @RequestParam String password,
			@RequestParam List<String> roles) throws IOException, ResponseStatusException {

		List<Roles> listaRoles = uService.obtenerRoles(roles);
		UsuarioPw usuarioPw = new UsuarioPw(u.getUsername(), password, true, 0, 0, listaRoles);
		UsuarioFiles uf = uService.crearUf(u.getUsername());

		if (cbFactory.create("usuario").run(
				() -> rmdClient.crearRecomendacion(u.getUsername(), u.getInterests(), u.getLocation()),
				e -> errorCreacionRecomendacion(e))) {
			log.info("Creacion Recomendacion Correcta");
			if (cbFactory.create("usuario").run(() -> nClient.crearNotificaciones(u.getUsername(), u.getEmail()),
					e -> errorCreacionNotificaciones(e))) {
				log.info("Creacion Notificacion Correcta");
				if (cbFactory.create("usuario").run(() -> eClient.crearUsuarioNotificaciones(u.getUsername()),
						e -> errorCreacionEstadisticas(e))) {
					log.info("Creacion Notificacion Correcta");
					uRepository.save(u);
					upRepository.save(usuarioPw);
					ufRepository.save(uf);
					return true;
				} else {
					rmdClient.eliminarRecomendacion(u.getUsername());
					nClient.eliminarNotificacion(u.getUsername());
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error en la creacion");
				}
			} else {
				rmdClient.eliminarRecomendacion(u.getUsername());
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error en la creacion");
			}
		}
		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error en la creacion");
	}

	// PETICION A INTERVENTOR PARA ELIMINAR USUARIO
	@PutMapping("/users/eliminarAdmin/{username}")
	@ResponseStatus(code = HttpStatus.OK)
	public void eliminarAdmin(@PathVariable("username") String username) {
		if (existsByUsername(username)) {
			if (cbFactory.create("usuario").run(() -> iClient.peticionEliminarUsuarios(username),
					e -> errorConexion(e))) {
				log.info("Peticion de eliminacion enviada");
			}
		}
	}

	// BORRAR PETICION PARA ELIMINAR USUARIO
	@PutMapping("/users/eliminarPeticionAdmin/{username}")
	@ResponseStatus(code = HttpStatus.OK)
	public void eliminarPeticionUsuario(@PathVariable("username") String username) {
		if (existsByUsername(username)) {
			if (cbFactory.create("usuario").run(() -> iClient.eliminarPeticionUsuarios(username),
					e -> errorConexion(e))) {
				log.info("Eliminacion de peticion lista");
			}
		}
	}

	// ELIMINAR USUARIO
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

			if (cbFactory.create("usuario").run(() -> rmdClient.eliminarRecomendacion(upDelete.getUsername()),
					e -> errorConexion(e))) {
				log.info("Eliminacion Recomendacion Correcta");
			}
			if (cbFactory.create("usuario").run(() -> bClient.eliminarBusquedasUsername(upDelete.getUsername()),
					e -> errorConexion(e))) {
				log.info("Eliminacion Recomendacion Correcta");
			}
			if (cbFactory.create("usuario").run(() -> nClient.eliminarNotificacion(upDelete.getUsername()),
					e -> errorConexion(e))) {
				log.info("Eliminacion Notificacion Correcta");
			}
			if (cbFactory.create("usuario").run(() -> eClient.borrarEstadisticasUsuario(upDelete.getUsername()),
					e -> errorConexion(e))) {
				log.info("Eliminacion Estadistica Correcta");
			}
			return true;
		}
		return false;
	}

	// ENVIAR CODIGO PARA LA VERIFICACION DE CAMBIOS
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
		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario " + username + " no existe");
	}

	// VERIFICAR CODIGO
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
		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario " + username + " no existe");
	}

	// EDITAR USUARIO
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
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error en la edicion");
			}
		}
		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario " + username + " no existe");
	}

	// EDITAR USERNAME
	@PutMapping("/users/editarUsername/{username}")
	@ResponseStatus(HttpStatus.OK)
	public Boolean editarUsuarioUsername(@PathVariable("username") String username,
			@RequestParam("nuevoUsername") String nuevoUsername) {
		if (existsByUsername(username) && !existsByUsername(nuevoUsername)) {
			Usuario uDb = uRepository.findByUsername(username);
			UsuarioPw uPwDb = upRepository.findByUsername(username);
			UsuarioFiles uFilesDb = ufRepository.findByUsername(username);
			uDb.setUsername(nuevoUsername);
			uPwDb.setUsername(nuevoUsername);
			uFilesDb.setUsername(nuevoUsername);

			if (cbFactory.create("usuario").run(() -> nClient.editUser(username, nuevoUsername, ""),
					e -> errorConexion(e))) {
				log.info("Edicion Notificaciones Correcta");
				if (cbFactory.create("usuario").run(() -> eClient.editUser(username, nuevoUsername),
						e -> errorConexion(e))) {
					log.info("Edicion Notificaciones Correcta");
					if (cbFactory.create("usuario").run(
							() -> rmdClient.editUser(username, nuevoUsername, new ArrayList<String>()),
							e -> errorConexion(e))) {
						log.info("Edicion Notificaciones Correcta");
						uRepository.save(uDb);
						upRepository.save(uPwDb);
						ufRepository.save(uFilesDb);
						return true;
					} else {
						nClient.editUser(nuevoUsername, username, "");
						eClient.editUser(nuevoUsername, username);
						throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario " + username + " no existe");
					}
				} else {
					nClient.editUser(nuevoUsername, username, "");
					throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario " + username + " no existe");
				}
			}
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error en la edicion");
		}
		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario " + username + " no existe");
	}

	// EDITAR CELULAR
	@PutMapping("/users/editarCellPhone/{username}")
	@ResponseStatus(HttpStatus.OK)
	public Boolean editarUsuarioCellPhone(@PathVariable("username") String username,
			@RequestParam("nuevoCellPhone") String nuevoCellPhone) {
		if (existsByUsername(username) && !existsByCellPhone(nuevoCellPhone)) {
			Usuario uDb = uRepository.findByUsername(username);
			uDb.setCellPhone(nuevoCellPhone);
			try {
				uRepository.save(uDb);
				return true;
			} catch (Exception e) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error en la edicion");
			}
		}
		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario " + username + " no existe");
	}

	// EDITAR EMAIL
	@PutMapping("/users/editarEmail/{username}")
	@ResponseStatus(HttpStatus.OK)
	public Boolean editarUsuarioEmail(@PathVariable("username") String username,
			@RequestParam("nuevoEmail") String nuevoEmail) {
		if (existsByUsername(username) && !existsByCellPhone(nuevoEmail)) {
			Usuario uDb = uRepository.findByUsername(username);
			uDb.setCellPhone(nuevoEmail);
			try {
				if (cbFactory.create("usuario").run(() -> nClient.editUser(username, "", nuevoEmail),
						e -> errorConexion(e))) {
					log.info("Edicion Notificaciones Correcta");
					uRepository.save(uDb);
				}
				return true;
			} catch (Exception e) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error en la edicion");
			}
		}
		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario " + username + " no existe");
	}

	// EDITAR UBICACION DE USUARIO
	@PutMapping("/users/editarUbicacion/{username}")
	@ResponseStatus(HttpStatus.OK)
	public Boolean eUbicacion(@PathVariable("username") String username,
			@RequestParam(value = "location") List<Double> usuarioLocation) {
		if (existsByUsername(username)) {
			try {
				Usuario uDb = uRepository.findByUsernameOrEmailOrCellPhone(username, username, username);
				uDb.setLocation(new ArrayList<Double>(Arrays.asList(
						(new BigDecimal(usuarioLocation.get(0)).setScale(5, RoundingMode.HALF_UP)).doubleValue(),
						(new BigDecimal(usuarioLocation.get(1)).setScale(5, RoundingMode.HALF_UP).doubleValue()))));
				if (cbFactory.create("usuario").run(() -> rmdClient.editarUbicacion(username, uDb.getLocation()),
						e -> errorConexion(e))) {
					log.info("Edicion Registro Correcta");
					uRepository.save(uDb);
				}
				return true;
			} catch (Exception e) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error en la edicion");
			}
		}
		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario " + username + " no existe");
	}

	// EDITAR CONTRASEÑA
	@PutMapping("/users/editarContrasena/{username}")
	@ResponseStatus(HttpStatus.OK)
	public Boolean eContrasena(@PathVariable("username") String username,
			@RequestParam(value = "password") String password) {
		if (existsByUsername(username)) {
			try {
				UsuarioPw uDb = upRepository.findByUsername(username);
				String newPassword = uService.codificar(password);
				uDb.setPassword(newPassword);
				upRepository.save(uDb);
				return true;
			} catch (Exception e) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error en la edicion");
			}
		}
		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario " + username + " no existe");
	}

	// ASIGNAR ROLE MODERATOR
	@PutMapping("/users/roleModerator/{username}")
	@ResponseStatus(code = HttpStatus.OK)
	public Boolean asignarModerator(@PathVariable("username") String username) {
		if (existsByUsername(username)) {
			UsuarioPw usuario = upRepository.findByUsername(username);
			Roles userRole1 = new Roles("2", "ROLE_MODERATOR");
			List<Roles> roles = usuario.getRoles();
			if (!roles.contains(userRole1)) {
				roles.add(userRole1);
				usuario.setRoles(roles);
				upRepository.save(usuario);
				return true;
			} else {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
						"Usuario: " + username + " ya tiene Role Moderator");
			}
		} else {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario " + username + " no existe");
		}
	}

	// ASIGNAR ROLE ADMIN
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
				return ResponseEntity.ok("Role Admin asignado");
			} else {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
						"Usuario: " + username + " ya tiene Role Admin");
			}
		}
		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario " + username + " no existe");
	}

	// CAMBIAR IMAGEN
	@PutMapping("/users/file/uploadImage/{username}")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<?> uploadImage(@PathVariable("username") String username,
			@RequestParam(value = "image") MultipartFile file) {
		if (existsByUsername(username)) {
			UsuarioFiles uploadFile = uService.ponerImagen(username, file);
			ufRepository.save(uploadFile);
			return ResponseEntity.ok("Imagen añadida");
		}
		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario " + username + " no existe");
	}

	// IMAGEN A BINARIO
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
		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario " + username + " no existe");
	}

	// DESCARGAR IMAGEN
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

	// PREGUNTAR SI UN USUARIO EXISTE
	@GetMapping("/users/existUsuario/{username}")
	@ResponseStatus(HttpStatus.FOUND)
	public Boolean usuarioExists(@PathVariable("username") String username) {
		return uRepository.existsByUsernameOrEmailOrCellPhone(username, username, username);
	}

	// PREGUNTAR SI UN USUARIO EXISTE: EMAIL, CORREO, USERNAME
	@GetMapping("/users/usuarioExisteDatos/")
	@ResponseStatus(HttpStatus.FOUND)
	public Boolean preguntarUsuarioExiste(@RequestParam(value = "username") String username,
			@RequestParam(value = "email") String email, @RequestParam(value = "cellPhone") String cellPhone)
			throws InterruptedException {
		return uRepository.existsByUsernameOrEmailOrCellPhone(username, email, cellPhone);
	}

	// PREGUNTAR SI UN USUARIO EXISTE USERNAME
	@GetMapping("/users/existUsername/{username}")
	@ResponseStatus(HttpStatus.FOUND)
	public Boolean existsByUsername(@PathVariable("username") String username) {
		return uRepository.existsByUsername(username);
	}

	// PREGUNTAR SI UN USUARIO EXISTE POR EMAIL
	@GetMapping("/users/existEmail/{email}")
	@ResponseStatus(HttpStatus.FOUND)
	public Boolean existsByEmail(@PathVariable("email") String email) {
		return uRepository.existsByEmail(email);
	}

	// PREGUNTAR SI UN USUARIO EXISTE POR CELULAR
	@GetMapping("/users/existCellPhone/{cellPhone}")
	@ResponseStatus(HttpStatus.FOUND)
	public Boolean existsByCellPhone(@PathVariable("cellPhone") String cellPhone) {
		return uRepository.existsByCellPhone(cellPhone);
	}

	// PREGUNTAR SI UN USUARIO EXISTE POR CEDULA
	@GetMapping("/users/existCedula/{cedula}")
	@ResponseStatus(HttpStatus.FOUND)
	public Boolean existsByCedula(@PathVariable("cedula") String cedula) {
		return uRepository.existsByCedula(cedula);
	}

	// OBTENER EDAD
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
		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario " + username + " no existe");
	}

	// CREAR PRIMER USUARIO
	@PostMapping("/users/crearUsuarioMod/")
	@ResponseStatus(HttpStatus.CREATED)
	public Boolean nuevoUsuarioMod(@RequestParam("username") String username,
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

			if (cbFactory.create("usuario").run(() -> rmdClient.crearRecomendacion(usuario.getUsername(),
					usuario.getInterests(), usuario.getLocation()), e -> errorCreacionRecomendacion(e))) {
				log.info("Creacion Recomendacion Correcta");
				if (cbFactory.create("usuario").run(
						() -> nClient.crearNotificaciones(usuario.getUsername(), usuario.getEmail()),
						e -> errorCreacionNotificaciones(e))) {
					log.info("Creacion Notificacion Correcta");
					if (cbFactory.create("usuario").run(() -> eClient.crearUsuarioNotificaciones(usuario.getUsername()),
							e -> errorCreacionEstadisticas(e))) {
						log.info("Creacion Notificacion Correcta");
						uRepository.save(usuario);
						upRepository.save(uPw);
						ufRepository.save(uploadFile);
						return true;
					} else {
						rmdClient.eliminarRecomendacion(usuario.getUsername());
						nClient.eliminarNotificacion(usuario.getUsername());
						throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error en la creacion");
					}
				} else {
					rmdClient.eliminarRecomendacion(usuario.getUsername());
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error en la creacion");
				}
			}
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error en la creacion");
		}
		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuario ya existe");

	}

	// INICIAR SESION
	@GetMapping("/users/iniciarSesion/{username}")
	public UsuarioPw autenticacion(@PathVariable String username) throws InterruptedException {
		if (usuarioExists(username)) {
			username = verUsername(username);
			return upRepository.findByUsername(username);
		}
		return null;
	}

	// VERIFICAR REGISTRO
	@GetMapping("/users/registroExistencia/")
	public Boolean registroExistenciaUsuarios(@RequestParam(value = "username") String username,
			@RequestParam(value = "email") String email, @RequestParam(value = "cellPhone") String cellPhone)
			throws InterruptedException {
		Boolean bandera = preguntarUsuarioExiste(username, email, cellPhone);
		return bandera;
	}

	// CEDULA EXISTE
	@GetMapping("/users/registroCedula/")
	public Boolean registroCedula(@RequestParam(value = "cedula") String cedula) throws InterruptedException {
		Boolean bandera = existsByCedula(cedula);
		return bandera;
	}

	@PutMapping("/users/arreglar/")
	public String arreglarUsuarios() throws IOException {

		return null;
	}

	// ELIMINAR TODOS LOS USUARIOS
	@DeleteMapping("/users/eliminar/all/usuarios/")
	@ResponseStatus(code = HttpStatus.ACCEPTED)
	public void eliminarAllUsuarios() {
		uRepository.deleteAll();
		ufRepository.deleteAll();
		upRepository.deleteAll();

		if (cbFactory.create("usuario").run(() -> rmdClient.eliminarAllUsuario(), e -> errorConexion(e))) {
			log.info("Eliminacion Todos usuarios, Recomendacion Correcta");
		}
		if (cbFactory.create("usuario").run(() -> nClient.eliminarAllUsuario(), e -> errorConexion(e))) {
			log.info("Eliminacion Todos usuarios, Notificacion Correcta");
		}
		if (cbFactory.create("usuario").run(() -> eClient.eliminarAllUsuario(), e -> errorConexion(e))) {
			log.info("Eliminacion Todos usuarios, Correcta");
		}
	}

//  ****************************	FUNCIONES TOLERANCIA A FALLOS	***********************************  //

	public Boolean errorConexion(Throwable e) {
		log.info(e.getMessage());
		return false;
	}

	public Boolean errorCreacionRecomendacion(Throwable e) {
		log.info(e.getMessage());
		return false;
	}

	public Boolean errorCreacionNotificaciones(Throwable e) {
		log.info(e.getMessage());
		return false;
	}

	public Boolean errorCreacionEstadisticas(Throwable e) {
		log.info(e.getMessage());
		return false;
	}

}
