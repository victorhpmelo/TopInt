package br.crud.barbershopapi.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TESTES UNITÁRIOS: PasswordPolicyService
 * 
 * Valida todos os requisitos de política de senha:
 * - Mínimo 10 caracteres
 * - Caracteres alfanuméricos, numéricos e especiais
 * - Pelo menos 1 letra maiúscula
 * - Histórico de últimas 3 senhas
 * - Bloqueio por 10 minutos
 * - Hash BCrypt
 */
@DisplayName("Testes de Política de Senha")
class PasswordPolicyServiceTest {

    private PasswordPolicyService passwordPolicyService;
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordPolicyService = new PasswordPolicyService();
        passwordEncoder = new BCryptPasswordEncoder();
    }

    // ========== TESTES DE VALIDAÇÃO DE POLÍTICA ==========

    @Test
    @DisplayName("DEVE validar senha que atende TODOS os requisitos")
    void testValidPasswordMeetsAllRequirements() {
        // Arrange
        String validPassword = "SenhaForte@123456";
        
        // Act & Assert
        assertTrue(passwordPolicyService.validatePasswordPolicy(validPassword),
            "Senha com 10+ chars, maiúscula, números e especiais deve ser válida");
    }

    @Test
    @DisplayName("DEVE rejeitar senha com MENOS de 10 caracteres")
    void testPasswordTooShort() {
        // Arrange
        String shortPassword = "Pass@1";  // Apenas 6 caracteres
        
        // Act & Assert
        assertFalse(passwordPolicyService.validatePasswordPolicy(shortPassword),
            "Senha com menos de 10 caracteres deve ser rejeitada");
    }

    @Test
    @DisplayName("DEVE rejeitar senha SEM letra maiúscula")
    void testPasswordWithoutUppercase() {
        // Arrange
        String noUppercase = "senhaforte@1234";  // Sem maiúscula
        
        // Act & Assert
        assertFalse(passwordPolicyService.validatePasswordPolicy(noUppercase),
            "Senha sem letra maiúscula deve ser rejeitada");
    }

    @Test
    @DisplayName("DEVE rejeitar senha SEM número")
    void testPasswordWithoutDigit() {
        // Arrange
        String noDigit = "SenhaForte@abcd";  // Sem dígito
        
        // Act & Assert
        assertFalse(passwordPolicyService.validatePasswordPolicy(noDigit),
            "Senha sem número deve ser rejeitada");
    }

    @Test
    @DisplayName("DEVE rejeitar senha SEM caractere especial")
    void testPasswordWithoutSpecialChar() {
        // Arrange
        String noSpecial = "SenhaForte123456";  // Sem caractere especial
        
        // Act & Assert
        assertFalse(passwordPolicyService.validatePasswordPolicy(noSpecial),
            "Senha sem caractere especial deve ser rejeitada");
    }

    @Test
    @DisplayName("DEVE rejeitar senha SEM letra minúscula")
    void testPasswordWithoutLowercase() {
        // Arrange
        String noLowercase = "SENHAFORTE@123";  // Sem letra minúscula
        
        // Act & Assert
        assertFalse(passwordPolicyService.validatePasswordPolicy(noLowercase),
            "Senha sem letra minúscula deve ser rejeitada");
    }

    @Test
    @DisplayName("DEVE rejeitar senha null")
    void testPasswordNull() {
        // Act & Assert
        assertFalse(passwordPolicyService.validatePasswordPolicy(null),
            "Senha null deve ser rejeitada");
    }

    @Test
    @DisplayName("DEVE rejeitar senha vazia")
    void testPasswordEmpty() {
        // Act & Assert
        assertFalse(passwordPolicyService.validatePasswordPolicy(""),
            "Senha vazia deve ser rejeitada");
    }

    // ========== TESTES DE REUTILIZAÇÃO DE SENHA ==========

    @Test
    @DisplayName("DEVE detectar quando senha foi REUTILIZADA (está no histórico)")
    void testDetectPasswordReuse() {
        // Arrange
        String newPassword = "SenhaForte@123456";
        String oldPasswordHash = passwordEncoder.encode("SenhaForte@123456");
        List<String> lastThreePasswords = List.of(oldPasswordHash);
        
        // Act
        boolean isReused = passwordPolicyService.isPasswordReused(newPassword, lastThreePasswords);
        
        // Assert
        assertTrue(isReused, "Senha já usada deve ser detectada como reutilização");
    }

    @Test
    @DisplayName("DEVE permitir senha NOVA (não está no histórico)")
    void testAllowNewPasswordNotInHistory() {
        // Arrange
        String newPassword = "NovaSenha@654321";
        String oldPasswordHash = passwordEncoder.encode("SenhaForte@123456");
        List<String> lastThreePasswords = List.of(oldPasswordHash);
        
        // Act
        boolean isReused = passwordPolicyService.isPasswordReused(newPassword, lastThreePasswords);
        
        // Assert
        assertFalse(isReused, "Senha nova não deve ser detectada como reutilização");
    }

    @Test
    @DisplayName("DEVE checar contra as 3 últimas senhas")
    void testCheckAgainstThreePasswords() {
        // Arrange
        String newPassword = "SenhaAnterior2@123";
        List<String> lastThreePasswords = List.of(
            passwordEncoder.encode("SenhaAnterior1@123"),
            passwordEncoder.encode("SenhaAnterior2@123"),  // Encontrada na 2ª posição
            passwordEncoder.encode("SenhaAnterior3@123")
        );
        
        // Act
        boolean isReused = passwordPolicyService.isPasswordReused(newPassword, lastThreePasswords);
        
        // Assert
        assertTrue(isReused, "Deve detectar reutilização em qualquer posição do histórico");
    }

    @Test
    @DisplayName("DEVE lidar com histórico VAZIO")
    void testEmptyPasswordHistory() {
        // Arrange
        String newPassword = "SenhaForte@123456";
        List<String> emptyHistory = List.of();
        
        // Act
        boolean isReused = passwordPolicyService.isPasswordReused(newPassword, emptyHistory);
        
        // Assert
        assertFalse(isReused, "Senha com histórico vazio não deve ser considerada reutilização");
    }

    // ========== TESTES DE ENCODING BCrypt ==========

    @Test
    @DisplayName("DEVE codificar senha com BCrypt")
    void testBCryptEncoding() {
        // Arrange
        String password = "SenhaForte@123456";
        
        // Act
        String encoded = passwordPolicyService.encodePassword(password);
        
        // Assert
        assertNotNull(encoded, "Senha codificada não deve ser null");
        assertNotEquals(password, encoded, "Senha não deve ser armazenada em texto plano");
        assertTrue(encoded.startsWith("$2a$"), "BCrypt começa com $2a$");
        assertEquals(60, encoded.length(), "Hash BCrypt tem 60 caracteres");
    }

    @Test
    @DisplayName("DEVE gerar hashes DIFERENTES para mesma senha (salt aleatório)")
    void testBCryptDifferentHashesSameSalt() {
        // Arrange
        String password = "SenhaForte@123456";
        
        // Act
        String hash1 = passwordPolicyService.encodePassword(password);
        String hash2 = passwordPolicyService.encodePassword(password);
        
        // Assert
        assertNotEquals(hash1, hash2, "Cada hash deve ter salt aleatório diferente");
    }

    @Test
    @DisplayName("DEVE verificar se hash corresponde à senha original")
    void testBCryptVerification() {
        // Arrange
        String password = "SenhaForte@123456";
        String encoded = passwordPolicyService.encodePassword(password);
        
        // Act
        boolean matches = new BCryptPasswordEncoder().matches(password, encoded);
        
        // Assert
        assertTrue(matches, "BCrypt deve verificar que senha corresponde ao hash");
    }

    // ========== TESTES DE BLOQUEIO DE CONTA ==========

    @Test
    @DisplayName("DEVE permitir login quando NÃO há tentativas falhadas")
    void testAccountNotLockedWithNoFailures() {
        // Arrange
        int failedAttempts = 0;
        Instant lastFailedAttempt = Instant.now();
        
        // Act
        boolean isLocked = passwordPolicyService.isAccountLocked(failedAttempts, lastFailedAttempt);
        
        // Assert
        assertFalse(isLocked, "Conta sem falhas não deve estar bloqueada");
    }

    @Test
    @DisplayName("DEVE permitir login com MENOS de 5 tentativas falhadas")
    void testAccountNotLockedWithFewerThanFiveFailures() {
        // Arrange
        int failedAttempts = 3;  // Menos de 5
        Instant lastFailedAttempt = Instant.now();
        
        // Act
        boolean isLocked = passwordPolicyService.isAccountLocked(failedAttempts, lastFailedAttempt);
        
        // Assert
        assertFalse(isLocked, "Conta com 3 falhas não deve estar bloqueada");
    }

    @Test
    @DisplayName("DEVE bloquear conta com 5 falhas E timestamp recente")
    void testAccountLockedWithFiveFailuresRecent() {
        // Arrange
        int failedAttempts = 5;
        Instant lastFailedAttempt = Instant.now().minusSeconds(30);  // Há 30 segundos
        
        // Act
        boolean isLocked = passwordPolicyService.isAccountLocked(failedAttempts, lastFailedAttempt);
        
        // Assert
        assertTrue(isLocked, "Conta com 5 falhas recentes deve estar bloqueada");
    }

    @Test
    @DisplayName("DEVE desbloquear conta após 10 minutos")
    void testAccountUnlockedAfterTenMinutes() {
        // Arrange
        int failedAttempts = 5;
        Instant lastFailedAttempt = Instant.now().minusSeconds(11 * 60);  // Há 11 minutos
        
        // Act
        boolean isLocked = passwordPolicyService.isAccountLocked(failedAttempts, lastFailedAttempt);
        
        // Assert
        assertFalse(isLocked, "Conta deve desbloquear após 10 minutos");
    }

    // ========== TESTES DE UTILIDADES ==========

    @Test
    @DisplayName("DEVE incrementar contador de tentativas falhadas")
    void testIncrementFailedAttempts() {
        // Arrange
        int currentAttempts = 3;
        
        // Act
        int incremented = passwordPolicyService.incrementFailedAttempts(currentAttempts);
        
        // Assert
        assertEquals(4, incremented, "Deve incrementar de 3 para 4");
    }

    @Test
    @DisplayName("DEVE resetar contador de tentativas falhadas para 0")
    void testResetFailedAttempts() {
        // Arrange
        int currentAttempts = 5;
        
        // Act
        int reset = passwordPolicyService.resetFailedAttempts(currentAttempts);
        
        // Assert
        assertEquals(0, reset, "Deve resetar para 0");
    }

    @Test
    @DisplayName("DEVE calcular força da senha")
    void testPasswordStrength() {
        // Arrange - Senha forte
        String strongPassword = "MuitoForte@123456789";
        
        // Act
        int strength = passwordPolicyService.getPasswordStrength(strongPassword);
        
        // Assert
        assertTrue(strength > 50, "Senha forte deve ter pontuação > 50");
    }

    @Test
    @DisplayName("DEVE retornar 0 para senha inválida")
    void testPasswordStrengthInvalid() {
        // Arrange - Senha fraca
        String weakPassword = "123";
        
        // Act
        int strength = passwordPolicyService.getPasswordStrength(weakPassword);
        
        // Assert
        assertEquals(0, strength, "Senha inválida deve ter pontuação 0");
    }
}
