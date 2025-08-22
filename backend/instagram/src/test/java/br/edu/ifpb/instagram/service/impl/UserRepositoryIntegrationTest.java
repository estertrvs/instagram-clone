package br.edu.ifpb.instagram.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

import br.edu.ifpb.instagram.model.entity.UserEntity;
import br.edu.ifpb.instagram.repository.UserRepository;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void testSaveUser() {
        String email = "joao.teste@example.com";

        userRepository.findAll().stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst()
                .ifPresent(userRepository::delete);

        UserEntity user = new UserEntity();
        user.setFullName("João Teste");
        user.setUsername("joaoteste");
        user.setEncryptedPassword("123456");
        user.setEmail(email);

        UserEntity savedUser = userRepository.save(user);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getFullName()).isEqualTo("João Teste");
        assertThat(savedUser.getUsername()).isEqualTo("joaoteste");
        assertThat(savedUser.getEncryptedPassword()).isEqualTo("123456");
        assertThat(savedUser.getEmail()).isEqualTo(email);
    }

    @Test
    void testSaveUserWithNullFullNameThrowsException() {
        UserEntity user = new UserEntity();
        user.setFullName(null);
        user.setUsername("teste");
        user.setEncryptedPassword("123");
        user.setEmail("teste@example.com");

        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.save(user);
            entityManager.flush();
        });
    }

    @Test
    void testFindById() {
        UserEntity user = new UserEntity();
        user.setFullName("Maria Teste");
        user.setUsername("mariateste");
        user.setEncryptedPassword("abcdef");
        user.setEmail("maria.teste@example.com");

        UserEntity savedUser = userRepository.save(user);
        Optional<UserEntity> foundUser = userRepository.findById(savedUser.getId());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("maria.teste@example.com");
        assertThat(foundUser.get().getUsername()).isEqualTo("mariateste");
    }

    @Test
    void testFindByUsername() {
        UserEntity user = new UserEntity();
        user.setFullName("Pedro Teste");
        user.setUsername("pedroteste");
        user.setEncryptedPassword("senha123");
        user.setEmail("pedro.teste@example.com");

        userRepository.save(user);

        Optional<UserEntity> foundUser = userRepository.findByUsername("pedroteste");
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("pedro.teste@example.com");
    }

    @Test
    void testFindAll() {
        UserEntity user1 = new UserEntity();
        user1.setFullName("User One");
        user1.setUsername("userone");
        user1.setEncryptedPassword("123");
        user1.setEmail("user.one@example.com");

        UserEntity user2 = new UserEntity();
        user2.setFullName("User Two");
        user2.setUsername("usertwo");
        user2.setEncryptedPassword("456");
        user2.setEmail("user.two@example.com");

        userRepository.save(user1);
        userRepository.save(user2);

        List<UserEntity> allUsers = userRepository.findAll();
        assertThat(allUsers).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void testExistsByEmailAndUsername() {
        UserEntity user = new UserEntity();
        user.setFullName("Ana Teste");
        user.setUsername("anatest");
        user.setEncryptedPassword("pass123");
        user.setEmail("ana.teste@example.com");

        userRepository.save(user);

        assertThat(userRepository.existsByEmail("ana.teste@example.com")).isTrue();
        assertThat(userRepository.existsByUsername("anatest")).isTrue();
        assertThat(userRepository.existsByEmail("nao.existe@example.com")).isFalse();
        assertThat(userRepository.existsByUsername("naoexiste")).isFalse();
    }

    @Test
    void testUpdatePartialUser() {
        String username = "lucasteste";
        String email = "lucas.teste@example.com";

        userRepository.findByUsername(username)
                .ifPresent(userRepository::delete);
        userRepository.findAll().stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst()
                .ifPresent(userRepository::delete);

        UserEntity user = new UserEntity();
        user.setFullName("Lucas Teste");
        user.setUsername(username);
        user.setEncryptedPassword("senhaabc");
        user.setEmail(email);

        UserEntity savedUser = userRepository.save(user);

        int updatedCount = userRepository.updatePartialUser(
                "Lucas Atualizado", null, null, "novasenha", savedUser.getId());
        assertThat(updatedCount).isEqualTo(1);

        entityManager.flush();
        entityManager.clear();

        Optional<UserEntity> updatedUser = userRepository.findById(savedUser.getId());
        assertThat(updatedUser).isPresent();
        assertThat(updatedUser.get().getFullName()).isEqualTo("Lucas Atualizado");
        assertThat(updatedUser.get().getEncryptedPassword()).isEqualTo("novasenha");
        assertThat(updatedUser.get().getEmail()).isEqualTo(email);
    }

    @Test
    void testDeleteUser() {
        UserEntity user = new UserEntity();
        user.setFullName("Carlos Teste");
        user.setUsername("carlosteste");
        user.setEncryptedPassword("123abc");
        user.setEmail("carlos.teste@example.com");

        UserEntity savedUser = userRepository.save(user);

        userRepository.delete(savedUser);

        Optional<UserEntity> deletedUser = userRepository.findById(savedUser.getId());
        assertThat(deletedUser).isNotPresent();
    }
}
