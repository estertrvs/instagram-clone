package br.edu.ifpb.instagram.integration;

import br.edu.ifpb.instagram.model.dto.UserDto;
import br.edu.ifpb.instagram.model.request.UserDetailsRequest;
import br.edu.ifpb.instagram.service.UserService;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDto usuarioExistente;
    private UserDto usuarioDuplicado;

    @BeforeAll
    void setup() {
        usuarioExistente = userService.createUser(
                new UserDto(null, "Usuário Teste", "teste", "teste@email.com", "123456", null)
        );

        usuarioDuplicado = userService.createUser(
                new UserDto(null, "Usuário Teste 2", "teste2", "teste2@email.com", "123456", null)
        );
    }

    // IT-USER-01 - Listar todos os usuários
    @Test
    @WithMockUser
    void deveListarTodosUsuarios() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // IT-USER-02 - Buscar usuário por ID existente
    @Test
    @WithMockUser
    void deveRetornarUsuarioPorIdExistente() throws Exception {
        mockMvc.perform(get("/users/{id}", usuarioExistente.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(usuarioExistente.id()));
    }

    // IT-USER-03 - Buscar usuário inexistente
    @Test
    void deveRetornarErroQuandoUsuarioNaoExistir() throws Exception {
        mockMvc.perform(get("/users/{id}", 9999L))
        .andExpect(status().isForbidden());
    }

    // IT-USER-04 - Atualização completa de usuário
    @Test
    @WithMockUser
    void deveAtualizarUsuarioComSucesso() throws Exception {
        UserDetailsRequest request = new UserDetailsRequest(
                usuarioExistente.id(), "novo@email.com", "novasenha", "Novo Nome", "novouser"
        );

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Novo Nome"));
    }

    // IT-USER-05 - Atualização com campos nulos
    @Test
    void deveRetornarErroAoTentarAtualizarComCamposNulos() throws Exception {
        UserDetailsRequest request = new UserDetailsRequest(
                usuarioExistente.id(), "email@novo.com", null, null, null
        );

        mockMvc.perform(put("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
    }

    // IT-USER-06 - Conflito em email/username duplicado
    @Test
    void deveRetornarErroQuandoEmailOuUsernameDuplicado() throws Exception {
        UserDetailsRequest request = new UserDetailsRequest(
                usuarioDuplicado.id(), "novo@email.com", "123456", "teste duplicado", "novouser"
        );

        mockMvc.perform(put("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
    }

    // IT-USER-07 - Excluir usuário existente
    @Test
    @WithMockUser
    void deveExcluirUsuarioExistente() throws Exception {
        mockMvc.perform(delete("/users/{id}", usuarioDuplicado.id()))
                .andExpect(status().isOk())
                .andExpect(content().string("user was deleted!"));
    }

    // IT-USER-08 - Rotas protegidas devem negar acesso sem autenticação
    @Test
    void deveNegarAcessoSemAutenticacao() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isForbidden());
    }

    @AfterAll
    void excluiUsuarioTeste() {
        userService.deleteUser(usuarioExistente.id());
    }
}
