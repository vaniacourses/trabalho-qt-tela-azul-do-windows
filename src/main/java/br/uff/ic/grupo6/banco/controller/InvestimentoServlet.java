package br.uff.ic.grupo6.banco.controller;

import br.uff.ic.grupo6.banco.model.Cliente;
import br.uff.ic.grupo6.banco.model.Conta;
import br.uff.ic.grupo6.banco.model.Investimento;
import br.uff.ic.grupo6.banco.service.InvestimentoService;
import br.uff.ic.grupo6.banco.service.exception.ValidationException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class InvestimentoServlet extends HttpServlet {

	private final InvestimentoService investimentoService;

	/**
	 * Construtor padrão, usado pelo Tomcat (produção). Inicializa o serviço real.
	 */
	public InvestimentoServlet() {
		this.investimentoService = new InvestimentoService();
	}

	/**
	 * Construtor para testes. Permite a injeção de um mock do serviço.
	 * 
	 * @param investimentoService Uma instância (real ou mock) de
	 *                            InvestimentoService.
	 */
	public InvestimentoServlet(InvestimentoService investimentoService) {
		this.investimentoService = investimentoService;
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession();
		Cliente clienteLogado = (Cliente) session.getAttribute("usuarioLogado");

		if (clienteLogado == null) {
			response.sendRedirect("login.jsp");
			return;
		}

		request.setCharacterEncoding("UTF-8");

		String tipoInvestimento = request.getParameter("tipoInvestimento");
		String valorStr = request.getParameter("valor");
		String urlDestino = "aplicarInvestimento.jsp?tipoInvestimento="
				+ URLEncoder.encode(tipoInvestimento != null ? tipoInvestimento : "", StandardCharsets.UTF_8);

		try {
			double valorInvestimento = Double.parseDouble(valorStr);
			Conta contaCliente = clienteLogado.getConta();

			if (contaCliente == null) {
				throw new ValidationException("Conta não encontrada para o cliente logado.");
			}

			// Chama a camada de serviço para executar a lógica de negócio e validações
			Investimento investimento = this.investimentoService.realizarInvestimento(contaCliente.getId(),
					tipoInvestimento, valorInvestimento, contaCliente.getSaldo());

			// Se o serviço foi executado com sucesso, atualiza a sessão e encaminha
			contaCliente.sacar(valorInvestimento);
			request.setAttribute("comprovante", investimento);
			request.getRequestDispatcher("comprovanteInvestimento.jsp").forward(request, response);

		} catch (NumberFormatException e) {
			String errorMessage = URLEncoder.encode("Valor invalido. Por favor, insira um numero no formato correto.",
					StandardCharsets.UTF_8);
			response.sendRedirect(urlDestino + "&erro=" + errorMessage);

		} catch (ValidationException e) {
			// Erro de regra de negócio (saldo, valor mínimo, etc.)
			String errorMessage = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
			response.sendRedirect(urlDestino + "&erro=" + errorMessage);

		} catch (SQLException e) {
			e.printStackTrace();
			String errorMessage = URLEncoder.encode(
					"Ocorreu um erro ao processar o investimento. Tente novamente mais tarde.", StandardCharsets.UTF_8);
			response.sendRedirect(urlDestino + "&erro=" + errorMessage);
		}
	}
}