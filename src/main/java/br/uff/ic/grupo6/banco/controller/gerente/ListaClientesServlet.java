package br.uff.ic.grupo6.banco.controller.gerente;

import br.uff.ic.grupo6.banco.model.Cliente;
import br.uff.ic.grupo6.banco.model.Gerente;
import br.uff.ic.grupo6.banco.service.ClienteService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ListaClientesServlet extends HttpServlet {

	private final ClienteService clienteService = new ClienteService();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Segurança
		if (!(request.getSession().getAttribute("usuarioLogado") instanceof Gerente)) {
			response.sendRedirect("../login.jsp?erro=Acesso Negado");
			return;
		}

		try {
			// 1. CONTROLLER: Chama o SERVIÇO
			List<Cliente> listaClientes = clienteService.listarTodosClientes();

			// 2. CONTROLLER: Prepara a VIEW
			request.setAttribute("listaClientes", listaClientes);

		} catch (SQLException e) {
			// 2. CONTROLLER: Trata erro do SERVIÇO
			e.printStackTrace();
			request.setAttribute("erro", "Erro ao carregar a lista de clientes.");
		}

		// 3. CONTROLLER: Encaminha para a VIEW
		request.getRequestDispatcher("/gerente/tabelaClientes.jsp").include(request, response);
	}
}