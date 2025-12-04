package br.uff.ic.grupo6.banco.estrutural;

import br.uff.ic.grupo6.banco.dao.ContaDAO;
import br.uff.ic.grupo6.banco.dao.TransacaoDAO;
import br.uff.ic.grupo6.banco.dao.UsuarioDAO;
import br.uff.ic.grupo6.banco.model.Transacao;
import br.uff.ic.grupo6.banco.service.TransacaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

public class ExtratoStructuralTest {

	private TransacaoService transacaoService;
	private TransacaoDAO transacaoDAOMock;
	private ContaDAO contaDAOMock;
	private UsuarioDAO usuarioDAOMock;

	@BeforeEach
	public void setUp() {
		// Mocks para as dependências para isolar o teste da camada de serviço
		transacaoDAOMock = Mockito.mock(TransacaoDAO.class);
		contaDAOMock = Mockito.mock(ContaDAO.class);
		usuarioDAOMock = Mockito.mock(UsuarioDAO.class);

		// mocks injetados no serviço através do construtor
		transacaoService = new TransacaoService(contaDAOMock, transacaoDAOMock, usuarioDAOMock);
	}

	@DisplayName("Teste Aresta Verdadeira: Deve chamar busca por período quando datas de início ou fim forem fornecidas")
	@Test
	public void testBuscarExtratoComFiltroDeData_DeveChamarBuscaPorPeriodo() throws SQLException {
		int idConta = 1;
		LocalDate dataInicio = LocalDate.of(2023, 1, 1);
		LocalDate dataFim = LocalDate.of(2023, 1, 31);

		List<Transacao> listaEsperada = new ArrayList<>();
		when(transacaoDAOMock.buscarTransacoesPorPeriodo(idConta, dataInicio, dataFim)).thenReturn(listaEsperada);

		List<Transacao> resultado = transacaoService.buscarExtrato(idConta, dataInicio, dataFim);

		assertNotNull(resultado);
		assertEquals(listaEsperada, resultado);

		// Verifica se o método correto do DAO foi chamado (Cobre a linha do IF)
		verify(transacaoDAOMock, times(1)).buscarTransacoesPorPeriodo(idConta, dataInicio, dataFim);

		// Garante que o método do ELSE NÃO foi chamado
		verify(transacaoDAOMock, never()).buscarTodasTransacoesPorConta(anyInt());
	}

	@DisplayName("Teste Aresta Falsa: Deve chamar busca geral quando nenhuma data for fornecida (null)")
	@Test
	public void testBuscarExtratoSemFiltroDeData_DeveChamarBuscaGeral() throws SQLException {
		int idConta = 1;
		LocalDate dataInicio = null;
		LocalDate dataFim = null;

		List<Transacao> listaEsperada = new ArrayList<>();

		when(transacaoDAOMock.buscarTodasTransacoesPorConta(idConta)).thenReturn(listaEsperada);

		List<Transacao> resultado = transacaoService.buscarExtrato(idConta, dataInicio, dataFim);

		assertNotNull(resultado);
		assertEquals(listaEsperada, resultado);

		// Verifica se o método correto do DAO foi chamado (Cobre a linha do ELSE)
		verify(transacaoDAOMock, times(1)).buscarTodasTransacoesPorConta(idConta);

		// Garante que o método do IF NÃO foi chamado
		verify(transacaoDAOMock, never()).buscarTransacoesPorPeriodo(anyInt(), any(), any());
	}
}