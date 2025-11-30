package br.uff.ic.grupo6.banco.estrutural;

import br.uff.ic.grupo6.banco.dao.ContaDAO;
import br.uff.ic.grupo6.banco.dao.UsuarioDAO;
import br.uff.ic.grupo6.banco.model.Cliente;
import br.uff.ic.grupo6.banco.model.Conta;
import br.uff.ic.grupo6.banco.service.exception.ValidationException;

// IMPORTANTE — ESTE IMPORT PRECISA EXISTIR
import br.uff.ic.grupo6.banco.service.TransacaoService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransacaoServiceEstruturalTest {

    @Mock
    private ContaDAO contaDAO;

    @Mock
    private UsuarioDAO usuarioDAO;

    @InjectMocks
    private TransacaoService service;

    private Cliente clienteOrigem;
    private Conta contaOrigem;
    private Conta contaDestino;
    private Cliente clienteDestino;

    @BeforeEach
    void setup() {
        contaOrigem = mock(Conta.class);
        when(contaOrigem.getId()).thenReturn(1);
        when(contaOrigem.getSaldo()).thenReturn(3000.0);
        when(contaOrigem.getNumero()).thenReturn("12345");

        clienteOrigem = mock(Cliente.class);
        when(clienteOrigem.getConta()).thenReturn(contaOrigem);
        when(clienteOrigem.getRenda()).thenReturn(3000.0);

        contaDestino = mock(Conta.class);
        when(contaDestino.getId()).thenReturn(2);
        when(contaDestino.getNumero()).thenReturn("56789");

        clienteDestino = mock(Cliente.class);
    }

    @Test
    void caminhoSucesso() throws Exception {
        when(contaDAO.buscarContaPorAgenciaENumeroDaConta("1111", "56789"))
                .thenReturn(contaDestino);
        when(usuarioDAO.buscarClientePorIdConta(2))
                .thenReturn(clienteDestino);

        Map<String, Object> dados = service.prepararTransferencia(
                clienteOrigem, "1111", "56789", 500.0);

        assertNotNull(dados);
    }

    @Test
    void valorNegativo() {
        assertThrows(ValidationException.class,
                () -> service.prepararTransferencia(clienteOrigem, "1111", "56789", -10));
    }

    @Test
    void saldoInsuficiente() {
        when(contaOrigem.getSaldo()).thenReturn(100.0);

        assertThrows(ValidationException.class,
                () -> service.prepararTransferencia(clienteOrigem, "1111", "56789", 200.0));
    }

    @Test
    void contaDestinoNula() throws Exception {
        when(contaDAO.buscarContaPorAgenciaENumeroDaConta("1111", "0000"))
                .thenReturn(null);

        assertThrows(ValidationException.class,
                () -> service.prepararTransferencia(clienteOrigem, "1111", "0000", 100));
    }

    @Test
    void mesmaConta() throws Exception {
        when(contaDestino.getId()).thenReturn(1);
        when(contaDAO.buscarContaPorAgenciaENumeroDaConta("1111", "56789"))
                .thenReturn(contaDestino);

        assertThrows(ValidationException.class,
                () -> service.prepararTransferencia(clienteOrigem, "1111", "56789", 200));
    }

    @Test
    void valorAcimaLimite() throws Exception {
        when(contaDAO.buscarContaPorAgenciaENumeroDaConta("1111", "56789"))
                .thenReturn(contaDestino);

        assertThrows(ValidationException.class,
                () -> service.prepararTransferencia(clienteOrigem, "1111", "56789", 6000));
    }

    @Test
    void agenciaBloqueada9999() {
        assertThrows(ValidationException.class,
                () -> service.prepararTransferencia(clienteOrigem, "9999", "56789", 200));
    }

    @Test
    void agenciaBloqueada0000() {
        assertThrows(ValidationException.class,
                () -> service.prepararTransferencia(clienteOrigem, "0000", "56789", 200));
    }

    @Test
    void baixaRendaValorAlto() throws Exception {
        when(clienteOrigem.getRenda()).thenReturn(1500.0);
        when(contaDAO.buscarContaPorAgenciaENumeroDaConta("1111", "56789"))
                .thenReturn(contaDestino);

        assertThrows(ValidationException.class,
                () -> service.prepararTransferencia(clienteOrigem, "1111", "56789", 2000.0));
    }

    @Test
    void contaSalario() throws Exception {
        when(contaDestino.getNumero()).thenReturn("91234");

        when(contaDAO.buscarContaPorAgenciaENumeroDaConta("1111", "91234"))
                .thenReturn(contaDestino);

        assertThrows(ValidationException.class,
                () -> service.prepararTransferencia(clienteOrigem, "1111", "91234", 200));
    }

    @Test
    void clienteDestinoNulo() throws Exception {
        when(contaDAO.buscarContaPorAgenciaENumeroDaConta("1111", "56789"))
                .thenReturn(contaDestino);

        when(usuarioDAO.buscarClientePorIdConta(2))
                .thenReturn(null);

        assertThrows(ValidationException.class,
                () -> service.prepararTransferencia(clienteOrigem, "1111", "56789", 200));
    }
}
