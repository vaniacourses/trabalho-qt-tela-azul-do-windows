package br.uff.ic.grupo6.banco.controller;

import br.uff.ic.grupo6.banco.model.Gerente;
import br.uff.ic.grupo6.banco.model.Usuario;
import br.uff.ic.grupo6.banco.service.LoginService;
import br.uff.ic.grupo6.banco.service.exception.LoginException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

public class LoginServlet extends HttpServlet {

	private final LoginService loginService = new LoginService();

	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// 1. CONTROLLER: Pega dados
		String cpf = request.getParameter("login");
		String senha = request.getParameter("senha");
		String lembrar = request.getParameter("lembrar");

		try {
			// 2. CONTROLLER: Chama o SERVIÇO de autenticação
			Usuario usuario = loginService.autenticar(cpf, senha);

			// 3. CONTROLLER: Lógica de SESSÃO
			HttpSession sessao = request.getSession();
			sessao.setAttribute("usuarioLogado", usuario);

			// 4. CONTROLLER: Lógica de "Lembrar-me" (Cookies)
			if (lembrar != null) {
				// Chama o SERVIÇO para gerar e salvar o token
				String token = loginService.gerarTokenLembrarMe(usuario.getId());

				// Cria o cookie
				Cookie cookieLembrarMe = new Cookie("lembrarMeToken", token);
				cookieLembrarMe.setMaxAge(30 * 24 * 60 * 60); // 30 dias
				cookieLembrarMe.setPath(request.getContextPath());
				response.addCookie(cookieLembrarMe);
			} else {
				// Chama o SERVIÇO para limpar o token
				loginService.limparTokenLembrarMe(usuario.getId());

				// Cria um cookie para expirar
				Cookie cookieLembrarMe = new Cookie("lembrarMeToken", "");
				cookieLembrarMe.setMaxAge(0);
				cookieLembrarMe.setPath(request.getContextPath());
				response.addCookie(cookieLembrarMe);
			}

			// 5. CONTROLLER: Redireciona para a VIEW correta
			if (usuario instanceof Gerente) {
				response.sendRedirect("gerente/dashboard.jsp");
			} else {
				response.sendRedirect("dashboard.jsp");
			}

		} catch (LoginException e) {
			// 3. CONTROLLER: Trata erro de validação do SERVIÇO
			request.setAttribute("erro", e.getMessage());
			request.getRequestDispatcher("login.jsp").forward(request, response);
		} catch (SQLException e) {
			// 3. CONTROLLER: Trata erro de banco do SERVIÇO
			e.printStackTrace();
			request.setAttribute("erro", "Ocorreu um erro inesperado. Tente novamente mais tarde.");
			request.getRequestDispatcher("login.jsp").forward(request, response);
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processRequest(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processRequest(request, response);
	}
}