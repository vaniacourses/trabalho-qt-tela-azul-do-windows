package br.uff.ic.grupo6.banco.controller;

import br.uff.ic.grupo6.banco.model.Cliente;
import br.uff.ic.grupo6.banco.service.ClienteService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

public class DadosCadastraisServlet extends HttpServlet {

	private final ClienteService clienteService = new ClienteService();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession sessao = request.getSession(false);
		if (sessao == null || !(sessao.getAttribute("usuarioLogado") instanceof Cliente)) {
			response.sendRedirect("login.jsp");
			return;
		}

		// 1. CONTROLLER: Pega dados da sessão
		Cliente clienteDaSessao = (Cliente) sessao.getAttribute("usuarioLogado");

		try {
			// 2. CONTROLLER: Chama o SERVIÇO para buscar dados atualizados
			Cliente clienteAtualizado = clienteService.buscarClientePorCpf(clienteDaSessao.getCpf());

			// 3. CONTROLLER: Prepara e encaminha para a VIEW
			if (clienteAtualizado != null) {
				request.setAttribute("cliente", clienteAtualizado);
				request.getRequestDispatcher("dadosCadastrais.jsp").forward(request, response);
			} else {
				response.sendRedirect("login.jsp?msg=Usuário não encontrado.");
			}
		} catch (SQLException e) {
			// 3. CONTROLLER: Trata erro do SERVIÇO
			e.printStackTrace();
			response.sendRedirect("dashboard.jsp");
		}
	}
}