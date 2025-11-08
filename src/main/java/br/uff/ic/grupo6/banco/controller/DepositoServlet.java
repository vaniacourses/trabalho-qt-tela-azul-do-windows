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

public class DepositoServlet extends HttpServlet {

	private final TransacaoService transacaoService = new TransacaoService();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession sessao = request.getSession();
		Cliente cliente = (Cliente) sessao.getAttribute("usuarioLogado");

		if (cliente == null) {
			response.sendRedirect("login.jsp");
			return;
		}

		try {
			// 1. CONTROLLER: Recebe dados
			double valor = Double.parseDouble(request.getParameter("valor"));
			Conta contaDoCliente = cliente.getConta();

			// 2. CONTROLLER: Chama o SERVIÇO
			Transacao transacao = transacaoService.realizarDeposito(contaDoCliente.getId(), valor);

			// 3. CONTROLLER: Atualiza dados da sessão e encaminha para VIEW
			contaDoCliente.depositar(valor); // Atualiza o objeto na sessão
			request.setAttribute("comprovante", transacao);
			request.getRequestDispatcher("comprovanteDeposito.jsp").forward(request, response);

		} catch (NumberFormatException e) {
			// 3. CONTROLLER: Trata erro de formato
			String msg = "Valor inválido. Use apenas números e ponto ou vírgula.";
			response.sendRedirect("deposito.jsp?erro=" + URLEncoder.encode(msg, StandardCharsets.UTF_8));
		} catch (ValidationException e) {
			// 3. CONTROLLER: Trata erro de validação do SERVIÇO
			response.sendRedirect("deposito.jsp?erro=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
		} catch (SQLException e) {
			// 3. CONTROLLER: Trata erro de banco do SERVIÇO
			e.printStackTrace();
			String msg = "Ocorreu um erro ao processar o depósito.";
			response.sendRedirect("dashboard.jsp?erro=" + URLEncoder.encode(msg, StandardCharsets.UTF_8));
		}
	}
}