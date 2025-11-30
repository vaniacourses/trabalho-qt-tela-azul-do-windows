package br.uff.ic.grupo6.banco.mutacao;

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
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;

class ExtratoServletMutationTest {

    private ExtratoServlet servlet;

    @Mock private TransacaoService service;
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

        // injeta o mock via reflexão
        Field f = ExtratoServlet.class.getDeclaredField("transacaoService");
        f.setAccessible(true);
        f.set(servlet, service);
    }

    // ============================================================
    // 1. Mutação: remover redirecionamento
    // ============================================================
    @Test
    void mutacao_usuarioNaoLogado() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("usuarioLogado")).thenReturn(null);

        servlet.service(request, response);

        verify(response).sendRedirect("login.jsp?erro=Acesso não autorizado.");
        verifyNoMoreInteractions(service);
    }

    // ============================================================
    // 2. Mutação: remover chamada ao service
    // ============================================================
    @Test
    void mutacao_datasVazias_chamaService() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("usuarioLogado")).thenReturn(cliente);

        when(cliente.getConta()).thenReturn(conta);
        when(conta.getId()).thenReturn(10);

        when(request.getParameter("dataInicio")).thenReturn("");
        when(request.getParameter("dataFim")).thenReturn("");

        when(request.getRequestDispatcher("extrato.jsp")).thenReturn(dispatcher);

        List<Transacao> lista = List.of(new Transacao());
        when(service.buscarExtrato(10, null, null)).thenReturn(lista);

        servlet.service(request, response);

        InOrder ordem = inOrder(service, dispatcher);

        ordem.verify(service).buscarExtrato(10, null, null);
        ordem.verify(dispatcher).forward(request, response);

        verify(request).setAttribute("listaTransacoes", lista);
    }

    // ============================================================
    // 3. Mutação: alterar valor de datas
    // ============================================================
    @Test
    void mutacao_datasValidas_chamadaCorreta() throws Exception {
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
        when(service.buscarExtrato(10, di, df)).thenReturn(List.of());

        servlet.service(request, response);

        verify(service).buscarExtrato(10, di, df);
        verify(dispatcher).forward(request, response);
    }

    // ============================================================
    // 4. Mutação: remover tratamento da data inválida
    // ============================================================
    @Test
    void mutacao_dataInvalida_deveGerarErroEChamarService() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("usuarioLogado")).thenReturn(cliente);

        when(cliente.getConta()).thenReturn(conta);
        when(conta.getId()).thenReturn(10);

        when(request.getParameter("dataInicio")).thenReturn("2024-99-99");
        when(request.getParameter("dataFim")).thenReturn("2024-01-01");

        when(request.getRequestDispatcher("extrato.jsp")).thenReturn(dispatcher);

        servlet.service(request, response);

        verify(request).setAttribute(eq("erro"), contains("Formato de data inválido"));
        
        // Dados após mutação interna
        verify(service).buscarExtrato(eq(10), isNull(), isNull());
        verify(dispatcher).forward(request, response);
    }

    // ============================================================
    // 5. Mutação: remover catch de SQLException
    // ============================================================
    @Test
    void mutacao_sqlException_capturada() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("usuarioLogado")).thenReturn(cliente);

        when(cliente.getConta()).thenReturn(conta);
        when(conta.getId()).thenReturn(10);

        when(request.getParameter("dataInicio")).thenReturn("");
        when(request.getParameter("dataFim")).thenReturn("");

        when(request.getRequestDispatcher("extrato.jsp")).thenReturn(dispatcher);

        doThrow(new SQLException("Erro SQL"))
                .when(service).buscarExtrato(10, null, null);

        servlet.service(request, response);

        verify(request)
                .setAttribute(eq("erro"), contains("Ocorreu um erro ao buscar o extrato"));
        verify(dispatcher).forward(request, response);
    }

}
