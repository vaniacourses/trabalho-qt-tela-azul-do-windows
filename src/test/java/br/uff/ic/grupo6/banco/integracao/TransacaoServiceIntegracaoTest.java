package br.uff.ic.grupo6.banco.integracao;

import br.uff.ic.grupo6.banco.dao.ContaDAO;
import br.uff.ic.grupo6.banco.dao.UsuarioDAO;
import br.uff.ic.grupo6.banco.model.Cliente;
import br.uff.ic.grupo6.banco.model.Conta;
import br.uff.ic.grupo6.banco.service.TransacaoService;
import br.uff.ic.grupo6.banco.service.exception.ValidationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.SQLException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransacaoServiceIntegracaoTest {

    @Mock
    private ContaDAO contaDAOParcial;

    @Mock
    private UsuarioDAO usuarioDAOParcial;

    @InjectMocks
    private TransacaoService service;

    private Cliente clienteOrigem;
    private Conta contaOrigem;
    private Conta contaDestino;
    private Cliente clienteDestino;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // Simula conta origem "vinda do banco"
        contaOrigem = new Conta("1111", "12345", 3000.0);
        contaOrigem.setId(1);

        clienteOrigem = mock(Cliente.class);
        when(clienteOrigem.getConta()).thenReturn(contaOrigem);
        when(clienteOrigem.getRenda()).thenReturn(5000.0);

        // Simula conta destino "vinda do banco"
        contaDestino = new Conta("2222", "56789", 1500.0);
        contaDestino.setId(2);

        // Simula cliente destino
        clienteDestino = mock(Cliente.class);
    }

    // ------------------------
    // TESTES DE INTEGRAÇÃO
    // ------------------------

    @Test
    void deveIntegrarServiceComDAOs_TransferenciaComSucesso() throws Exception {
        when(contaDAOParcial.buscarContaPorAgenciaENumeroDaConta("2222", "56789"))
                .thenReturn(contaDestino);

        when(usuarioDAOParcial.buscarClientePorIdConta(2))
                .thenReturn(clienteDestino);

        Map<String, Object> result = service.prepararTransferencia(
                clienteOrigem, "2222", "56789", 500
        );

        assertNotNull(result);
        assertEquals(contaDestino, result.get("contaDeDestino"));
        assertEquals(clienteDestino, result.get("clienteDestino"));
        assertEquals(500.0, result.get("valor"));
    }

    @Test
    void deveFalharQuandoContaDestinoNaoExiste() throws SQLException {
        when(contaDAOParcial.buscarContaPorAgenciaENumeroDaConta("9999", "0000"))
                .thenReturn(null);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.prepararTransferencia(clienteOrigem, "9999", "0000", 200)
        );

        assertEquals("Conta de destino nao encontrada", ex.getMessage());
    }

    @Test
    void deveFalharQuandoClienteDestinoNaoExiste() throws SQLException {
        when(contaDAOParcial.buscarContaPorAgenciaENumeroDaConta("2222", "56789"))
                .thenReturn(contaDestino);

        when(usuarioDAOParcial.buscarClientePorIdConta(2))
                .thenReturn(null);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.prepararTransferencia(clienteOrigem, "2222", "56789", 200)
        );

        assertEquals("Cliente de destino nao encontrado", ex.getMessage());
    }

    @Test
    void deveFalharQuandoSaldoInsuficiente() throws SQLException {
        contaOrigem.setSaldo(100);

        when(contaDAOParcial.buscarContaPorAgenciaENumeroDaConta("2222", "56789"))
                .thenReturn(contaDestino);

        when(usuarioDAOParcial.buscarClientePorIdConta(2))
                .thenReturn(clienteDestino);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.prepararTransferencia(clienteOrigem, "2222", "56789", 200)
        );

        assertEquals("Saldo insuficiente", ex.getMessage());
    }

    @Test
    void deveFalharQuandoAgenciaBloqueada() throws SQLException {
        // A conta precisa EXISTIR para não disparar "conta não encontrada"
        Conta contaFake = new Conta("9999", "56789", 1000.0);
        contaFake.setId(2);

        when(contaDAOParcial.buscarContaPorAgenciaENumeroDaConta("9999", "56789"))
                .thenReturn(contaFake);

        // Também precisamos simular cliente destino existente para não cair em outra validação
        when(usuarioDAOParcial.buscarClientePorIdConta(2))
                .thenReturn(mock(Cliente.class));

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.prepararTransferencia(clienteOrigem, "9999", "56789", 200)
        );

        assertEquals("Nao e possível transferir para esta agencia.", ex.getMessage());
    }
}
