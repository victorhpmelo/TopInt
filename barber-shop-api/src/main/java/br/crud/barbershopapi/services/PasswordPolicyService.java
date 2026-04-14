package br.crud.barbershopapi.services;

import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

/**
 * REQUISITO DE SEGURANÇA: Implementação da Política de Senha conforme especificações do projeto.
 * 
 * Esta classe implementa todos os requisitos de validação e segurança de senhas:
 * - Mínimo 10 caracteres
 * - Caracteres alfanuméricos, numéricos e especiais
 * - Pelo menos uma letra maiúscula
 * - Impossibilidade de reusar as 3 últimas senhas
 * - Armazenamento seguro com hash BCrypt
 * - Bloqueio de conta por 10 minutos após 5 falhas (implementado em AuthService)
 */
@Service
@Log4j2
public class PasswordPolicyService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * REQUISITO 1: Valida se a senha atende todos os critérios da política de segurança.
     * 
     * Critérios obrigatórios:
     * - Mínimo de 10 caracteres
     * - Pelo menos UMA letra maiúscula (A-Z)
     * - Pelo menos UM número (0-9)
     * - Pelo menos UM caractere especial (!@#$%^&*...)
     * - Pelo menos uma letra minúscula (a-z)
     * 
     * @param password Senha em texto plano a ser validada
     * @return true se atende todos os critérios, false caso contrário
     */
    public boolean validatePasswordPolicy(String password) {
        // Valida se null ou vazia
        if (password == null || password.isEmpty()) {
            log.warn("Tentativa de validação com senha nula ou vazia");
            return false;
        }

        // Requisito 1a: Verifica tamanho mínimo de 10 caracteres
        if (password.length() < 10) {
            log.debug("Senha rejeitada: menos de 10 caracteres. Comprimento: {}", password.length());
            return false;
        }

        // Requisito 1b: Verifica presença de pelo menos UMA letra maiúscula (A-Z)
        if (!Pattern.compile("[A-Z]").matcher(password).find()) {
            log.debug("Senha rejeitada: sem letra maiúscula");
            return false;
        }

        // Requisito 1c: Verifica presença de pelo menos UM número (0-9)
        if (!Pattern.compile("[0-9]").matcher(password).find()) {
            log.debug("Senha rejeitada: sem dígito numérico");
            return false;
        }

        // Requisito 1d: Verifica presença de pelo menos UM caractere especial
        if (!Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>?]").matcher(password).find()) {
            log.debug("Senha rejeitada: sem caractere especial");
            return false;
        }

        // Requisito 1e: Verifica presença de pelo menos uma letra minúscula (a-z)
        if (!Pattern.compile("[a-z]").matcher(password).find()) {
            log.debug("Senha rejeitada: sem letra minúscula");
            return false;
        }

        log.info("Senha validada com sucesso conforme política de segurança");
        return true;
    }

    /**
     * REQUISITO 2: Verifica se a nova senha é uma reutilização das últimas 3 senhas do usuário.
     * 
     * Detalhes:
     * - Compara a nova senha (em texto plano) com cada uma das 3 senhas anteriores (em hash)
     * - Usa BCrypt para comparação segura entre texto plano e hash
     * - Previne reutilização de senhas recentes, aumentando segurança
     * 
     * @param newPassword Nova senha em texto plano
     * @param lastThreePasswordHashes Lista com até 3 hashes das senhas anteriores
     * @return true se a senha é reutilização, false se é nova
     */
    public boolean isPasswordReused(String newPassword, List<String> lastThreePasswordHashes) {
        // Se não há histórico de senhas, não é reutilização
        if (lastThreePasswordHashes == null || lastThreePasswordHashes.isEmpty()) {
            return false;
        }

        // Itera sobre cada uma das senhas anteriores armazenadas em hash
        for (String oldPasswordHash : lastThreePasswordHashes) {
            // Usa BCrypt.matches() para comparar texto plano com hash de forma segura
            // Esta operação é custosa intencionalmente (slow-hash) para prevenir força bruta
            if (passwordEncoder.matches(newPassword, oldPasswordHash)) {
                log.warn("Tentativa de reutilizar senha anterior detectada");
                return true; // Senha está sendo reutilizada - REJEITAR
            }
        }
        
        // A senha não corresponde a nenhuma das 3 anteriores
        return false;
    }

    /**
     * REQUISITO 3: Codifica a senha usando BCrypt antes de salvar no banco de dados.
     * 
     * Detalhes técnicos:
     * - BCrypt é um algoritmo de hashing com "salt" automático
     * - É "slow-hash" propositalmente: ~1 segundo por hash para prevenir força bruta
     * - A mesma senha gera hashes diferentes a cada execução (salt randômico)
     * - É IMPOSSÍVEL recuperar a senha original do hash (função unidirecional)
     * 
     * @param password Senha em texto plano
     * @return Hash BCrypt da senha, pronto para salvar no banco
     */
    public String encodePassword(String password) {
        // Gera hash BCrypt com salt aleatório
        // Resultado: $2a$10$... (60 caracteres)
        String encoded = passwordEncoder.encode(password);
        log.info("Senha codificada com sucesso usando BCrypt");
        return encoded;
    }

    /**
     * REQUISITO 4: Verifica se a conta está bloqueada por limite de tentativas falhadas.
     * 
     * Regra de negócio:
     * - Após 5 tentativas de login falhadas, a conta é bloqueada por 10 minutos
     * - Este método verifica se o bloqueio ainda está em vigor
     * - O timestamps de desbloqueio é armazenado em AppUserModel.lockedUntil
     * 
     * @param failedAttempts Número de tentativas falhadas
     * @param lastFailedAttempt Timestamp do ÚLTIMO acesso negado (java.time.Instant)
     * @return true se a conta ainda está bloqueada
     */
    public boolean isAccountLocked(int failedAttempts, java.time.Instant lastFailedAttempt) {
        // Se menos de 5 tentativas, conta não está bloqueada
        if (failedAttempts < 5) {
            return false;
        }

        // Se 5 ou mais falhas, verifica se passaram 10 minutos desde o bloqueio
        if (lastFailedAttempt == null) {
            return false;
        }

        // Calcula o tempo de desbloqueio (bloqueio + 10 minutos)
        java.time.Instant unlockTime = lastFailedAttempt.plus(java.time.Duration.ofMinutes(10));
        
        // Se agora for ANTES do tempo de desbloqueio, conta ainda está bloqueada
        boolean isLocked = java.time.Instant.now().isBefore(unlockTime);
        
        if (isLocked) {
            log.warn("Tentativa de acesso a conta bloqueada. Desbloqueio em: {}", unlockTime);
        }
        
        return isLocked;
    }

    /**
     * Método auxiliar: Incrementa o contador de tentativas falhadas.
     * 
     * @param currentAttempts Número atual de tentativas
     * @return Novo número de tentativas (currentAttempts + 1)
     */
    public int incrementFailedAttempts(int currentAttempts) {
        return currentAttempts + 1;
    }

    /**
     * Método auxiliar: Reseta o contador de tentativas falhadas após login bem-sucedido.
     * 
     * @param currentAttempts Número atual de tentativas (ignorado)
     * @return 0 (contador resetado)
     */
    public int resetFailedAttempts(int currentAttempts) {
        return 0;
    }

    /**
     * Valida força de uma senha utilizando OWASP guidelines.
     * Retorna uma pontuação indicando quanto a senha excede os requisitos mínimos.
     * 
     * @param password Senha a ser analisada
     * @return Pontuação de 0-100 indicando força (0 = não atende requisitos)
     */
    public int getPasswordStrength(String password) {
        int strength = 0;

        if (password == null || !validatePasswordPolicy(password)) {
            return 0; // Não atende requisitos mínimos
        }

        strength += 40; // Pontos básicos por atender requisitos

        // Bonus por comprimento
        if (password.length() >= 12) strength += 10;
        if (password.length() >= 14) strength += 10;
        if (password.length() >= 16) strength += 10;

        // Verificações adicionais
        long digitCount = password.chars().filter(Character::isDigit).count();
        if (digitCount >= 2) strength += 10;

        long specialCount = password.chars()
            .filter(c -> !Character.isLetterOrDigit(c))
            .count();
        if (specialCount >= 2) strength += 10;

        return Math.min(strength, 100);
    }
}
