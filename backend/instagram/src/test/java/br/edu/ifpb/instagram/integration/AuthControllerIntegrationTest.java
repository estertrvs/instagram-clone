package br.edu.ifpb.instagram.integration;

import br.edu.ifpb.instagram.model.dto.UserDto;
import br.edu.ifpb.instagram.service.UserService;
import br.edu.ifpb.instagram.security.JwtUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private AuthenticationManager authenticationManager;

    private UserDto usuarioExistente;

    @BeforeEach
    void setup() {
        // Remove usuário antigo se existir
        try {
            if (usuarioExistente != null) {
                userService.deleteUser(usuarioExistente.id());
            }
        } catch (Exception ignored) {}

        usuarioExistente = userService.createUser(
                new UserDto(null, "Usuário Auth", "authuser", "auth@email.com", "123456", null)
        );
    }

    // IT-AUTH-01: login válido
    @Test
    void deveAutenticarComCredenciaisValidas() throws Exception {
        String payload = """
                { "username": "authuser", "password": "123456" }
                """;

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtUtils.generateToken(auth)).thenReturn("tokenFake123");

        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("authuser"))
                .andExpect(jsonPath("$.token").value("tokenFake123"));
    }

    // IT-AUTH-02: senha inválida
    @Test
    void deveRejeitarCredenciaisInvalidas() throws Exception {
        String payload = """
                { "username": "authuser", "password": "senhaErrada" }
                """;

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Usuário ou senha inválidos"));

        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized());
    }

    // IT-AUTH-03: cadastro válido
    @Test
    void deveCadastrarNovoUsuario() throws Exception {
        String payload = """
                {
                  "fullName": "Novo Usuário",
                  "username": "novouser",
                  "email": "novo@email.com",
                  "password": "123456"
                }
                """;

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value("novouser"))
                .andExpect(jsonPath("$.email").value("novo@email.com"));
    }

    // IT-AUTH-04: cadastro duplicado
    @Test
    void deveRetornarConflitoQuandoUsuarioDuplicado() throws Exception {
        String payload = """
                {
                  "fullName": "Usuário Duplicado",
                  "username": "authuser",
                  "email": "auth@email.com",
                  "password": "123456"
                }
                """;

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isConflict());
    }

    @AfterEach
    void limpaUsuarioTeste() {
        try {
            userService.deleteUser(usuarioExistente.id());
        } catch (Exception ignored) {}
    }
}