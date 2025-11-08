package br.uff.ic.grupo6.banco.controller;

import br.uff.ic.grupo6.banco.model.Usuario;
import br.uff.ic.grupo6.banco.service.LoginService;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

public class LembrarMeFiltro implements Filter {

	private final LoginService loginService = new LoginService();

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest req = (HttpServletRequest) request;
		HttpSession session = req.getSession(false);

		// Só executa se o usuário AINDA NÃO estiver logado
		if (session == null || session.getAttribute("usuarioLogado") == null) {
			Cookie[] cookies = req.getCookies();
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if ("lembrarMeToken".equals(cookie.getName())) {
						String token = cookie.getValue();
						try {
							// 1. CONTROLLER (Filtro): Chama o SERVIÇO
							Usuario usuario = loginService.buscarUsuarioPorToken(token);

							// 2. CONTROLLER (Filtro): Prepara dados para a VIEW
							if (usuario != null) {
								request.setAttribute("cpfLembrado", usuario.getLogin());
							}
						} catch (SQLException e) {
							e.printStackTrace();
						}
						break; // Já achou o cookie
					}
				}
			}
		}

		chain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void destroy() {
	}
}