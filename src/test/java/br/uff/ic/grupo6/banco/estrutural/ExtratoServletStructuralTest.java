package br.uff.ic.grupo6.banco.estrutural;

import br.uff.ic.grupo6.banco.controller.ExtratoServlet;
import br.uff.ic.grupo6.banco.model.Cliente;
import br.uff.ic.grupo6.banco.model.Conta;
import br.uff.ic.grupo6.banco.model.Transacao;
import br.uff.ic.grupo6.banco.service.TransacaoService;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;

class ExtratoServletStructuralTest {

    private ExtratoServlet servlet;

    @Mock private TransacaoService transacaoService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    private Cliente cliente;
    private Conta conta;

    @BeforeEach
    void setup() throws Exception {
        MockitoAnnotations.openMocks(this);

        servlet = new ExtratoServlet();

        cliente = mock(Cliente.class);
        conta = mock(Conta.class);

        // Injeção via reflection
        Field field = ExtratoServlet.class.getDeclaredField("transacaoService");
        field.setAccessible(true);
        field.set(servlet, transacaoService);
    }

    // ============================================================
    // 1. Usuário não logado → redirect
    // ============================================================
    @Test
    void estrutural_usuarioNaoLogado() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("usuarioLogado")).thenReturn(null);

        servlet.service(request, response);

        verify(response).sendRedirect("login.jsp?erro=Acesso não autorizado.");
    }

    // ============================================================
    // 2. Datas vazias → fluxo normal
    // ============================================================
    @Test
    void estrutural_datasVazias() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("usuarioLogado")).thenReturn(cliente);

        when(cliente.getConta()).thenReturn(conta);
        when(conta.getId()).thenReturn(10);

        when(request.getParameter("dataInicio")).thenReturn("");
        when(request.getParameter("dataFim")).thenReturn("");

        when(request.getRequestDispatcher("extrato.jsp")).thenReturn(dispatcher);
        when(transacaoService.buscarExtrato(10, null, null)).thenReturn(List.of());

        servlet.service(request, response);

        verify(transacaoService).buscarExtrato(10, null, null);
        verify(dispatcher).forward(request, response);
    }

    // ============================================================
    // 3. Apenas dataInicio preenchida
    // ============================================================
    @Test
    void estrutural_apenasDataInicio() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("usuarioLogado")).thenReturn(cliente);

        when(cliente.getConta()).thenReturn(conta);
        when(conta.getId()).thenReturn(10);

        when(request.getParameter("dataInicio")).thenReturn("2024-02-10");
        when(request.getParameter("dataFim")).thenReturn("");

        LocalDate di = LocalDate.parse("2024-02-10");

        when(request.getRequestDispatcher("extrato.jsp")).thenReturn(dispatcher);
        when(transacaoService.buscarExtrato(10, di, null)).thenReturn(List.of());

        servlet.service(request, response);

        verify(transacaoService).buscarExtrato(10, di, null);
        verify(dispatcher).forward(request, response);
    }

    // ============================================================
    // 4. Apenas dataFim preenchida
    // ============================================================
    @Test
    void estrutural_apenasDataFim() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("usuarioLogado")).thenReturn(cliente);

        when(cliente.getConta()).thenReturn(conta);
        when(conta.getId()).thenReturn(10);

        when(request.getParameter("dataInicio")).thenReturn("");
        when(request.getParameter("dataFim")).thenReturn("2025-01-01");

        LocalDate df = LocalDate.parse("2025-01-01");

        when(request.getRequestDispatcher("extrato.jsp")).thenReturn(dispatcher);
        when(transacaoService.buscarExtrato(10, null, df)).thenReturn(List.of());

        servlet.service(request, response);

        verify(transacaoService).buscarExtrato(10, null, df);
        verify(dispatcher).forward(request, response);
    }

    // ============================================================
    // 5. Data inválida → erro + service ainda é chamado
    // ============================================================
    @Test
    void estrutural_dataInvalida() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("usuarioLogado")).thenReturn(cliente);

        when(cliente.getConta()).thenReturn(conta);
        when(conta.getId()).thenReturn(10);

        when(request.getParameter("dataInicio")).thenReturn("2024-99-99");
        when(request.getParameter("dataFim")).thenReturn("2024-01-01");

        when(request.getRequestDispatcher("extrato.jsp")).thenReturn(dispatcher);

        servlet.service(request, response);

        // erro exibido
        verify(request).setAttribute(eq("erro"), contains("Formato de data inválido"));

        // O service é chamado com dataInicio = null e dataFim = null
        verify(transacaoService).buscarExtrato(eq(10), isNull(), isNull());

        verify(dispatcher).forward(request, response);
    }

    // ============================================================
    // 6. SQLException lançado pelo service
    // ============================================================
    @Test
    void estrutural_sqlException() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("usuarioLogado")).thenReturn(cliente);

        when(cliente.getConta()).thenReturn(conta);
        when(conta.getId()).thenReturn(10);

        when(request.getParameter("dataInicio")).thenReturn("");
        when(request.getParameter("dataFim")).thenReturn("");

        when(request.getRequestDispatcher("extrato.jsp")).thenReturn(dispatcher);

        doThrow(new SQLException("Erro SQL"))
                .when(transacaoService).buscarExtrato(10, null, null);

        servlet.service(request, response);

        verify(request).setAttribute(eq("erro"), contains("Ocorreu um erro ao buscar o extrato"));
        verify(dispatcher).forward(request, response);
    }

    // ============================================================
    // 7. Datas válidas → caminho principal (arestas finais)
    // ============================================================
    @Test
    void estrutural_datasValidas() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("usuarioLogado")).thenReturn(cliente);

        when(cliente.getConta()).thenReturn(conta);
        when(conta.getId()).thenReturn(10);

        when(request.getParameter("dataInicio")).thenReturn("2024-01-01");
        when(request.getParameter("dataFim")).thenReturn("2024-02-01");

        LocalDate di = LocalDate.parse("2024-01-01");
        LocalDate df = LocalDate.parse("2024-02-01");

        when(request.getRequestDispatcher("extrato.jsp")).thenReturn(dispatcher);
        when(transacaoService.buscarExtrato(10, di, df)).thenReturn(List.of());

        servlet.service(request, response);

        verify(transacaoService).buscarExtrato(10, di, df);
        verify(dispatcher).forward(request, response);
    }
}
