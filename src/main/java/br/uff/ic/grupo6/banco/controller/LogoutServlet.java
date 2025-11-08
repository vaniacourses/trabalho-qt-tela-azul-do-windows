package br.uff.ic.grupo6.banco.controller;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Pega a sessão atual, se existir
        HttpSession sessao = request.getSession(false);

        // 2. Se a sessão existir, a invalida
        if (sessao != null) {
            sessao.invalidate();
        }

        // 3. Redireciona o usuário de volta para a página de login
        response.sendRedirect("login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // O logout pode ser chamado via GET
        doGet(request, response);
    }
}
