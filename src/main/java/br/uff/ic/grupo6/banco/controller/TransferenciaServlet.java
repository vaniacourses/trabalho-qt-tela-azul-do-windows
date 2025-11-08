package br.uff.ic.grupo6.banco.controller;

import br.uff.ic.grupo6.banco.model.Cliente;
import br.uff.ic.grupo6.banco.model.Conta;
import br.uff.ic.grupo6.banco.service.TransacaoService;
import br.uff.ic.grupo6.banco.service.exception.ValidationException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Map;

public class TransferenciaServlet extends HttpServlet {

	private final TransacaoService transacaoService = new TransacaoService();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession sessao = request.getSession();
		Cliente clienteOrigem = (Cliente) sessao.getAttribute("usuarioLogado");

		if (clienteOrigem == null) {
			response.sendRedirect("login.jsp");
			return;
		}

		try {
			// 1. CONTROLLER: Coleta dados
			String agenciaDestino = request.getParameter("agenciaDestino");
			String contaDestino = request.getParameter("contaDestino");
			double valor = Double.parseDouble(request.getParameter("valor"));

			// 2. CONTROLLER: Chama o SERVIÇO
			Map<String, Object> dadosConfirmacao = transacaoService.prepararTransferencia(clienteOrigem, agenciaDestino,
					contaDestino, valor);

			// 3. CONTROLLER: Prepara a VIEW de confirmação
			request.setAttribute("clienteOrigem", dadosConfirmacao.get("clienteOrigem"));
			request.setAttribute("clienteDestino", dadosConfirmacao.get("clienteDestino"));
			request.setAttribute("contaDeDestino", dadosConfirmacao.get("contaDeDestino"));
			request.setAttribute("valor", dadosConfirmacao.get("valor"));

			request.getRequestDispatcher("confirmacaoTransferencia.jsp").forward(request, response);

		} catch (NumberFormatException e) {
			// 3. CONTROLLER: Trata erro de formato
			response.sendRedirect(
					"transferencia.jsp?erro=" + URLEncoder.encode("Valor inválido", StandardCharsets.UTF_8));
		} catch (ValidationException e) {
			// 3. CONTROLLER: Trata erro de validação do SERVIÇO
			response.sendRedirect(
					"transferencia.jsp?erro=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
		} catch (SQLException e) {
			// 3. CONTROLLER: Trata erro de banco do SERVIÇO
			e.printStackTrace();
			response.sendRedirect("dashboard.jsp?erro="
					+ URLEncoder.encode("Ocorreu um erro ao buscar os dados da conta", StandardCharsets.UTF_8));
		}
	}
}