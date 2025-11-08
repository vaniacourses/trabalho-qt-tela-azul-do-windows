package br.uff.ic.grupo6.banco.controller;

import br.uff.ic.grupo6.banco.model.Cliente;
import br.uff.ic.grupo6.banco.model.Conta;
import br.uff.ic.grupo6.banco.service.ClienteService;
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

public class AtualizarDadosServlet extends HttpServlet {

	private final ClienteService clienteService = new ClienteService();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		request.setCharacterEncoding("UTF-8");
		HttpSession sessao = request.getSession(false);

		if (sessao == null || !(sessao.getAttribute("usuarioLogado") instanceof Cliente)) {
			response.sendRedirect("login.jsp");
			return;
		}

		Cliente clienteDaSessao = (Cliente) sessao.getAttribute("usuarioLogado");
		int idCliente = clienteDaSessao.getId();
		String mensagemSucesso = "Dados atualizados com sucesso!";

		// 1. CONTROLLER: Coleta campos de senha
		String senhaAtual = request.getParameter("senhaAtual");
		String novaSenha = request.getParameter("novaSenha");
		String confirmarNovaSenha = request.getParameter("confirmarNovaSenha");

		// 2. CONTROLLER: Chama o SERVIÇO para lógica de senha
		if (novaSenha != null && !novaSenha.isEmpty()) {
			try {
				clienteService.atualizarSenha(idCliente, senhaAtual, novaSenha, confirmarNovaSenha,
						clienteDaSessao.getSenha());

				// Atualiza também o objeto na sessão
				clienteDaSessao.setSenha(novaSenha);
				mensagemSucesso += " Sua senha tambem foi alterada.";

			} catch (ValidationException e) {
				// 3. CONTROLLER: Trata erro de validação do SERVIÇO
				String msg = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
				response.sendRedirect("DadosCadastraisServlet?erro=" + msg);
				return;
			} catch (SQLException e) {
				// 3. CONTROLLER: Trata erro de banco do SERVIÇO
				e.printStackTrace();
				String msg = "Ocorreu um erro ao tentar atualizar a senha.";
				response.sendRedirect("DadosCadastraisServlet?erro=" + URLEncoder.encode(msg, StandardCharsets.UTF_8));
				return;
			}
		}

		// 1. CONTROLLER: Coleta os outros dados do formulário
		Cliente clienteParaAtualizar = new Cliente(clienteDaSessao.getLogin(), clienteDaSessao.getSenha(),
				request.getParameter("nome"), clienteDaSessao.getCpf());
		clienteParaAtualizar.setId(idCliente);
		clienteParaAtualizar.setDataNascimento(LocalDate.parse(request.getParameter("dataNascimento")));
		clienteParaAtualizar.setEmail(request.getParameter("email"));
		clienteParaAtualizar.setTelefone(request.getParameter("telefone"));
		clienteParaAtualizar.setCep(request.getParameter("cep"));
		clienteParaAtualizar.setEndereco(request.getParameter("endereco"));
		clienteParaAtualizar.setBairro(request.getParameter("bairro"));
		clienteParaAtualizar.setCidade(request.getParameter("cidade"));
		clienteParaAtualizar.setEstado(request.getParameter("estado"));
		clienteParaAtualizar.setRenda(Double.parseDouble(request.getParameter("renda")));
		clienteParaAtualizar.setOcupacao(request.getParameter("ocupacao"));
		clienteParaAtualizar.setConta(clienteDaSessao.getConta()); // Mantém a conta

		// 2. CONTROLLER: Chama o SERVIÇO para atualizar dados
		try {
			clienteService.atualizarCliente(clienteParaAtualizar);

			// 3. CONTROLLER: Atualiza sessão e redireciona para a VIEW
			sessao.setAttribute("usuarioLogado", clienteParaAtualizar);
			response.sendRedirect("PerfilServlet?msg=" + URLEncoder.encode(mensagemSucesso, StandardCharsets.UTF_8));

		} catch (SQLException e) {
			// 3. CONTROLLER: Trata erro de banco do SERVIÇO
			e.printStackTrace();
			String msg = "Nao foi possivel atualizar os dados. Tente novamente.";
			response.sendRedirect("DadosCadastraisServlet?erro=" + URLEncoder.encode(msg, StandardCharsets.UTF_8));
		}
	}
}