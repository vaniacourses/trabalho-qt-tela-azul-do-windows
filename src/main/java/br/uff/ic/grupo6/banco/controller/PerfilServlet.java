package br.uff.ic.grupo6.banco.controller;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class PerfilServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Este servlet apenas encaminha a requisição para a página JSP de perfil
        // A página JSP será responsável por verificar a sessão e exibir os dados
        request.getRequestDispatcher("perfil.jsp").forward(request, response);
    }
}
