package br.edu.ifpb.instagram.service.impl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.edu.ifpb.instagram.exception.FieldAlreadyExistsException;
import br.edu.ifpb.instagram.model.dto.UserDto;
import br.edu.ifpb.instagram.model.entity.UserEntity;
import br.edu.ifpb.instagram.repository.UserRepository;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UserEntity userEntity;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("testuser");
        userEntity.setEmail("test@example.com");
        userEntity.setFullName("Test User");
        userEntity.setEncryptedPassword("encrypted");

        userDto = new UserDto(1L, "Test User", "testuser", "test@example.com", "password", null);
    }

    /**
     * UT-US-01: Criar usuário com email/username únicos
     */
    @Test
    void createUser_shouldCreateUser_whenEmailAndUsernameAreUnique() {
        when(userRepository.existsByEmail(userDto.email())).thenReturn(false);
        when(userRepository.existsByUsername(userDto.username())).thenReturn(false);
        when(passwordEncoder.encode(userDto.password())).thenReturn("encrypted");
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        UserDto created = userService.createUser(userDto);

        assertNotNull(created);
        assertEquals(userDto.username(), created.username());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    /**
     * UT-US-02: Rejeitar cadastro quando email já existe
     */
    @Test
    void createUser_shouldThrow_whenEmailExists() {
        when(userRepository.existsByEmail(userDto.email())).thenReturn(true);

        FieldAlreadyExistsException ex = assertThrows(FieldAlreadyExistsException.class,
                () -> userService.createUser(userDto));

        assertEquals("E-email already in use.", ex.getMessage());
        verify(userRepository, never()).save(any());
    }


    /**
     * UT-US-03: Rejeitar cadastro quando username já existe
     */
    @Test
    void createUser_shouldThrow_whenUsernameExists() {
        when(userRepository.existsByEmail(userDto.email())).thenReturn(false);
        when(userRepository.existsByUsername(userDto.username())).thenReturn(true);

        FieldAlreadyExistsException ex = assertThrows(FieldAlreadyExistsException.class,
                () -> userService.createUser(userDto));

        assertEquals("Username already in use.", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    /**
     * UT-US-04: Retornar usuário existente
     */
    @Test
    void findById_shouldReturnUser_whenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));

        UserDto found = userService.findById(1L);

        assertNotNull(found);
        assertEquals("testuser", found.username());
    }

    /**
     * UT-US-05: Lançar exceção quando usuário não existe
     */
    @Test
    void findById_shouldThrow_whenUserDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.findById(1L));

        assertTrue(ex.getMessage().contains("User not found with id"));
    }

    /**
     * UT-US-06: Listar usuários
     */
    @Test
    void findAll_shouldReturnListOfUsers() {
        when(userRepository.findAll()).thenReturn(List.of(userEntity));

        List<UserDto> users = userService.findAll();

        assertFalse(users.isEmpty());
        assertEquals(1, users.size());
        assertEquals("testuser", users.get(0).username());
    }

    /**
     * UT-US-07: Atualizar usuário com todos os campos
     */
    @Test
    void updateUser_shouldUpdateAllFields() {
        when(userRepository.findById(userDto.id())).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.encode(userDto.password())).thenReturn("encrypted");
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        UserDto updated = userService.updateUser(userDto);

        assertEquals(userDto.username(), updated.username());
        verify(userRepository, times(1)).save(any());
    }

    /**
     * UT-US-08: Atualização parcial preserva campos nulos
     */
    @Test
    void updateUser_shouldPreserveNullPassword() {
        UserDto partial = new UserDto(1L, "Test User", "testuser", "test@example.com", null, null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        UserDto updated = userService.updateUser(partial);

        assertEquals("testuser", updated.username());
    }

    /**
     * UT-US-09: Deletar usuário por id
     */
    @Test
    void deleteUser_shouldDelete_whenUserExists() {
        when(userRepository.existsById(1L)).thenReturn(true);

        assertDoesNotThrow(() -> userService.deleteUser(1L));
        verify(userRepository, times(1)).deleteById(1L);
    }

    /**
     * UT-US-10: Deletar usuário lança exceção quando não existe
     */
    @Test
    void deleteUser_shouldThrow_whenUserDoesNotExist() {
        when(userRepository.existsById(1L)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.deleteUser(1L));
        assertTrue(ex.getMessage().contains("User not found with id"));
    }
        /**
     * UT-US-11: Atualização lança exceção quando UserDto.id é null
     * Objetivo: Garantir que o método updateUser valide a entrada e não permita atualização sem ID
     * Pré-condições: Nenhuma
     * Passos: Chamar updateUser com UserDto.id = null
     * Resultado esperado: Lança IllegalArgumentException
     */
    @Test
    void updateUser_shouldThrow_whenUserIdIsNull() {
        UserDto invalidUser = new UserDto(null, "Test User", "testuser", "test@example.com", "password", null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.updateUser(invalidUser));

        assertEquals("UserDto or UserDto.id must not be null", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    /**
     * UT-US-12: Criar usuário codifica senha corretamente
     * Objetivo: Garantir que a senha do usuário seja codificada antes de salvar
     * Pré-condições: userRepository.existsByEmail e existsByUsername retornam false
     * Passos: Chamar createUser com UserDto contendo senha
     * Resultado esperado: PasswordEncoder.encode é chamado e a senha armazenada está codificada
     */
    @Test
    void createUser_shouldEncodePassword() {
        when(userRepository.existsByEmail(userDto.email())).thenReturn(false);
        when(userRepository.existsByUsername(userDto.username())).thenReturn(false);
        when(passwordEncoder.encode(userDto.password())).thenReturn("encrypted");
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        UserDto created = userService.createUser(userDto);

        assertNotNull(created);
        verify(passwordEncoder, times(1)).encode(userDto.password());
    }

    /**
     * UT-US-13: Atualização não altera senha se password for null
     * Objetivo: Garantir que a senha não seja sobrescrita quando o UserDto.password for null
     * Pré-condições: userRepository.findById retorna usuário existente
     * Passos: Chamar updateUser com UserDto.password = null
     * Resultado esperado: userRepository.save é chamado, senha original permanece
     */
    @Test
    void updateUser_shouldNotChangePassword_whenPasswordIsNull() {
        UserDto partial = new UserDto(1L, "Test User", "testuser", "test@example.com", null, null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        UserDto updated = userService.updateUser(partial);

        assertNotNull(updated);
        // Verifica que PasswordEncoder.encode nunca foi chamado
        verify(passwordEncoder, never()).encode(any());
    }

}
