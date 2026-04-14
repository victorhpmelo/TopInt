package br.crud.barbershopapi.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import br.crud.barbershopapi.audit.AuditLogService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * REQUISITO DE SEGURANÇA: Serviço especializado em logging de eventos da aplicação.
 * 
 * Este serviço gerencia o registro de todos os eventos de auditoria e segurança:
 * - Cadastro, alteração e exclusão de usuários
 * - Autenticação (sucessos e falhas)
 * - Eventos de negócio (agendamentos, alterações, etc.)
 * 
 * Cada evento é registrado com timestamp, usuário, tipo de evento e descrição detalhada.
 * Os logs são armazenados em arquivo de auditoria para rastreabilidade completa.
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class LogService {

    private final AuditLogService auditLogService;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ========== REQUISITO 1: LOGS DE GESTÃO DE USUÁRIOS ==========

    /**
     * REQUISITO: Registra o cadastro de um novo usuário no sistema.
     * 
     * Informações registradas:
     * - Timestamp completo da operação
     * - Nome/login do usuário criado
     * - Descrição clara do evento
     * 
     * Exemplo de log gerado:
     * "2026-04-07 14:30:45 | REGISTRO | joao.silva | Novo usuário cadastrado no sistema."
     * 
     * @param username Nome do usuário criado
     */
    public void logUserRegistration(String username) {
        String timestamp = LocalDateTime.now().format(formatter);
        String description = String.format(
            "Novo usuário cadastrado no sistema. Timestamp: %s", timestamp
        );
        auditLogService.log("REGISTRO_USUARIO", username, description);
        log.info("Usuário registrado: {}", username);
    }

    /**
     * REQUISITO: Registra quando dados ou senha de um usuário são alterados.
     * 
     * Informações registradas:
     * - Timestamp completo da operação
     * - Nome do usuário afetado
     * - Qual campo foi modificado (dados pessoais ou senha)
     * - Descrição clara do evento
     * 
     * Exemplo de log gerado:
     * "2026-04-07 14:30:45 | ALTERACAO_USUARIO | joao.silva | Dados de usuário alterados: email/senha/telefone"
     * 
     * @param username Nome do usuário que foi alterado
     * @param fieldModified Campo que foi modificado (ex: "email", "senha", "telefone")
     */
    public void logUserDataModification(String username, String fieldModified) {
        String timestamp = LocalDateTime.now().format(formatter);
        String description = String.format(
            "Dados de usuário alterados. Campo: %s. Timestamp: %s", 
            fieldModified, timestamp
        );
        auditLogService.log("ALTERACAO_USUARIO", username, description);
        log.info("Usuário alterado: {} - Campo: {}", username, fieldModified);
    }

    /**
     * REQUISITO: Registra quando um usuário é deletado/desativado do sistema.
     * 
     * Informações registradas:
     * - Timestamp completo da exclusão
     * - Nome do usuário removido
     * - Descrição clara do evento
     * 
     * Exemplo de log gerado:
     * "2026-04-07 14:30:45 | EXCLUSAO_USUARIO | joao.silva | Usuário removido do sistema."
     * 
     * @param username Nome do usuário excluído
     */
    public void logUserDeletion(String username) {
        String timestamp = LocalDateTime.now().format(formatter);
        String description = String.format(
            "Usuário removido do sistema. Timestamp: %s", timestamp
        );
        auditLogService.log("EXCLUSAO_USUARIO", username, description);
        log.warn("Usuário deletado: {}", username);
    }

    // ========== REQUISITO 2: LOGS DE AUTENTICAÇÃO ==========

    /**
     * REQUISITO: Registra um erro/falha na autenticação de um usuário.
     * 
     * Casos de falha:
     * - Credenciais inválidas (senha incorreta)
     * - Usuário não encontrado
     * - Usuário inativo
     * 
     * Informações registradas:
     * - Timestamp completo da tentativa
     * - Nome do usuário
     * - Descrição do tipo de erro
     * 
     * Exemplo de log gerado:
     * "2026-04-07 14:30:45 | ERRO_AUTENTICACAO | joao.silva | Credenciais inválidas."
     * 
     * @param username Nome do usuário que tentou autenticar
     */
    public void logAuthenticationError(String username) {
        String timestamp = LocalDateTime.now().format(formatter);
        String description = String.format(
            "Falha na autenticação. Credenciais inválidas. Timestamp: %s", timestamp
        );
        auditLogService.log("ERRO_AUTENTICACAO", username, description);
        log.warn("Falha de autenticação: {}", username);
    }

    /**
     * REQUISITO: Registra quando um usuário atinge 5 falhas de autenticação consecutivas
     * NO MESMO DIA, resultando em bloqueio de conta.
     * 
     * Detalhes importantes:
     * - Só registra QUANDO atinge a 5ª falha (não a cada falha)
     * - Registra que a conta foi bloqueada por 10 minutos
     * - Serve como ALERTA CRÍTICO de segurança
     * 
     * Informações registradas:
     * - Timestamp completo
     * - Nome do usuário afetado
     * - Número de tentativas (sempre 5)
     * - Descrição de bloqueio
     * 
     * Exemplo de log gerado:
     * "2026-04-07 14:30:45 | ALERTA_BLOQUEIO_CONTA | joao.silva | 5 falhas detectadas. Conta bloqueada por 10 minutos."
     * 
     * @param username Nome do usuário cuja conta foi bloqueada
     * @param failureCount Número de falhas (tipicamente 5)
     */
    public void logMultipleAuthenticationFailures(String username, int failureCount) {
        String timestamp = LocalDateTime.now().format(formatter);
        String description = String.format(
            "%d falhas de autenticação no mesmo dia. Conta bloqueada por 10 minutos. Timestamp: %s", 
            failureCount, timestamp
        );
        auditLogService.log("ALERTA_BLOQUEIO_CONTA", username, description);
        log.error("Alerta de segurança: {} com {} tentativas falhadas", username, failureCount);
    }

    // ========== REQUISITO 3: LOGS DE EVENTOS DE NEGÓCIO ==========
    // Abaixo estão registrados PELO MENOS 5 exemplos de eventos da aplicação

    /**
     * REQUISITO 3.1: Registra quando uma nova localidade de venda é adicionada ao sistema.
     * (Exemplo de evento de negócio #1)
     * 
     * @param locationName Nome da localidade (ex: "Filial Boa Viagem")
     * @param address Endereço completo da localidade
     */
    public void logNewLocationAdded(String locationName, String address) {
        String timestamp = LocalDateTime.now().format(formatter);
        String description = String.format(
            "Nova localidade de venda adicionada. Local: %s. Endereço: %s. Timestamp: %s", 
            locationName, address, timestamp
        );
        auditLogService.log("EVENTO_NOVA_LOCALIDADE", "SISTEMA", description);
        log.info("Nova localidade criada: {}", locationName);
    }

    /**
     * REQUISITO 3.2: Registra quando um endereço de entrega é alterado.
     * (Exemplo de evento de negócio #2)
     * 
     * @param clientName Nome do cliente afetado
     * @param oldAddress Endereço anterior
     * @param newAddress Novo endereço
     */
    public void logDeliveryAddressModification(String clientName, String oldAddress, String newAddress) {
        String timestamp = LocalDateTime.now().format(formatter);
        String description = String.format(
            "Endereço de entrega alterado. Cliente: %s. De: %s. Para: %s. Timestamp: %s", 
            clientName, oldAddress, newAddress, timestamp
        );
        auditLogService.log("EVENTO_ALTERACAO_ENDERECO", clientName, description);
        log.info("Endereço alterado para cliente: {}", clientName);
    }

    /**
     * REQUISITO 3.3: Registra quando um novo agendamento de serviço é criado.
     * (Exemplo de evento de negócio #3)
     * 
     * @param clientName Nome do cliente
     * @param serviceName Nome/tipo do serviço (ex: "Corte + Barba")
     * @param scheduledDateTime Data e hora agendada
     */
    public void logServiceScheduling(String clientName, String serviceName, String scheduledDateTime) {
        String timestamp = LocalDateTime.now().format(formatter);
        String description = String.format(
            "Novo agendamento criado. Cliente: %s. Serviço: %s. Agendado para: %s. Timestamp: %s", 
            clientName, serviceName, scheduledDateTime, timestamp
        );
        auditLogService.log("EVENTO_AGENDAMENTO_CRIADO", clientName, description);
        log.info("Agendamento criado para {}: {}", clientName, serviceName);
    }

    /**
     * REQUISITO 3.4: Registra quando um agendamento é cancelado.
     * (Exemplo de evento de negócio #4)
     * 
     * @param clientName Nome do cliente
     * @param schedulingId ID/código do agendamento cancelado
     */
    public void logSchedulingCancellation(String clientName, String schedulingId) {
        String timestamp = LocalDateTime.now().format(formatter);
        String description = String.format(
            "Agendamento cancelado. Cliente: %s. ID: %s. Timestamp: %s", 
            clientName, schedulingId, timestamp
        );
        auditLogService.log("EVENTO_AGENDAMENTO_CANCELADO", clientName, description);
        log.info("Agendamento cancelado para {}: {}", clientName, schedulingId);
    }

    /**
     * REQUISITO 3.5: Registra quando as tarifas/preços dos serviços são alterados.
     * (Exemplo de evento de negócio #5)
     * 
     * Informações críticas para auditoria financeira:
     * - Serviço afetado
     * - Preço anterior
     * - Novo preço
     * - Timestamp da alteração
     * 
     * @param serviceName Nome do serviço
     * @param oldPrice Preço anterior
     * @param newPrice Novo preço
     */
    public void logPriceModification(String serviceName, double oldPrice, double newPrice) {
        String timestamp = LocalDateTime.now().format(formatter);
        String description = String.format(
            "Alteração de tarifa. Serviço: %s. Preço anterior: R$ %.2f. Novo preço: R$ %.2f. Timestamp: %s", 
            serviceName, oldPrice, newPrice, timestamp
        );
        auditLogService.log("EVENTO_ALTERACAO_TARIFA", "SISTEMA", description);
        log.info("Tarifa alterada para {}: R$ {:.2f} -> R$ {:.2f}", serviceName, oldPrice, newPrice);
    }

    /**
     * REQUISITO ADICIONAL: Método genérico para registrar eventos personalizados.
     * Pode ser usado para outros eventos de negócio conforme necessário.
     * 
     * @param eventType Tipo/categoria do evento
     * @param username Usuário envolvido (ou "SISTEMA" se automático)
     * @param description Descrição detalhada do evento
     */
    public void logCustomEvent(String eventType, String username, String description) {
        String timestamp = LocalDateTime.now().format(formatter);
        String fullDescription = description + " (Timestamp: " + timestamp + ")";
        auditLogService.log(eventType, username, fullDescription);
        log.info("Evento registrado: {} por {}", eventType, username);
    }
}
