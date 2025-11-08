package br.uff.ic.grupo6.banco.controller.gerente;

import br.uff.ic.grupo6.banco.service.ClienteService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

public class ExcluirClienteServlet extends HttpServlet {

	private final ClienteService clienteService = new ClienteService();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			// 1. CONTROLLER: Obtém o ID
			int idCliente = Integer.parseInt(request.getParameter("id"));

			// 2. CONTROLLER: Chama o SERVIÇO
			clienteService.excluirCliente(idCliente);

			// 3. CONTROLLER: Redireciona para a VIEW com sucesso
			response.sendRedirect(request.getContextPath()
					+ "/gerente/dashboard.jsp?acao=listarTodos&msg=Cliente+excluido+com+sucesso!");

		} catch (NumberFormatException e) {
			// 3. CONTROLLER: Trata erro de formato e redireciona
			response.sendRedirect(request.getContextPath() + "/gerente/dashboard.jsp?erro=ID+invalido");
		} catch (SQLException e) {
			// 3. CONTROLLER: Trata erro do SERVIÇO e redireciona
			e.printStackTrace();
			response.sendRedirect(request.getContextPath() + "/gerente/dashboard.jsp?erro=Erro+ao+excluir+cliente");
		}
	}
}