package br.uff.ic.grupo6.banco.controller;

import br.uff.ic.grupo6.banco.model.Cliente;
import br.uff.ic.grupo6.banco.model.Conta;
import br.uff.ic.grupo6.banco.model.Transacao;
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

public class SaqueServlet extends HttpServlet {

	private final TransacaoService transacaoService = new TransacaoService();

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		Cliente clienteLogado = (Cliente) session.getAttribute("usuarioLogado");

		if (clienteLogado == null) {
			String msg = URLEncoder.encode("Acesso não autorizado. Faça login como cliente.", StandardCharsets.UTF_8);
			response.sendRedirect("login.jsp?erro=" + msg);
			return;
		}

		request.setCharacterEncoding("UTF-8");

		try {
			// 1. CONTROLLER: Coleta dados
			String valorStr = request.getParameter("valor");
			double valorSaque = Double.parseDouble(valorStr);
			Conta contaCliente = clienteLogado.getConta();

			if (contaCliente == null) {
				String msg = URLEncoder.encode("Conta não encontrada para o cliente logado.", StandardCharsets.UTF_8);
				response.sendRedirect("saque.jsp?erro=" + msg);
				return;
			}

			// 2. CONTROLLER: Chama o SERVIÇO
			// Atualizado para passar o objeto Conta inteiro, em vez de id e saldo
			Transacao transacaoSaque = transacaoService.realizarSaque(contaCliente, valorSaque);

			// 3. CONTROLLER: Atualiza sessão e encaminha para VIEW
			contaCliente.sacar(valorSaque);
			request.setAttribute("comprovante", transacaoSaque);
			request.getRequestDispatcher("comprovanteSaque.jsp").forward(request, response);

		} catch (NumberFormatException e) {
			// 3. CONTROLLER: Trata erro de formato
			String msg = URLEncoder.encode("Valor inválido. Por favor, insira um número no formato correto.",
					StandardCharsets.UTF_8);
			response.sendRedirect("saque.jsp?erro=" + msg);
		} catch (ValidationException e) {
			// 3. CONTROLLER: Trata erro de validação do SERVIÇO
			String msg = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
			response.sendRedirect("saque.jsp?erro=" + msg);
		} catch (SQLException e) {
			// 3. CONTROLLER: Trata erro de banco do SERVIÇO
			e.printStackTrace();
			String msg = URLEncoder.encode("Ocorreu um erro ao processar o saque. Tente novamente mais tarde.",
					StandardCharsets.UTF_8);
			response.sendRedirect("saque.jsp?erro=" + msg);
		}
	}
}