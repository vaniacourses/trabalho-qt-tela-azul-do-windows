package br.uff.ic.grupo6.banco.mutacao;

import br.uff.ic.grupo6.banco.dao.ContaDAO;
import br.uff.ic.grupo6.banco.dao.UsuarioDAO;
import br.uff.ic.grupo6.banco.model.Cliente;
import br.uff.ic.grupo6.banco.model.Conta;
import br.uff.ic.grupo6.banco.service.TransacaoService;
import br.uff.ic.grupo6.banco.service.exception.ValidationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransacaoServiceMutacaoTest {

    @Mock
    private ContaDAO contaDAO;

    @Mock
    private UsuarioDAO usuarioDAO;

    @InjectMocks
    private TransacaoService service;

    private Cliente clienteOrigem;
    private Conta contaOrigem;
    private Conta contaDestino;

    @BeforeEach
    void setup() {
        clienteOrigem = mock(Cliente.class);
        contaOrigem = mock(Conta.class);
        contaDestino = mock(Conta.class);

        when(clienteOrigem.getConta()).thenReturn(contaOrigem);
    }

    // Validação 1 — valor > 0
    @Test
    void deveFalharQuandoValorNaoForPositivo() {
        assertThrows(ValidationException.class, () ->
                service.prepararTransferencia(clienteOrigem, "001", "1234", 0)
        );
    }

    // Validação 2 — saldo insuficiente
    @Test
    void deveFalharQuandoSaldoInsuficiente() {
        when(contaOrigem.getSaldo()).thenReturn(500.0);

        assertThrows(ValidationException.class, () ->
                service.prepararTransferencia(clienteOrigem, "001", "1234", 600)
        );
    }

    // Validação 3 — conta destino inexistente
    @Test
    void deveFalharQuandoContaDestinoNaoExistir() throws SQLException {
        when(contaOrigem.getSaldo()).thenReturn(5000.0);

        when(contaDAO.buscarContaPorAgenciaENumeroDaConta("001", "1234"))
                .thenReturn(null);

        assertThrows(ValidationException.class, () ->
                service.prepararTransferencia(clienteOrigem, "001", "1234", 10)
        );
    }

    // Validação 4 — mesma conta
    @Test
    void deveFalharQuandoTransferirParaMesmaConta() throws SQLException {

        Conta contaDestinoReal = mock(Conta.class, invocation -> {
            String metodo = invocation.getMethod().getName();
            if (metodo.equals("getNumero")) return "9999";
            if (metodo.equals("getId")) return 0;
            return null;
        });

        when(contaDAO.buscarContaPorAgenciaENumeroDaConta("001", "9999"))
                .thenReturn(contaDestinoReal);

        assertThrows(ValidationException.class, () ->
                service.prepararTransferencia(clienteOrigem, "001", "9999", 50)
        );
    }


    // Validação 5 — limite máximo 5000
    @Test
    void deveFalharQuandoExcederLimiteMaximoDe5000() throws SQLException {
        when(contaOrigem.getSaldo()).thenReturn(10000.0);

        when(contaDAO.buscarContaPorAgenciaENumeroDaConta("001", "1111"))
                .thenReturn(contaDestino);

        assertThrows(ValidationException.class, () ->
                service.prepararTransferencia(clienteOrigem, "001", "1111", 5001)
        );
    }


    // Validação 6 — agências proibidas
    @Test
    void deveFalharQuandoAgenciaForBloqueada() throws SQLException {
        when(contaOrigem.getSaldo()).thenReturn(5000.0);

        when(contaDAO.buscarContaPorAgenciaENumeroDaConta("9999", "0000"))
                .thenReturn(contaDestino);

        assertThrows(ValidationException.class, () ->
                service.prepararTransferencia(clienteOrigem, "9999", "0000", 100)
        );
    }

  
    // Validação 7 — baixa renda ultrapassando limite
    @Test
    void deveFalharQuandoClienteBaixaRendaUltrapassarLimite() throws SQLException {

        when(contaOrigem.getSaldo()).thenReturn(5000.0);

        lenient().when(clienteOrigem.getRenda()).thenReturn(1500.0);

        when(contaDAO.buscarContaPorAgenciaENumeroDaConta("001", "2222"))
                .thenReturn(contaDestino);

        assertThrows(ValidationException.class, () ->
                service.prepararTransferencia(clienteOrigem, "001", "2222", 1501)
        );
    }

    
    // Validação 8 — conta salário começando com 9
    @Test
    void deveFalharQuandoContaDestinoForContaSalario() throws SQLException {

        when(contaDAO.buscarContaPorAgenciaENumeroDaConta("001", "91234"))
                .thenAnswer(invocation ->
                        mock(Conta.class, call -> {
                            if (call.getMethod().getName().equals("getNumero"))
                                return "91234";
                            return null;
                        })
                );

        assertThrows(ValidationException.class, () ->
                service.prepararTransferencia(clienteOrigem, "001", "91234", 500)
        );
    }




    // ===========  TESTES EXTRA  ============

    private void prepararCenarioTransferenciaValida(
            double saldoOrigem,
            String agencia,
            String conta,
            double renda,
            Conta destino
    ) throws SQLException {

        when(contaOrigem.getSaldo()).thenReturn(saldoOrigem);

        if (renda >= 0)
            when(clienteOrigem.getRenda()).thenReturn(renda);

        when(contaDAO.buscarContaPorAgenciaENumeroDaConta(agencia, conta))
                .thenReturn(destino);

        when(usuarioDAO.buscarClientePorIdConta(anyInt()))
                .thenReturn(new Cliente());
    }

    private Conta mockContaDestino(int id, String numero) {
        Conta c = mock(Conta.class);
        when(c.getId()).thenReturn(id);
        when(c.getNumero()).thenReturn(numero);
        return c;
    }


    @Test
    void deveAceitarValorMinimoPositivoExpondoMutante() throws SQLException {
        prepararCenarioTransferenciaValida(100, "001", "1234", 3000, mockContaDestino(2, "1234"));

        assertDoesNotThrow(() ->
                service.prepararTransferencia(clienteOrigem, "001", "1234", 0.01)
        );
    }

    // Linha 168 — valor > saldo 
    @Test
    void devePermitirTransferenciaQuandoValorIgualAoSaldo() throws SQLException {
        prepararCenarioTransferenciaValida(500, "001", "1234", 3000, mockContaDestino(2, "1234"));

        assertDoesNotThrow(() ->
                service.prepararTransferencia(clienteOrigem, "001", "1234", 500)
        );
    }

    // Linha 176 — baixa renda limite de 1000 
    @Test
    void devePermitirBaixaRendaQuandoValorExatamente1000() throws SQLException {
        prepararCenarioTransferenciaValida(5000, "001", "2222", 1500, mockContaDestino(3, "2222"));

        assertDoesNotThrow(() ->
                service.prepararTransferencia(clienteOrigem, "001", "2222", 1000)
        );
    }


    // Linha 180 — limite 5000
    @Test
    void devePermitirTransferenciaDeExatamente5000() throws SQLException {
        prepararCenarioTransferenciaValida(10000, "001", "1111", 3000, mockContaDestino(4, "1111"));

        assertDoesNotThrow(() ->
                service.prepararTransferencia(clienteOrigem, "001", "1111", 5000)
        );
    }
}
