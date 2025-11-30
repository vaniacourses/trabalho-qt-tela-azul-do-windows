package br.uff.ic.grupo6.banco.funcional;

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

class ExtratoServletFunctionalTest {

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

        cliente = mock(Cliente.class);
        conta = mock(Conta.class);

        // Instancia o servlet REAL
        servlet = new ExtratoServlet();

        // INJETA O MOCK transacaoService NO SERVLET VIA REFLECTION
        Field field = ExtratoServlet.class.getDeclaredField("transacaoService");
        field.setAccessible(true);
        field.set(servlet, transacaoService);
    }

    /**
     * CT-01 — Sem login → redirecionar
     */
    @Test
    void deveRedirecionarQuandoNaoLogado() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("usuarioLogado")).thenReturn(null);

        servlet.service(request, response);

        verify(response).sendRedirect("login.jsp?erro=Acesso não autorizado.");
    }

    /**
     * CT-02 — Extrato completo (sem datas)
     */
    @Test
    void deveConsultarExtratoCompletoSemDatas() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("usuarioLogado")).thenReturn(cliente);

        when(cliente.getConta()).thenReturn(conta);
        when(conta.getId()).thenReturn(10);

        when(request.getParameter("dataInicio")).thenReturn("");
        when(request.getParameter("dataFim")).thenReturn("");

        List<Transacao> lista = List.of(new Transacao());

        when(transacaoService.buscarExtrato(10, null, null)).thenReturn(lista);
        when(request.getRequestDispatcher("extrato.jsp")).thenReturn(dispatcher);

        servlet.service(request, response);

        verify(transacaoService).buscarExtrato(10, null, null);
        verify(request).setAttribute("listaTransacoes", lista);
        verify(dispatcher).forward(request, response);
    }

    /**
     * CT-03 — Datas válidas
     */
    @Test
    void deveConsultarComPeriodoValido() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("usuarioLogado")).thenReturn(cliente);

        when(cliente.getConta()).thenReturn(conta);
        when(conta.getId()).thenReturn(10);

        when(request.getParameter("dataInicio")).thenReturn("2024-01-01");
        when(request.getParameter("dataFim")).thenReturn("2024-12-31");

        List<Transacao> lista = List.of(new Transacao());

        when(transacaoService.buscarExtrato(
                10,
                LocalDate.parse("2024-01-01"),
                LocalDate.parse("2024-12-31")
        )).thenReturn(lista);

        when(request.getRequestDispatcher("extrato.jsp")).thenReturn(dispatcher);

        servlet.service(request, response);

        verify(transacaoService).buscarExtrato(
                10,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        );

        verify(request).setAttribute("listaTransacoes", lista);
        verify(dispatcher).forward(request, response);
    }

    /**
     * CT-04 — Data inválida
     */
    @Test
    void deveMostrarErroParaDataInvalida() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("usuarioLogado")).thenReturn(cliente);

        when(cliente.getConta()).thenReturn(conta);
        when(conta.getId()).thenReturn(10);

        when(request.getParameter("dataInicio")).thenReturn("2024-99-99");
        when(request.getParameter("dataFim")).thenReturn("2024-12-31");

        when(request.getRequestDispatcher("extrato.jsp")).thenReturn(dispatcher);

        servlet.service(request, response);

        verify(request).setAttribute(eq("erro"), contains("Formato de data inválido"));
        verify(dispatcher).forward(request, response);
    }

    /**
     * CT-05 — SQLException
     */
    @Test
    void deveTratarSQLException() throws Exception {
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
}
