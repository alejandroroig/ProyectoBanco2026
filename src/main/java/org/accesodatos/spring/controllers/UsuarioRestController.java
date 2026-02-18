package org.accesodatos.spring.controllers;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.accesodatos.spring.dtos.request.create.UsuarioCreateDTO;
import org.accesodatos.spring.dtos.request.update.UsuarioUpdateDTO;
import org.accesodatos.spring.dtos.response.UsuarioDTO;
import org.accesodatos.spring.services.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioRestController {
    private final UsuarioService usuarioService;

    @GetMapping
    @Operation(summary = "Obtiene todos los usuarios")
    public ResponseEntity<List<UsuarioDTO>> obtenerTodosLosUsuarios() {
        List<UsuarioDTO> usuarios = usuarioService.obtenerTodosLosUsuarios();
        if (usuarios.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204 No Content
        }
        return ResponseEntity.ok(usuarios); // 200 OK
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un usuario por su ID")
    public ResponseEntity<UsuarioDTO> obtenerUsuarioPorId(@PathVariable Long id) {
        UsuarioDTO usuarioDTO = usuarioService.obtenerUsuarioPorId(id);
        return ResponseEntity.ok(usuarioDTO); // 200 OK
    }

    @PostMapping
    @Operation(summary = "Crea un nuevo usuario")
    public ResponseEntity<UsuarioDTO> crearUsuario(@Valid @RequestBody UsuarioCreateDTO dto) {
        UsuarioDTO usuarioCreado = usuarioService.crearUsuario(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioCreado); // 201 Created
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifica los datos de un usuario")
    public ResponseEntity<UsuarioDTO> actualizarUsuario(@PathVariable Long id, @Valid @RequestBody UsuarioUpdateDTO dto) {
        UsuarioDTO usuarioActualizado = usuarioService.actualizarUsuario(id, dto);
        return ResponseEntity.ok(usuarioActualizado); // 200 OK
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina un usuario por su ID")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
