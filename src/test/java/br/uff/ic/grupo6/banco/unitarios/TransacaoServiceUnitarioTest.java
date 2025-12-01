package br.uff.ic.grupo6.banco.unitarios;

import br.uff.ic.grupo6.banco.dao.ContaDAO;
import br.uff.ic.grupo6.banco.dao.UsuarioDAO;
import br.uff.ic.grupo6.banco.model.Cliente;
import br.uff.ic.grupo6.banco.model.Conta;
import br.uff.ic.grupo6.banco.service.TransacaoService;
import br.uff.ic.grupo6.banco.service.exception.ValidationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.MockitoAnnotations;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransacaoServiceUnitarioTest {

    @Mock private ContaDAO contaDAO;
    @Mock private UsuarioDAO usuarioDAO;

    @InjectMocks
    private TransacaoService service;

    private Cliente clienteOrigem;
    private Conta contaOrigem;
    private Conta contaDestino;
    private Cliente clienteDestino;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

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

    private void mockDestinoValido() throws Exception {
        when(contaDAO.buscarContaPorAgenciaENumeroDaConta("1111", "56789"))
                .thenReturn(contaDestino);

        when(usuarioDAO.buscarClientePorIdConta(2))
                .thenReturn(clienteDestino);
    }

    // ============================================================
    //                        TESTES DE SUCESSO
    // ============================================================

    @Test
    @DisplayName("Deve permitir transferência válida")
    void devePermitirTransferenciaValida() throws Exception {
        mockDestinoValido();

        Map<String, Object> dados = service.prepararTransferencia(
                clienteOrigem, "1111", "56789", 500);

        assertNotNull(dados);
        assertEquals(contaDestino, dados.get("contaDeDestino"));
        assertEquals(clienteDestino, dados.get("clienteDestino"));
        assertEquals(500.0, dados.get("valor"));

        verify(contaDAO).buscarContaPorAgenciaENumeroDaConta("1111", "56789");
        verify(usuarioDAO).buscarClientePorIdConta(2);
        verifyNoMoreInteractions(contaDAO, usuarioDAO);
    }

    // ============================================================
    //                TESTES DE VALIDAÇÃO DE ENTRADA
    // ============================================================

    @Test
    @DisplayName("Valor negativo deve lançar erro")
    void deveLancarErroQuandoValorNegativo() {

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.prepararTransferencia(clienteOrigem, "1111", "56789", -10)
        );

        assertEquals("Valor deve ser positivo", ex.getMessage());
    }

    @Test
    @DisplayName("Saldo insuficiente deve lançar erro")
    void deveLancarErroQuandoSaldoInsuficiente() {
        when(contaOrigem.getSaldo()).thenReturn(100.0);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.prepararTransferencia(clienteOrigem, "1111", "56789", 500)
        );

        assertEquals("Saldo insuficiente", ex.getMessage());
    }

    // ============================================================
    //                   TESTES SOBRE O DESTINO
    // ============================================================

    @Test
    @DisplayName("Conta destino inexistente deve lançar erro")
    void deveLancarErroQuandoContaDestinoNaoExiste() throws Exception {
        when(contaDAO.buscarContaPorAgenciaENumeroDaConta("1111", "56789"))
                .thenReturn(null);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.prepararTransferencia(clienteOrigem, "1111", "56789", 200)
        );

        assertEquals("Conta de destino nao encontrada", ex.getMessage());
        verify(contaDAO).buscarContaPorAgenciaENumeroDaConta("1111", "56789");
        verifyNoMoreInteractions(contaDAO);
        verifyNoInteractions(usuarioDAO);
    }

    @Test
    @DisplayName("Transferência para mesma conta deve falhar")
    void deveLancarErroQuandoDestinoForMesmaConta() throws Exception {
        when(contaDestino.getId()).thenReturn(1); // igual origem
        when(contaDAO.buscarContaPorAgenciaENumeroDaConta("1111", "56789"))
                .thenReturn(contaDestino);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.prepararTransferencia(clienteOrigem, "1111", "56789", 200)
        );

        assertEquals("Conta de destino nao pode ser a mesma de origem", ex.getMessage());
        verify(contaDAO).buscarContaPorAgenciaENumeroDaConta("1111", "56789");
        verifyNoInteractions(usuarioDAO);
    }

    @Test
    @DisplayName("Cliente destino inexistente deve falhar")
    void deveLancarErroQuandoClienteDestinoNaoExiste() throws Exception {
        when(contaDAO.buscarContaPorAgenciaENumeroDaConta("1111", "56789"))
                .thenReturn(contaDestino);
        when(usuarioDAO.buscarClientePorIdConta(2))
                .thenReturn(null);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.prepararTransferencia(clienteOrigem, "1111", "56789", 200)
        );

        assertEquals("Cliente de destino nao encontrado", ex.getMessage());

        verify(contaDAO).buscarContaPorAgenciaENumeroDaConta("1111", "56789");
        verify(usuarioDAO).buscarClientePorIdConta(2);
        verifyNoMoreInteractions(contaDAO, usuarioDAO);
    }

    // ============================================================
    //                   TESTES DE LIMITES E BLOQUEIOS
    // ============================================================

    @Test
    @DisplayName("Valor acima do limite deve lançar erro")
    void deveLancarErroQuandoValorAcimaDoLimite() throws Exception {

        // garante saldo suficiente para não cair na regra de saldo insuficiente
        when(contaOrigem.getSaldo()).thenReturn(10000.0);

        when(contaDAO.buscarContaPorAgenciaENumeroDaConta("1111", "56789"))
                .thenReturn(contaDestino);

        when(usuarioDAO.buscarClientePorIdConta(2))
                .thenReturn(clienteDestino);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.prepararTransferencia(clienteOrigem, "1111", "56789", 6000)
        );

        assertEquals("O limite maximo por transferencia e de R$ 5.000,00.", ex.getMessage());
    }


    @Test
    @DisplayName("Agência 9999 deve ser bloqueada")
    void deveLancarErroQuandoAgenciaFor9999() throws Exception {

        // criar conta apenas para impedir 'conta não encontrada'
        when(contaDAO.buscarContaPorAgenciaENumeroDaConta("9999", "56789"))
                .thenReturn(contaDestino);

        when(usuarioDAO.buscarClientePorIdConta(2))
                .thenReturn(clienteDestino);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.prepararTransferencia(clienteOrigem, "9999", "56789", 200)
        );

        assertEquals("Nao e possível transferir para esta agencia.", ex.getMessage());
    }

    @Test
    @DisplayName("Agência 0000 deve ser bloqueada")
    void deveLancarErroQuandoAgenciaFor0000() throws Exception {

        when(contaDAO.buscarContaPorAgenciaENumeroDaConta("0000", "56789"))
                .thenReturn(contaDestino);

        when(usuarioDAO.buscarClientePorIdConta(2))
                .thenReturn(clienteDestino);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.prepararTransferencia(clienteOrigem, "0000", "56789", 200)
        );

        assertEquals("Nao e possível transferir para esta agencia.", ex.getMessage());
    }

    @Test
    @DisplayName("Cliente de baixa renda não pode transferir acima de 1000")
    void deveLancarErroParaClienteBaixaRendaComValorAlto() throws Exception {
        when(clienteOrigem.getRenda()).thenReturn(1500.0);
        when(contaDAO.buscarContaPorAgenciaENumeroDaConta("1111", "56789"))
                .thenReturn(contaDestino);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.prepararTransferencia(clienteOrigem, "1111", "56789", 2000)
        );

        assertEquals(
                "Clientes com renda inferior a R$ 2.000,00 tem limite de R$ 1.000,00 por transferencia.",
                ex.getMessage()
        );
    }

    @Test
    @DisplayName("Conta salário deve ser bloqueada")
    void deveLancarErroQuandoContaDestinoForContaSalario() throws Exception {
        when(contaDestino.getNumero()).thenReturn("91234");
        when(contaDAO.buscarContaPorAgenciaENumeroDaConta("1111", "91234"))
                .thenReturn(contaDestino);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.prepararTransferencia(clienteOrigem, "1111", "91234", 200)
        );

        assertEquals(
                "Nao e permitido transferir para Contas Salario (iniciadas com 9).",
                ex.getMessage());
    }
}
