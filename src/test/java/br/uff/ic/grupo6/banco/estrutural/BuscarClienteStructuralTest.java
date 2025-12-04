package br.uff.ic.grupo6.banco.estrutural;

import br.uff.ic.grupo6.banco.dao.UsuarioDAO;
import br.uff.ic.grupo6.banco.model.Cliente;
import br.uff.ic.grupo6.banco.service.ClienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class BuscarClienteStructuralTest {

	private ClienteService clienteService;
	private UsuarioDAO usuarioDAOMock;

	@BeforeEach
	public void setUp() {
		// Mock do DAO
		usuarioDAOMock = Mockito.mock(UsuarioDAO.class);

		// Injeção de dependência via construtor
		clienteService = new ClienteService(usuarioDAOMock);
	}

	@DisplayName("Teste Aresta 1: Deve buscar todos os clientes ordenados quando a ação for 'listarTodos'")
	@Test
	public void testBuscarClientes_AcaoListarTodos_DeveBuscarTodos() throws SQLException {
		String acao = "listarTodos";
		String termoBusca = null;

		List<Cliente> listaEsperada = new ArrayList<>();
		listaEsperada.add(new Cliente()); // Adiciona um dummy para verificar retorno

		when(usuarioDAOMock.buscarTodosClientesOrdenados()).thenReturn(listaEsperada);

		List<Cliente> resultado = clienteService.buscarClientes(termoBusca, acao);

		assertEquals(listaEsperada, resultado);

		// Verifica se chamou o método correto
		verify(usuarioDAOMock, times(1)).buscarTodosClientesOrdenados();
		// Garante que NÃO chamou a busca por termo
		verify(usuarioDAOMock, never()).buscarClientesPorTermo(anyString());
	}

	@DisplayName("Teste Aresta 2: Deve buscar clientes por termo quando a ação não for 'listarTodos' e o termo for válido")
	@Test
	public void testBuscarClientes_TermoValido_DeveBuscarPorTermo() throws SQLException {

		String acao = "buscar"; // Qualquer coisa diferente de "listarTodos"
		String termoBusca = "Maria";

		List<Cliente> listaEsperada = new ArrayList<>();
		listaEsperada.add(new Cliente());

		when(usuarioDAOMock.buscarClientesPorTermo(termoBusca)).thenReturn(listaEsperada);

		List<Cliente> resultado = clienteService.buscarClientes(termoBusca, acao);

		assertEquals(listaEsperada, resultado);

		// Verifica se chamou o método correto
		verify(usuarioDAOMock, times(1)).buscarClientesPorTermo(termoBusca);
		// Garante que NÃO chamou a busca geral
		verify(usuarioDAOMock, never()).buscarTodosClientesOrdenados();
	}

	@DisplayName("Teste Aresta 3: Deve retornar lista vazia quando nenhum critério de busca for atendido")
	@Test
	public void testBuscarClientes_SemCriterio_DeveRetornarVazio() throws SQLException {
		String acao = "qualquerOutraCoisa";
		String termoBusca = ""; // Vazio ou Null falha na condição do else if

		List<Cliente> resultado = clienteService.buscarClientes(termoBusca, acao);

		assertTrue(resultado.isEmpty());

		// Garante que NENHUM método do DAO foi chamado
		verifyNoInteractions(usuarioDAOMock);
	}
}