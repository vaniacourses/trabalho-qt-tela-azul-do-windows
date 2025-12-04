package br.uff.ic.grupo6.banco.estrutural;

import br.uff.ic.grupo6.banco.dao.ContaDAO;
import br.uff.ic.grupo6.banco.dao.TransacaoDAO;
import br.uff.ic.grupo6.banco.dao.UsuarioDAO;
import br.uff.ic.grupo6.banco.model.Transacao;
import br.uff.ic.grupo6.banco.service.TransacaoService;
import br.uff.ic.grupo6.banco.service.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

public class DepositoStructuralTest {

	private TransacaoService transacaoService;
	private TransacaoDAO transacaoDAOMock;
	private ContaDAO contaDAOMock;
	private UsuarioDAO usuarioDAOMock;

	@BeforeEach
	public void setUp() {
		// Mocks
		transacaoDAOMock = Mockito.mock(TransacaoDAO.class);
		contaDAOMock = Mockito.mock(ContaDAO.class);
		usuarioDAOMock = Mockito.mock(UsuarioDAO.class);

		// Injeção de dependência
		transacaoService = new TransacaoService(contaDAOMock, transacaoDAOMock, usuarioDAOMock);
	}

	@DisplayName("Teste Aresta Verdadeira: Deve lançar ValidationException quando o valor do depósito for inválido (<= 0)")
	@Test
	public void testRealizarDepositoValorInvalido_DeveLancarException() throws SQLException {
		int idConta = 1;
		double valorInvalido = 0.0; // Testa o limite (<= 0)

		ValidationException exception = assertThrows(ValidationException.class, () -> {
			transacaoService.realizarDeposito(idConta, valorInvalido);
		});

		assertEquals("O valor do deposito deve ser positivo.", exception.getMessage());

		// Garante que o DAO NÃO foi chamado
		verify(contaDAOMock, never()).realizarDeposito(anyInt(), anyDouble());
	}

	@DisplayName("Teste Aresta Falsa: Deve realizar o depósito via DAO quando o valor for válido (positivo)")
	@Test
	public void testRealizarDepositoValorValido_DeveChamarDAO() throws ValidationException, SQLException {

		int idConta = 1;
		double valorValido = 150.00;
		Transacao transacaoEsperada = new Transacao();

		when(contaDAOMock.realizarDeposito(idConta, valorValido)).thenReturn(transacaoEsperada);

		Transacao resultado = transacaoService.realizarDeposito(idConta, valorValido);

		assertNotNull(resultado);
		assertEquals(transacaoEsperada, resultado);

		verify(contaDAOMock, times(1)).realizarDeposito(idConta, valorValido);
	}
}