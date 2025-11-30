package br.uff.ic.grupo6.banco.integracao;
import java.lang.reflect.Field;



import br.uff.ic.grupo6.banco.controller.ExtratoServlet;
import br.uff.ic.grupo6.banco.model.Cliente;
import br.uff.ic.grupo6.banco.model.Conta;
import br.uff.ic.grupo6.banco.model.Transacao;
import br.uff.ic.grupo6.banco.service.TransacaoService;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ExtratoServletIntegracaoTeste {

    @InjectMocks
    private ExtratoServlet servlet;

    @Mock
    private TransacaoService transacaoService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private RequestDispatcher dispatcher;

    private Cliente cliente;
    private Conta conta;

    @BeforeEach
    void setup() throws Exception {
        MockitoAnnotations.openMocks(this);

        cliente = new Cliente();
        conta = new Conta(null, null);
        conta.setId(10);
        cliente.setConta(conta);

        when(request.getSession()).thenReturn(session);

        // 🔥 força o servlet a usar o mock TransacaoService
        Field f = ExtratoServlet.class.getDeclaredField("transacaoService");
        f.setAccessible(true);
        f.set(servlet, transacaoService);
    }

    @Test
    void deveIntegrarServletComServiceEEncaminharParaView() throws ServletException, IOException, SQLException {
        when(session.getAttribute("usuarioLogado")).thenReturn(cliente);
        when(request.getParameter("dataInicio")).thenReturn(null);
        when(request.getParameter("dataFim")).thenReturn(null);

        List<Transacao> lista = List.of(new Transacao());
        when(transacaoService.buscarExtrato(10, null, null)).thenReturn(lista);

        when(request.getRequestDispatcher("extrato.jsp")).thenReturn(dispatcher);

        when(request.getMethod()).thenReturn("GET");
        servlet.service(request, response);

        // Integração servlet → service
        verify(transacaoService).buscarExtrato(10, null, null);

        // Integração servlet → view
        verify(request).setAttribute("listaTransacoes", lista);
        verify(dispatcher).forward(request, response);
    }

    @Test
    void deveRetornarErroQuandoUsuarioNaoLogado() throws ServletException, IOException {
        when(session.getAttribute("usuarioLogado")).thenReturn(null);

        when(request.getMethod()).thenReturn("GET");
        servlet.service(request, response);

        verify(response).sendRedirect("login.jsp?erro=Acesso não autorizado.");
    }

    @Test
    void deveDetectarDataInvalida() throws ServletException, IOException {
        when(session.getAttribute("usuarioLogado")).thenReturn(cliente);

        when(request.getParameter("dataInicio")).thenReturn("2024-99-99");
        when(request.getParameter("dataFim")).thenReturn(null);

        when(request.getRequestDispatcher("extrato.jsp")).thenReturn(dispatcher);

        when(request.getMethod()).thenReturn("GET");
        servlet.service(request, response);

        String erro = (String) request.getAttribute("erro");
        assertTrue(erro.contains("Formato de data inválido"));

        verify(dispatcher).forward(request, response);
    }

    @Test
    void deveTratarErroDoServico() throws Exception {
        when(session.getAttribute("usuarioLogado")).thenReturn(cliente);

        when(request.getParameter("dataInicio")).thenReturn(null);
        when(request.getParameter("dataFim")).thenReturn(null);

        when(request.getRequestDispatcher("extrato.jsp")).thenReturn(dispatcher);

        doThrow(new RuntimeException("Falha no banco")).when(transacaoService)
                .buscarExtrato(10, null, null);

        when(request.getMethod()).thenReturn("GET");
        servlet.service(request, response);

        String erro = (String) request.getAttribute("erro");
        assertTrue(erro.contains("Ocorreu um erro ao buscar o extrato"));

        verify(dispatcher).forward(request, response);
    }
}
