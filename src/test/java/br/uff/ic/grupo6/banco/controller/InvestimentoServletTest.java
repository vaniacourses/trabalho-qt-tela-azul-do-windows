package br.uff.ic.grupo6.banco.controller;

import br.uff.ic.grupo6.banco.model.Cliente;
import br.uff.ic.grupo6.banco.model.Conta;
import br.uff.ic.grupo6.banco.model.Investimento;
import br.uff.ic.grupo6.banco.service.InvestimentoService;
import br.uff.ic.grupo6.banco.service.exception.ValidationException;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvestimentoServletTest {

	@Mock
	private HttpServletRequest request;
	@Mock
	private HttpServletResponse response;
	@Mock
	private HttpSession session;
	@Mock
	private RequestDispatcher requestDispatcher;

	// Mock do SERVIÇO que será injetado
	@Mock
	private InvestimentoService investimentoService;

	// A instância do Servlet que vamos testar
	private InvestimentoServlet servlet;

	@BeforeEach
	void setUp() {
		// Injetamos o mock do SERVIÇO no construtor do Servlet
		servlet = new InvestimentoServlet(investimentoService);

		when(request.getSession()).thenReturn(session);
	}

	@Test
	@DisplayName("Deve redirecionar para login se nao houver cliente logado")
	void doPost_QuandoClienteNaoLogado_DeveRedirecionarParaLogin() throws ServletException, IOException {
		when(session.getAttribute("usuarioLogado")).thenReturn(null);
		servlet.doPost(request, response);
		verify(response).sendRedirect("login.jsp");
	}

	@Test
	@DisplayName("Deve redirecionar com erro se o tipo de investimento for invalido")
	void doPost_QuandoTipoInvestimentoInvalido_DeveRedirecionarComErro()
			throws ServletException, IOException, SQLException, ValidationException {
		// Setup
		when(session.getAttribute("usuarioLogado")).thenReturn(new Cliente());
		when(request.getParameter("tipoInvestimento")).thenReturn("TIPO_INVALIDO");
		when(request.getParameter("valor")).thenReturn("100.00"); // Precisa de um valor para a chamada do serviço

		// Simula o serviço lançando a exceção de validação
		when(investimentoService.realizarInvestimento(anyInt(), eq("TIPO_INVALIDO"), anyDouble(), anyDouble()))
				.thenThrow(new ValidationException("Tipo de investimento inválido."));

		// Execução
		servlet.doPost(request, response);

		// Verificação
		verify(response).sendRedirect(contains("Tipo+de+investimento+inv%C3%A1lido."));
	}

	@Test
	@DisplayName("Deve redirecionar com erro se o valor do investimento for negativo")
	void doPost_QuandoValorInvestimentoNegativo_DeveRedirecionarComErro()
			throws ServletException, IOException, SQLException, ValidationException {
		// Setup
		when(session.getAttribute("usuarioLogado")).thenReturn(new Cliente());
		when(request.getParameter("tipoInvestimento")).thenReturn("SELIC");
		when(request.getParameter("valor")).thenReturn("-100.00");

		// Simula o serviço lançando a exceção de validação
		when(investimentoService.realizarInvestimento(anyInt(), eq("SELIC"), eq(-100.00), anyDouble()))
				.thenThrow(new ValidationException("O valor do investimento deve ser positivo."));

		// Execução
		servlet.doPost(request, response);

		// Verificação
		verify(response).sendRedirect(contains("O+valor+do+investimento+deve+ser+positivo."));
	}

	@Test
	@DisplayName("Deve redirecionar com erro se o saldo for insuficiente")
	void doPost_QuandoSaldoInsuficiente_DeveRedirecionarComErro()
			throws ServletException, IOException, SQLException, ValidationException {
		// Setup
		Cliente cliente = new Cliente();
		Conta contaComPoucoSaldo = new Conta("0001", "123", 50.0); // Saldo de R$ 50
		cliente.setConta(contaComPoucoSaldo);
		when(session.getAttribute("usuarioLogado")).thenReturn(cliente);
		when(request.getParameter("tipoInvestimento")).thenReturn("CDB");
		when(request.getParameter("valor")).thenReturn("100.00"); // Tentando investir R$ 100

		// Simula o serviço lançando a exceção de validação
		when(investimentoService.realizarInvestimento(anyInt(), eq("CDB"), eq(100.00), eq(50.0)))
				.thenThrow(new ValidationException("Saldo insuficiente para realizar o investimento."));

		// Execução
		servlet.doPost(request, response);

		// Verificação
		verify(response).sendRedirect(contains("Saldo+insuficiente+para+realizar+o+investimento."));
	}

	@Test
	@DisplayName("Deve encaminhar para a página de comprovante em caso de sucesso")
	void doPost_QuandoInvestimentoValido_DeveEncaminharParaComprovante() throws Exception {
		// Setup
		Cliente cliente = new Cliente();
		Conta contaComSaldo = new Conta("0001", "123", 1000.0);
		contaComSaldo.setId(1);
		cliente.setConta(contaComSaldo);

		when(session.getAttribute("usuarioLogado")).thenReturn(cliente);
		when(request.getParameter("tipoInvestimento")).thenReturn("FII");
		when(request.getParameter("valor")).thenReturn("500.00");

		Investimento investimentoComprovante = new Investimento();
		investimentoComprovante.setValorAplicado(new BigDecimal("500.00"));
		investimentoComprovante.setTipoInvestimento("FII");

		// Mock do SERVIÇO: quando for chamado, retorna o comprovante
		when(investimentoService.realizarInvestimento(1, "FII", 500.00, 1000.0)).thenReturn(investimentoComprovante);

		when(request.getRequestDispatcher("comprovanteInvestimento.jsp")).thenReturn(requestDispatcher);

		// Execução
		servlet.doPost(request, response);

		// O método do SERVIÇO foi chamado com os valores corretos?
		verify(investimentoService).realizarInvestimento(1, "FII", 500.00, 1000.0);

		// O saldo do objeto na sessão foi atualizado?
		assertEquals(500.0, contaComSaldo.getSaldo(), "O saldo na sessão deveria ser atualizado para 500.0");

		// O comprovante foi colocado como atributo na requisição?
		verify(request).setAttribute("comprovante", investimentoComprovante);

		// O servlet encaminhou para a página de comprovante?
		verify(requestDispatcher).forward(request, response);
	}

	@Test
	@DisplayName("Deve redirecionar com erro se o Servico lancar uma SQLException")
	void doPost_QuandoServicoLancaSQLException_DeveRedirecionarComErro() throws Exception {
		// Setup
		Cliente cliente = new Cliente();
		Conta contaComSaldo = new Conta("0001", "123", 1000.0);
		contaComSaldo.setId(1);
		cliente.setConta(contaComSaldo);

		when(session.getAttribute("usuarioLogado")).thenReturn(cliente);
		when(request.getParameter("tipoInvestimento")).thenReturn("CDB");
		when(request.getParameter("valor")).thenReturn("200.00");

		// Mock do SERVIÇO para lançar exceção
		when(investimentoService.realizarInvestimento(anyInt(), anyString(), anyDouble(), anyDouble()))
				.thenThrow(new SQLException("Erro simulado de conexao com o banco de dados"));

		// Execução
		servlet.doPost(request, response);

		// Verificação
		verify(response).sendRedirect(contains("Ocorreu+um+erro+ao+processar+o+investimento."));
	}
}