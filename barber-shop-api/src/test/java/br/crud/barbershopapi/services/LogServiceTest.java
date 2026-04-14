package br.crud.barbershopapi.services;

import br.crud.barbershopapi.audit.AuditLogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

/**
 * TESTES UNITÁRIOS: LogService
 * 
 * Valida o registro de todos os tipos de eventos:
 * - Gestão de usuários (cadastro, alteração, exclusão)
 * - Autenticação (erros, bloqueios)
 * - Eventos de negócio (5+ tipos)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Testes de Registro de Logs")
class LogServiceTest {

    private LogService logService;

    @Mock
    private AuditLogService auditLogService;

    @BeforeEach
    void setUp() {
        logService = new LogService(auditLogService);
    }

    // ========== TESTES DE GESTÃO DE USUÁRIOS ==========

    @Test
    @DisplayName("DEVE registrar cadastro de novo usuário com tipo REGISTRO_USUARIO")
    void testLogUserRegistration() {
        // Arrange
        String username = "joao.silva";

        // Act
        logService.logUserRegistration(username);

        // Assert
        verify(auditLogService, times(1)).log(
            eq("REGISTRO_USUARIO"),
            eq(username),
            argThat(desc -> desc.contains("Novo usuário cadastrado no sistema"))
        );
    }

    @Test
    @DisplayName("DEVE registrar alteração de dados/senha com tipo ALTERACAO_USUARIO")
    void testLogUserDataModification() {
        // Arrange
        String username = "joao.silva";
        String fieldModified = "senha";

        // Act
        logService.logUserDataModification(username, fieldModified);

        // Assert
        verify(auditLogService, times(1)).log(
            eq("ALTERACAO_USUARIO"),
            eq(username),
            argThat(desc -> desc.contains("Dados de usuário alterados") 
                           && desc.contains("senha"))
        );
    }

    @Test
    @DisplayName("DEVE registrar exclusão de usuário com tipo EXCLUSAO_USUARIO")
    void testLogUserDeletion() {
        // Arrange
        String username = "joao.silva";

        // Act
        logService.logUserDeletion(username);

        // Assert
        verify(auditLogService, times(1)).log(
            eq("EXCLUSAO_USUARIO"),
            eq(username),
            argThat(desc -> desc.contains("Usuário removido do sistema"))
        );
    }

    // ========== TESTES DE AUTENTICAÇÃO ==========

    @Test
    @DisplayName("DEVE registrar erro de autenticação com tipo ERRO_AUTENTICACAO")
    void testLogAuthenticationError() {
        // Arrange
        String username = "joao.silva";

        // Act
        logService.logAuthenticationError(username);

        // Assert
        verify(auditLogService, times(1)).log(
            eq("ERRO_AUTENTICACAO"),
            eq(username),
            argThat(desc -> desc.contains("Falha na autenticação") 
                           && desc.contains("inválidas"))
        );
    }

    @Test
    @DisplayName("DEVE registrar 5 falhas com tipo ALERTA_BLOQUEIO_CONTA")
    void testLogMultipleAuthenticationFailures() {
        // Arrange
        String username = "joao.silva";
        int failureCount = 5;

        // Act
        logService.logMultipleAuthenticationFailures(username, failureCount);

        // Assert
        verify(auditLogService, times(1)).log(
            eq("ALERTA_BLOQUEIO_CONTA"),
            eq(username),
            argThat(desc -> desc.contains("5 falhas") 
                           && desc.contains("bloqueada"))
        );
    }

    // ========== TESTES DE EVENTOS DE NEGÓCIO ==========

    @Test
    @DisplayName("EVENTO 1: DEVE registrar nova localidade de venda")
    void testLogNewLocationAdded() {
        // Arrange
        String locationName = "Filial Boa Viagem";
        String address = "Rua das Flores, 123, Recife - PE";

        // Act
        logService.logNewLocationAdded(locationName, address);

        // Assert
        verify(auditLogService, times(1)).log(
            eq("EVENTO_NOVA_LOCALIDADE"),
            eq("SISTEMA"),
            argThat(desc -> desc.contains("Nova localidade") 
                           && desc.contains(locationName)
                           && desc.contains(address))
        );
    }

    @Test
    @DisplayName("EVENTO 2: DEVE registrar alteração de endereço de entrega")
    void testLogDeliveryAddressModification() {
        // Arrange
        String clientName = "João Silva";
        String oldAddress = "Rua A, 100";
        String newAddress = "Rua B, 200";

        // Act
        logService.logDeliveryAddressModification(clientName, oldAddress, newAddress);

        // Assert
        verify(auditLogService, times(1)).log(
            eq("EVENTO_ALTERACAO_ENDERECO"),
            eq(clientName),
            argThat(desc -> desc.contains("Endereço de entrega alterado") 
                           && desc.contains(oldAddress)
                           && desc.contains(newAddress))
        );
    }

    @Test
    @DisplayName("EVENTO 3: DEVE registrar novo agendamento de serviço")
    void testLogServiceScheduling() {
        // Arrange
        String clientName = "João Silva";
        String serviceName = "Corte + Barba";
        String scheduledDateTime = "2026-04-10 14:30";

        // Act
        logService.logServiceScheduling(clientName, serviceName, scheduledDateTime);

        // Assert
        verify(auditLogService, times(1)).log(
            eq("EVENTO_AGENDAMENTO_CRIADO"),
            eq(clientName),
            argThat(desc -> desc.contains("Novo agendamento criado") 
                           && desc.contains(serviceName)
                           && desc.contains(scheduledDateTime))
        );
    }

    @Test
    @DisplayName("EVENTO 4: DEVE registrar cancelamento de agendamento")
    void testLogSchedulingCancellation() {
        // Arrange
        String clientName = "João Silva";
        String schedulingId = "AGD-2026-001";

        // Act
        logService.logSchedulingCancellation(clientName, schedulingId);

        // Assert
        verify(auditLogService, times(1)).log(
            eq("EVENTO_AGENDAMENTO_CANCELADO"),
            eq(clientName),
            argThat(desc -> desc.contains("Agendamento cancelado") 
                           && desc.contains(schedulingId))
        );
    }

    @Test
    @DisplayName("EVENTO 5: DEVE registrar alteração de tarifa/preço")
    void testLogPriceModification() {
        // Arrange
        String serviceName = "Corte de Cabelo";
        double oldPrice = 35.00;
        double newPrice = 45.00;

        // Act
        logService.logPriceModification(serviceName, oldPrice, newPrice);

        // Assert
        verify(auditLogService, times(1)).log(
            eq("EVENTO_ALTERACAO_TARIFA"),
            eq("SISTEMA"),
            argThat(desc -> desc.contains("Alteração de tarifa") 
                           && desc.contains(serviceName)
                           && desc.contains("35.00")
                           && desc.contains("45.00"))
        );
    }

    // ========== TESTES DE EVENTO GENÉRICO ==========

    @Test
    @DisplayName("DEVE registrar evento customizado com tipo personalizado")
    void testLogCustomEvent() {
        // Arrange
        String eventType = "EVENTO_PAGAMENTO";
        String username = "joao.silva";
        String description = "Pagamento realizado via PIX";

        // Act
        logService.logCustomEvent(eventType, username, description);

        // Assert
        verify(auditLogService, times(1)).log(
            eq("EVENTO_PAGAMENTO"),
            eq(username),
            argThat(desc -> desc.contains(description))
        );
    }

    // ========== TESTES DE INTEGRAÇÃO ==========

    @Test
    @DisplayName("TESTE FLUXO COMPLETO: Cadastro + Alteração + Exclusão de usuário")
    void testCompleteUserLifecycle() {
        // Arrange
        String username = "maria.santos";

        // Act
        logService.logUserRegistration(username);
        logService.logUserDataModification(username, "email");
        logService.logUserDeletion(username);

        // Assert
        verify(auditLogService, times(3)).log(anyString(), anyString(), anyString());
        verify(auditLogService, times(1)).log(eq("REGISTRO_USUARIO"), eq(username), anyString());
        verify(auditLogService, times(1)).log(eq("ALTERACAO_USUARIO"), eq(username), anyString());
        verify(auditLogService, times(1)).log(eq("EXCLUSAO_USUARIO"), eq(username), anyString());
    }

    @Test
    @DisplayName("TESTE FLUXO COMPLETO: Erros de autenticação com bloqueio")
    void testAuthenticationFailureFlow() {
        // Arrange
        String username = "hacker.attempt";

        // Act
        logService.logAuthenticationError(username);
        logService.logAuthenticationError(username);
        logService.logAuthenticationError(username);
        logService.logAuthenticationError(username);
        logService.logMultipleAuthenticationFailures(username, 5);

        // Assert
        verify(auditLogService, times(5)).log(anyString(), eq(username), anyString());
        verify(auditLogService, times(4)).log(eq("ERRO_AUTENTICACAO"), eq(username), anyString());
        verify(auditLogService, times(1)).log(eq("ALERTA_BLOQUEIO_CONTA"), eq(username), anyString());
    }

    @Test
    @DisplayName("TESTE FLUXO COMPLETO: Agendamento com alteração e cancelamento")
    void testSchedulingLifecycle() {
        // Arrange
        String clientName = "Pedro Oliveira";

        // Act
        logService.logServiceScheduling(clientName, "Corte", "2026-04-10 14:30");
        logService.logDeliveryAddressModification(clientName, "Rua X", "Rua Y");
        logService.logSchedulingCancellation(clientName, "AGD-001");

        // Assert
        verify(auditLogService, times(3)).log(anyString(), anyString(), anyString());
        verify(auditLogService, times(1)).log(eq("EVENTO_AGENDAMENTO_CRIADO"), eq(clientName), anyString());
        verify(auditLogService, times(1)).log(eq("EVENTO_ALTERACAO_ENDERECO"), eq(clientName), anyString());
        verify(auditLogService, times(1)).log(eq("EVENTO_AGENDAMENTO_CANCELADO"), eq(clientName), anyString());
    }
}
