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
import java.util.Map;

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

    // 1 — Teste ultra forte para validar fronteira valor == 0
    @Test
    void deveFalharQuandoValorIgualZero() {
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.prepararTransferencia(clienteOrigem, "1111", "56789", 0)
        );

        assertEquals("Valor deve ser positivo", ex.getMessage(),
                "Mutação que troca valor <= 0 por valor < 0 deve ser detectada");
    }

    // 2 — Teste para matar mutação em saldo insuficiente (>= ao invés de >)
    @Test
    void deveFalharQuandoValorIgualAoSaldo() throws Exception {
        when(contaOrigem.getSaldo()).thenReturn((double) 200);

        // conta destino precisa existir
        when(contaDAO.buscarContaPorAgenciaENumeroDaConta("1111", "56789"))
                .thenReturn(contaDestino);
        when(usuarioDAO.buscarClientePorIdConta(2))
                .thenReturn(clienteDestino);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.prepararTransferencia(clienteOrigem, "1111", "56789", 300)
        );

        assertEquals("Saldo insuficiente", ex.getMessage(),
                "Mutação que troca saldo < valor por saldo <= valor deve morrer");
    }

    // 3 — Matar mutante que remove verificação de contaDestino nula
    @Test
    void deveLancarErroSeContaDestinoForNull() throws Exception {
        when(contaDAO.buscarContaPorAgenciaENumeroDaConta("1111", "56789"))
                .thenReturn(null);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.prepararTransferencia(clienteOrigem, "1111", "56789", 100)
        );

        assertEquals("Conta de destino nao encontrada", ex.getMessage(),
                "Mutante que remove este if deve ser morto");
    }

    // 4 — Matar mutante que troca == por != nas contas iguais
    @Test
    void deveFalharSeContaDestinoIgualOrigem() throws Exception {
        when(contaDestino.getId()).thenReturn(1);
        when(contaDAO.buscarContaPorAgenciaENumeroDaConta("1111", "56789"))
                .thenReturn(contaDestino);
        when(usuarioDAO.buscarClientePorIdConta(1))
                .thenReturn(clienteDestino);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.prepararTransferencia(clienteOrigem, "1111", "56789", 100)
        );

        assertEquals("Conta de destino nao pode ser a mesma de origem", ex.getMessage());
    }

    // 5 — Limite máximo → matar mutante que muda > 5000 para >= 5000
    @Test
    void deveFalharAoExcederLimiteTransferencia() throws Exception {
        when(contaOrigem.getSaldo()).thenReturn(10000.0);
        when(contaDAO.buscarContaPorAgenciaENumeroDaConta("1111", "56789"))
                .thenReturn(contaDestino);
        when(usuarioDAO.buscarClientePorIdConta(2))
                .thenReturn(clienteDestino);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.prepararTransferencia(clienteOrigem, "1111", "56789", 5001)
        );

        assertEquals("O limite maximo por transferencia e de R$ 5.000,00.", ex.getMessage());
    }

    // 6 — Matar mutante que remove verificação de agência bloqueada
    @Test
    void deveFalharParaAgenciaBloqueada() throws Exception {
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

    // 7 — Matar mutante que ignora limite de baixa renda
    @Test
    void deveFalharParaClienteBaixaRendaValorAlto() throws Exception {
        when(clienteOrigem.getRenda()).thenReturn(1500.0);
        when(contaDAO.buscarContaPorAgenciaENumeroDaConta("1111", "56789"))
                .thenReturn(contaDestino);
        when(usuarioDAO.buscarClientePorIdConta(2))
                .thenReturn(clienteDestino);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.prepararTransferencia(clienteOrigem, "1111", "56789", 1500)
        );

        assertEquals(
                "Clientes com renda inferior a R$ 2.000,00 tem limite de R$ 1.000,00 por transferencia.",
                ex.getMessage()
        );
    }

    // 8 — Matar mutante que remove verificação de conta salário
    @Test
    void deveFalharParaContaSalario() throws Exception {
        when(contaDestino.getNumero()).thenReturn("91234");
        when(contaDAO.buscarContaPorAgenciaENumeroDaConta("1111", "91234"))
                .thenReturn(contaDestino);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.prepararTransferencia(clienteOrigem, "1111", "91234", 100)
        );

        assertEquals("Nao e permitido transferir para Contas Salario (iniciadas com 9).",
                ex.getMessage());
    }
}
