package br.uff.ic.grupo6.banco.controller.gerente;

import br.uff.ic.grupo6.banco.model.Cliente;
import br.uff.ic.grupo6.banco.service.ClienteService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class BuscaClientesServlet extends HttpServlet {

	private final ClienteService clienteService = new ClienteService();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// 1. CONTROLLER: Recebe dados da requisição
		String termoBusca = request.getParameter("termoBusca");
		String acao = request.getParameter("acao");

		try {
			// 2. CONTROLLER: Chama a camada de SERVIÇO
			List<Cliente> clientesEncontrados = clienteService.buscarClientes(termoBusca, acao);

			// 3. CONTROLLER: Prepara a VIEW
			request.setAttribute("listaClientes", clientesEncontrados);

		} catch (SQLException e) {
			e.printStackTrace();
			// Tratar erro (ex: setar atributo de erro para a JSP)
		}

		// 4. CONTROLLER: Encaminha para a VIEW
		request.getRequestDispatcher("/gerente/tabelaClientes.jsp").include(request, response);
	}
}