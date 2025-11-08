package br.uff.ic.grupo6.banco.controller;

import br.uff.ic.grupo6.banco.model.Cliente;
import br.uff.ic.grupo6.banco.model.Transacao;
import br.uff.ic.grupo6.banco.service.TransacaoService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class FinalizarTransferenciaServlet extends HttpServlet {

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
			// 1. CONTROLLER: Coleta dados
			int idContaOrigem = Integer.parseInt(request.getParameter("idContaOrigem"));
			int idContaDestino = Integer.parseInt(request.getParameter("idContaDestino"));
			double valor = Double.parseDouble(request.getParameter("valor"));

			// Dados para o comprovante
			String nomeDestino = request.getParameter("nomeDestino");
			String cpfDestino = request.getParameter("cpfDestino");
			String agenciaDestino = request.getParameter("agenciaDestino");
			String numeroContaDestino = request.getParameter("numeroContaDestino");

			// 2. CONTROLLER: Chama o SERVIÇO
			List<Transacao> transacoes = transacaoService.realizarTransferencia(idContaOrigem, idContaDestino, valor);

			// 3. CONTROLLER: Atualiza sessão
			cliente.getConta().sacar(valor);

			// 4. CONTROLLER: Prepara dados para a VIEW (comprovante)
			Transacao comprovanteRemetente = null;
			for (Transacao t : transacoes) {
				if (t.getIdConta() == idContaOrigem) {
					comprovanteRemetente = t;
					break;
				}
			}

			request.setAttribute("comprovante", comprovanteRemetente);
			request.setAttribute("nomeDestino", nomeDestino);
			request.setAttribute("cpfDestino", cpfDestino);
			request.setAttribute("agenciaDestino", agenciaDestino);
			request.setAttribute("numeroContaDestino", numeroContaDestino);

			// 5. CONTROLLER: Encaminha para a VIEW
			request.getRequestDispatcher("comprovanteTransferencia.jsp").forward(request, response);

		} catch (NumberFormatException | SQLException e) {
			// 3. CONTROLLER: Trata erro
			e.printStackTrace();
			response.sendRedirect("dashboard.jsp?erro=Ocorreu+um+erro+ao+finalizar+a+transferencia");
		}
	}
}