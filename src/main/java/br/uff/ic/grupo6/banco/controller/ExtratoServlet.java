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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class ExtratoServlet extends HttpServlet {

	private final TransacaoService transacaoService = new TransacaoService();

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		Cliente clienteLogado = (Cliente) session.getAttribute("usuarioLogado");

		if (clienteLogado == null) {
			response.sendRedirect("login.jsp?erro=Acesso não autorizado.");
			return;
		}

		// 1. CONTROLLER: Recebe dados
		int idConta = clienteLogado.getConta().getId();
		LocalDate dataInicio = null;
		LocalDate dataFim = null;
		String dataInicioStr = request.getParameter("dataInicio");
		String dataFimStr = request.getParameter("dataFim");

		try {
			if (dataInicioStr != null && !dataInicioStr.isEmpty()) {
				dataInicio = LocalDate.parse(dataInicioStr);
			}
			if (dataFimStr != null && !dataFimStr.isEmpty()) {
				dataFim = LocalDate.parse(dataFimStr);
			}
		} catch (DateTimeParseException e) {
			request.setAttribute("erro", "Formato de data inválido. Use AAAA-MM-DD.");
		}

		// 2. CONTROLLER: Chama o SERVIÇO
		try {
			List<Transacao> transacoes = transacaoService.buscarExtrato(idConta, dataInicio, dataFim);

			// 3. CONTROLLER: Prepara a VIEW
			request.setAttribute("listaTransacoes", transacoes);

		} catch (SQLException e) {
			// 3. CONTROLLER: Trata erro do SERVIÇO
			e.printStackTrace();
			request.setAttribute("erro", "Ocorreu um erro ao buscar o extrato: " + e.getMessage());
		}

		// 4. CONTROLLER: Encaminha para a VIEW
		request.setAttribute("dataInicio", dataInicioStr);
		request.setAttribute("dataFim", dataFimStr);
		request.getRequestDispatcher("extrato.jsp").forward(request, response);
	}
}