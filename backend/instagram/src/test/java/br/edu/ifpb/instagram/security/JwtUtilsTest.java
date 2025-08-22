package br.edu.ifpb.instagram.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

// Importações necessárias para o teste
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

// Habilita as funcionalidades do Mockito, como a criação de 'mocks'.
@ExtendWith(MockitoExtension.class)
public class JwtUtilsTest {
    
    // Cria um objeto "dublê" ou "simulado" da interface Authentication.
    // Isso me permite testar sem precisar de um sistema de login real.
    @Mock
    private Authentication authentication;

    // Esta é a instância da classe que realmente vou testar.
    private JwtUtils jwtUtils;

    // 3: Este método é executado automaticamente ANTES de CADA teste.
    // Isso garante que cada teste comece com uma instância "limpa" de JwtUtils.
    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
    }

    // Atende ao caso de teste: UT-JWT-01
    // A anotação @DisplayName dá um nome legível para o teste, que aparecerá nos relatórios.
    @Test
    @DisplayName("Deve gerar um token JWT válido")
    void deveGerarUmTokenValido() {
        // --- PREPARAÇÃO (Arrange) ---
        // "Ensina" o dublê a se comportar: quando o método getName() for chamado, ele deve retornar o nome de usuário "testuser".
        when(authentication.getName()).thenReturn("testuser");

        // --- AÇÃO (Act) ---
        // Executa o método que quero testar, passando o dublê como parâmetro.
        String token = jwtUtils.generateToken(authentication);

        // --- VERIFICAÇÃO (Assert) ---
        // Verifica se o resultado é o que se espera.
        // O token não pode ser nulo...
        assertNotNull(token);
        // ...e não pode estar em branco.
        assertFalse(token.isBlank());
        // Imprime o token no console para visualização durante o teste.
        System.out.println("Token Gerado: " + token);
    }

    // Atende ao caso de teste: UT-JWT-02
    @Test
    @DisplayName("Deve validar um token gerado corretamente")
    void deveValidarUmTokenCorreto() {
        // --- PREPARAÇÃO ---
        when(authentication.getName()).thenReturn("testuser");
        // Primeiro, precisa de um token válido para testar a validação.
        String token = jwtUtils.generateToken(authentication);

        // --- AÇÃO ---
        // Chamo o método de validação com o token que acabo de criar.
        boolean isValid = jwtUtils.validateToken(token);

        // --- VERIFICAÇÃO ---
        // Esperamos que o método retorne 'true', confirmando que o token é válido.
        assertTrue(isValid);
    }

    // Atende ao caso de teste: UT-JWT-03
    @Test
    @DisplayName("Deve retornar o username correto a partir de um token válido")
    void deveRetornarUsernameDeUmTokenValido() {
        // --- PREPARAÇÃO ---
        when(authentication.getName()).thenReturn("testuser");
        String token = jwtUtils.generateToken(authentication);

        // --- AÇÃO ---
        // Chamo o método para extrair o nome de usuário do token.
        String username = jwtUtils.getUsernameFromToken(token);

        // --- VERIFICAÇÃO ---
        // Verifica se o nome de usuário extraído é exatamente "testuser".
        assertEquals("testuser", username);
    }

    // Atende ao caso de teste: UT-JWT-04
    @Test
    @DisplayName("NÃO deve validar um token com assinatura inválida (falsificado)")
    void naoDeveValidarUmTokenComAssinaturaInvalida() {
        // --- PREPARAÇÃO ---
        when(authentication.getName()).thenReturn("testuser");
        String token = jwtUtils.generateToken(authentication);
        
        // Aqui, simula um ataque: pega o token válido e altera o último caractere para corromper a assinatura digital.
        String tamperedToken = token.substring(0, token.length() - 1) + "X";

        // --- AÇÃO ---
        // Tenta validar o token que foi "falsificado".
        boolean isValid = jwtUtils.validateToken(tamperedToken);

        // --- VERIFICAÇÃO ---
        // O resultado esperado é 'false', pois o sistema deve detectar a falsificação.
        assertFalse(isValid);
    }
    
    // Atende ao caso de teste: UT-JWT-05
    @Test
    @DisplayName("NÃO deve validar um token expirado")
    void naoDeveValidarUmTokenExpirado() {
        // --- PREPARAÇÃO ---
        // Para este teste, não usa o mock. Crio um token manualmente para ter controle total sobre suas datas de criação e expiração.
        final String SECRET_KEY = "umaChaveMuitoSeguraDePeloMenos64CaracteresParaHS512JwtAlgoritmo!";
        final byte[] jwtSecret = Base64.getEncoder().encode(SECRET_KEY.getBytes());

        String expiredToken = Jwts.builder()
                .setSubject("testuser")
                // Criado 20 segundos no passado.
                .setIssuedAt(new Date(System.currentTimeMillis() - 20000)) 
                // Expirado 10 segundos no passado.
                .setExpiration(new Date(System.currentTimeMillis() - 10000)) 
                .signWith(Keys.hmacShaKeyFor(jwtSecret), SignatureAlgorithm.HS512)
                .compact();

        // --- AÇÃO ---
        // Tentamos validar este token que já "nasceu" expirado.
        boolean isValid = jwtUtils.validateToken(expiredToken);
        
        // --- VERIFICAÇÃO ---
        // O sistema deve retornar 'false', rejeitando o token por estar fora da validade.
        assertFalse(isValid);
    }

    // Atende ao caso de teste: UT-JWT-06
    @Test
    @DisplayName("NÃO deve validar um token com formato inválido")
    void naoDeveValidarUmTokenComFormatoInvalido() {
        // --- PREPARAÇÃO ---
        // Uma string que não tem o formato de um JWT (header.payload.signature)
        String invalidFormatToken = "abc.def.ghi";

        // --- AÇÃO ---
        // Tenta validar a string malformada. O bloco try-catch dentro de validateToken() deve capturar a exceção e retornar false.
        boolean isValid = jwtUtils.validateToken(invalidFormatToken);

        // --- VERIFICAÇÃO ---
        // O sistema deve retornar 'false'.
        assertFalse(isValid);
    }
}