package br.uff.ic.grupo6.banco.controller;

import br.uff.ic.grupo6.banco.model.Cliente;
import br.uff.ic.grupo6.banco.service.ClienteService;
import br.uff.ic.grupo6.banco.service.exception.ValidationException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class CadastroServlet extends HttpServlet {

	private final ClienteService clienteService = new ClienteService();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		request.setCharacterEncoding("UTF-8");

		// 1. CONTROLLER: Coleta dados do formulário
		String confirmaSenha = request.getParameter("confirmaSenha");
		Cliente novoCliente = new Cliente();

		try {
			novoCliente.setNome(request.getParameter("nome"));
			novoCliente.setCpf(request.getParameter("cpf"));
			novoCliente.setLogin(request.getParameter("cpf")); // Login é o CPF
			novoCliente.setSenha(request.getParameter("senha"));
			novoCliente.setDataNascimento(LocalDate.parse(request.getParameter("dataNascimento")));
			novoCliente.setEmail(request.getParameter("email"));
			novoCliente.setTelefone(request.getParameter("telefone"));
			novoCliente.setCep(request.getParameter("cep"));
			novoCliente.setEndereco(request.getParameter("endereco"));
			novoCliente.setBairro(request.getParameter("bairro"));
			novoCliente.setCidade(request.getParameter("cidade"));
			novoCliente.setEstado(request.getParameter("estado"));
			novoCliente.setRenda(Double.parseDouble(request.getParameter("renda")));
			novoCliente.setOcupacao(request.getParameter("ocupacao"));

		} catch (DateTimeParseException | NumberFormatException e) {
			// 3. CONTROLLER: Trata erro de formato
			request.setAttribute("mensagem", "Erro: Formato de data ou renda inválido.");
			request.getRequestDispatcher("cadastro.jsp").forward(request, response);
			return;
		}

		// 2. CONTROLLER: Chama o SERVIÇO
		try {
			clienteService.cadastrarNovoCliente(novoCliente, confirmaSenha);

			// 3. CONTROLLER: Redireciona para a VIEW com sucesso
			String mensagemSucesso = "Cadastro realizado com sucesso! Faça seu login.";
			String mensagemCodificada = URLEncoder.encode(mensagemSucesso, StandardCharsets.UTF_8.toString());
			response.sendRedirect("login.jsp?msg=" + mensagemCodificada);

		} catch (ValidationException e) {
			// 3. CONTROLLER: Trata erro de validação do SERVIÇO
			request.setAttribute("mensagem", e.getMessage());
			request.getRequestDispatcher("cadastro.jsp").forward(request, response);
		} catch (SQLException e) {
			// 3. CONTROLLER: Trata erro de banco do SERVIÇO
			e.printStackTrace();
			request.setAttribute("mensagem", "Erro ao salvar os dados no banco. Tente novamente.");
			request.getRequestDispatcher("cadastro.jsp").forward(request, response);
		}
	}
}