package br.uff.ic.grupo6.banco.funcional;

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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransacaoServiceFuncionalTest {

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
        when(contaOrigem.getNumero()).thenReturn("1234");

        clienteOrigem = mock(Cliente.class);
        when(clienteOrigem.getConta()).thenReturn(contaOrigem);
        when(clienteOrigem.getRenda()).thenReturn(5000.0);

        contaDestino = mock(Conta.class);
        when(contaDestino.getId()).thenReturn(2);
        when(contaDestino.getNumero()).thenReturn("5678");

        clienteDestino = mock(Cliente.class);
    }

    // --- TESTE DE SUCESSO ---
    @Test
    void devePrepararTransferenciaComSucesso() throws Exception {
        when(contaDAO.buscarContaPorAgenciaENumeroDaConta("1234", "5678"))
                .thenReturn(contaDestino);

        when(usuarioDAO.buscarClientePorIdConta(2))
                .thenReturn(clienteDestino);

        Map<String, Object> result = service.prepararTransferencia(
                clienteOrigem, "1234", "5678", 500.0);

        assertEquals(clienteOrigem, result.get("clienteOrigem"));
        assertEquals(clienteDestino, result.get("clienteDestino"));
        assertEquals(contaDestino, result.get("contaDeDestino"));
        assertEquals(500.0, result.get("valor"));
    }

    // --- VALOR INVÁLIDO ---
    @Test
    void deveLancarErroQuandoValorNaoForPositivo() {
        Exception ex = assertThrows(
                ValidationException.class,
                () -> service.prepararTransferencia(clienteOrigem, "1234", "5678", 0)
        );
        assertEquals("Valor deve ser positivo", ex.getMessage());
    }

    // --- SALDO INSUFICIENTE ---
    @Test
    void deveLancarErroQuandoSaldoForInsuficiente() {
        when(contaOrigem.getSaldo()).thenReturn(100.0);

        Exception ex = assertThrows(
                ValidationException.class,
                () -> service.prepararTransferencia(clienteOrigem, "1234", "5678", 500)
        );
        assertEquals("Saldo insuficiente", ex.getMessage());
    }

    // --- CONTA DESTINO INEXISTENTE ---
    @Test
    void deveLancarErroQuandoContaDestinoNaoExistir() throws Exception {
        when(contaDAO.buscarContaPorAgenciaENumeroDaConta("1234", "9999"))
                .thenReturn(null);

        Exception ex = assertThrows(
                ValidationException.class,
                () -> service.prepararTransferencia(clienteOrigem, "1234", "9999", 500)
        );
        assertEquals("Conta de destino nao encontrada", ex.getMessage());
    }

    // --- MESMA CONTA ---
    @Test
    void deveLancarErroQuandoDestinoForMesmaConta() throws Exception {
        when(contaDestino.getId()).thenReturn(1); // mesmo ID da origem

        when(contaDAO.buscarContaPorAgenciaENumeroDaConta("1234", "5678"))
                .thenReturn(contaDestino);

        Exception ex = assertThrows(
                ValidationException.class,
                () -> service.prepararTransferencia(clienteOrigem, "1234", "5678", 500)
        );
        assertEquals("Conta de destino nao pode ser a mesma de origem", ex.getMessage());
    }

    // --- VALOR ACIMA DO LIMITE ---
    @Test
    void deveLancarErroQuandoValorExcederLimite() throws Exception {
        // saldo deve ser MAIOR que o valor para não cair na regra de saldo insuficiente
        when(contaOrigem.getSaldo()).thenReturn(10000.0);

        when(contaDAO.buscarContaPorAgenciaENumeroDaConta("1111", "56789"))
                .thenReturn(contaDestino);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.prepararTransferencia(clienteOrigem, "1111", "56789", 6000)
        );

        assertEquals("O limite maximo por transferencia e de R$ 5.000,00.", ex.getMessage());
    }
    // --- AGENCIA BLOQUEADA ---
    @Test
    void deveLancarErroParaAgenciaBloqueada() throws Exception {
        // A conta destino PRECISA existir para que o método chegue na validação da agência
        when(contaDAO.buscarContaPorAgenciaENumeroDaConta("9999", "56789"))
                .thenReturn(contaDestino);

        // Cliente destino também precisa existir para não travar antes
        when(usuarioDAO.buscarClientePorIdConta(2))
                .thenReturn(clienteDestino);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.prepararTransferencia(clienteOrigem, "9999", "56789", 500)
        );

        assertEquals("Nao e possível transferir para esta agencia.", ex.getMessage());
    }


    // --- CLIENTE BAIXA RENDA ---
    @Test
    void deveLancarErroParaClienteBaixaRendaComValorAlto() throws Exception {
        when(clienteOrigem.getRenda()).thenReturn(1500.0);

        when(contaDAO.buscarContaPorAgenciaENumeroDaConta("1234", "5678"))
                .thenReturn(contaDestino);

        Exception ex = assertThrows(
                ValidationException.class,
                () -> service.prepararTransferencia(clienteOrigem, "1234", "5678", 1500)
        );

        assertEquals(
                "Clientes com renda inferior a R$ 2.000,00 tem limite de R$ 1.000,00 por transferencia.",
                ex.getMessage()
        );
    }

    // --- CONTA SALÁRIO DESTINO ---
    @Test
    void deveLancarErroParaContaSalarioDestino() throws Exception {
        when(contaDestino.getNumero()).thenReturn("91234");

        when(contaDAO.buscarContaPorAgenciaENumeroDaConta("1234", "91234"))
                .thenReturn(contaDestino);

        Exception ex = assertThrows(
                ValidationException.class,
                () -> service.prepararTransferencia(clienteOrigem, "1234", "91234", 200)
        );

        assertEquals(
                "Nao e permitido transferir para Contas Salario (iniciadas com 9).",
                ex.getMessage()
        );
    }

    // --- CLIENTE DESTINO NAO EXISTE ---
    @Test
    void deveLancarErroQuandoClienteDestinoNaoExistir() throws Exception {
        when(contaDAO.buscarContaPorAgenciaENumeroDaConta("1234", "5678"))
                .thenReturn(contaDestino);

        when(usuarioDAO.buscarClientePorIdConta(2))
                .thenReturn(null);

        Exception ex = assertThrows(
                ValidationException.class,
                () -> service.prepararTransferencia(clienteOrigem, "1234", "5678", 500)
        );

        assertEquals("Cliente de destino nao encontrado", ex.getMessage());
    }
}
