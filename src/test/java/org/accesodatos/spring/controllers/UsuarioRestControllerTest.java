package org.accesodatos.spring.controllers;

import org.accesodatos.spring.dtos.request.create.PerfilCreateDTO;
import org.accesodatos.spring.dtos.request.create.UsuarioCreateDTO;
import org.accesodatos.spring.dtos.response.UsuarioDTO;
import org.accesodatos.spring.services.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioRestController.class)
public class UsuarioRestControllerTest {
    @Autowired
    private MockMvc mockMvc; // Para realizar peticiones HTTP

    @MockitoBean
    private UsuarioService usuarioService; // Moqueamos el servicio para no necesitar base de datos

    @Autowired
    private ObjectMapper objectMapper;

    private UsuarioCreateDTO usuarioCreateDTO;
    private UsuarioDTO usuarioResponseDTO;

    @BeforeEach
    void setUp() {
        // Preparamos el Perfil DTO (parte del request)
        PerfilCreateDTO perfilDTO = new PerfilCreateDTO();
        perfilDTO.setNombreCompleto("Controller Test");
        perfilDTO.setTelefono("600111222");
        perfilDTO.setDireccion("Calle Mayor 1");

        // Preparamos el Usuario Create DTO (request principal)
        usuarioCreateDTO = new UsuarioCreateDTO();
        usuarioCreateDTO.setUsername("controller_test");
        usuarioCreateDTO.setPassword("password123");
        usuarioCreateDTO.setEmail("controllertest@gmail.com");
        usuarioCreateDTO.setPerfil(perfilDTO); // Vinculación

        // 3. Respuesta simulada (lo que devuelve el servicio)
        usuarioResponseDTO = new UsuarioDTO();
        usuarioResponseDTO.setId(1L);
        usuarioResponseDTO.setUsername("controller_test");
    }

    @Test
    void crearUsuarioYPerfil_Exito() throws Exception {
        // GIVEN
        when(usuarioService.crearUsuario(any(UsuarioCreateDTO.class))).thenReturn(usuarioResponseDTO);

        // WHEN & THEN
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("controller_test"));

        verify(usuarioService).crearUsuario(any(UsuarioCreateDTO.class));
    }

    @Test
    void crearUsuario_PerfilInvalido() throws Exception {
        // GIVEN: El teléfono es obligatorio y aquí lo mandamos vacío
        usuarioCreateDTO.getPerfil().setTelefono("");

        // WHEN & THEN
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioCreateDTO)))
                .andExpect(status().isBadRequest());

        // El servicio nunca debe ser llamado si los DTOs fallan la validación
        verify(usuarioService, never()).crearUsuario(any());
    }

    @Test
    void eliminarUsuario_DebeRetornar204() throws Exception {
        // GIVEN: El servicio no hace nada (éxito)
        doNothing().when(usuarioService).eliminarUsuario(1L);

        // WHEN & THEN
        mockMvc.perform(delete("/api/usuarios/1"))
                .andExpect(status().isNoContent()); // Verifica 204 No Content
    }

    @Test
    void eliminarUsuario_ConCuentas_DebeRetornar409() throws Exception {
        // GIVEN: El servicio lanza la excepción que definimos
        doThrow(new IllegalStateException("No se puede eliminar con cuentas"))
                .when(usuarioService).eliminarUsuario(1L);

        // WHEN & THEN
        mockMvc.perform(delete("/api/usuarios/1"))
                .andExpect(status().isConflict()); // Verifica 409 Conflict (gracias al ExceptionHandler)
    }
}
