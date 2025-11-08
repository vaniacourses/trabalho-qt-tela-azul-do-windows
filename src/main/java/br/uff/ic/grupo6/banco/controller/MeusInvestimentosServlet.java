package br.uff.ic.grupo6.banco.controller;

import br.uff.ic.grupo6.banco.model.Cliente;
import br.uff.ic.grupo6.banco.model.Investimento;
import br.uff.ic.grupo6.banco.service.InvestimentoService;
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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class MeusInvestimentosServlet extends HttpServlet {

	private final InvestimentoService investimentoService = new InvestimentoService();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		Cliente clienteLogado = (Cliente) session.getAttribute("usuarioLogado");

		if (clienteLogado == null) {
			response.sendRedirect("login.jsp?erro=Acesso não autorizado.");
			return;
		}

		request.setCharacterEncoding("UTF-8");

		// 1. CONTROLLER: Coleta dados
		String dataInicioStr = request.getParameter("dataInicio");
		String dataFimStr = request.getParameter("dataFim");
		LocalDate dataInicio = null;
		LocalDate dataFim = null;

		try {
			if (dataInicioStr != null && !dataInicioStr.isEmpty()) {
				dataInicio = LocalDate.parse(dataInicioStr);
			}
			if (dataFimStr != null && !dataFimStr.isEmpty()) {
				dataFim = LocalDate.parse(dataFimStr);
			}

			// 2. CONTROLLER: Chama o SERVIÇO
			List<Investimento> listaInvestimentos = investimentoService
					.buscarInvestimentos(clienteLogado.getConta().getId(), dataInicio, dataFim);

			// 3. CONTROLLER: Prepara a VIEW
			request.setAttribute("listaInvestimentos", listaInvestimentos);

		} catch (DateTimeParseException e) {
			// 3. CONTROLLER: Trata erro de formato
			request.setAttribute("erro", "Formato de data inválido. Use AAAA-MM-DD.");
		} catch (ValidationException e) {
			// 3. CONTROLLER: Trata erro de validação do SERVIÇO
			request.setAttribute("erro", e.getMessage());
		} catch (SQLException e) {
			// 3. CONTROLLER: Trata erro de banco do SERVIÇO
			e.printStackTrace();
			request.setAttribute("erro", "Ocorreu um erro ao buscar o histórico de investimentos.");
		}

		// 4. CONTROLLER: Encaminha para a VIEW
		request.setAttribute("dataInicio", dataInicioStr);
		request.setAttribute("dataFim", dataFimStr);
		request.getRequestDispatcher("meusInvestimentos.jsp").forward(request, response);
	}
}