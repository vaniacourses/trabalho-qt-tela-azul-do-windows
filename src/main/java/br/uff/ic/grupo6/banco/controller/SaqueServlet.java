package br.uff.ic.grupo6.banco.controller;

import br.uff.ic.grupo6.banco.model.Cliente;
import br.uff.ic.grupo6.banco.model.Conta;
import br.uff.ic.grupo6.banco.model.Transacao;
import br.uff.ic.grupo6.banco.service.TransacaoService;
import br.uff.ic.grupo6.banco.service.exception.ValidationException;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class SaqueServlet extends HttpServlet {

    private final TransacaoService transacaoService;

    // Construtor padrão exigido pelo container (Tomcat/Jetty)
    public SaqueServlet() {
        this.transacaoService = new TransacaoService();
    }

    // Construtor para injeção de dependência em TESTES (Public conforme solicitado)
    public SaqueServlet(TransacaoService transacaoService) {
        this.transacaoService = transacaoService;
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Cliente clienteLogado = (Cliente) session.getAttribute("usuarioLogado");

        // Aresta 1: Cliente não logado
        if (clienteLogado == null) {
            String msg = URLEncoder.encode("Acesso não autorizado. Faça login como cliente.", StandardCharsets.UTF_8);
            response.sendRedirect("login.jsp?erro=" + msg);
            return;
        }

        request.setCharacterEncoding("UTF-8");

        try {
            // 1. CONTROLLER: Coleta dados
            String valorStr = request.getParameter("valor");
            double valorSaque = Double.parseDouble(valorStr);
            Conta contaCliente = clienteLogado.getConta();

            // Aresta 2: Cliente sem conta vinculada
            if (contaCliente == null) {
                String msg = URLEncoder.encode("Conta não encontrada para o cliente logado.", StandardCharsets.UTF_8);
                response.sendRedirect("saque.jsp?erro=" + msg);
                return;
            }

            // 2. CONTROLLER: Chama o SERVIÇO
            Transacao transacaoSaque = transacaoService.realizarSaque(contaCliente, valorSaque);

            // 3. CONTROLLER: Atualiza sessão e encaminha para VIEW
            contaCliente.sacar(valorSaque); // Atualiza o objeto em memória na sessão
            request.setAttribute("comprovante", transacaoSaque);
            
            RequestDispatcher dispatcher = request.getRequestDispatcher("comprovanteSaque.jsp");
            dispatcher.forward(request, response);

        } catch (NumberFormatException e) {
            // Aresta 3: Erro de formato numérico
            String msg = URLEncoder.encode("Valor inválido. Por favor, insira um número no formato correto.",
                    StandardCharsets.UTF_8);
            response.sendRedirect("saque.jsp?erro=" + msg);
        } catch (ValidationException e) {
            // Aresta 4: Erro de validação de negócio
            String msg = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            response.sendRedirect("saque.jsp?erro=" + msg);
        } catch (SQLException e) {
            // Aresta 5: Erro de banco de dados
            e.printStackTrace();
            String msg = URLEncoder.encode("Ocorreu um erro ao processar o saque. Tente novamente mais tarde.",
                    StandardCharsets.UTF_8);
            response.sendRedirect("saque.jsp?erro=" + msg);
        }
    }
}