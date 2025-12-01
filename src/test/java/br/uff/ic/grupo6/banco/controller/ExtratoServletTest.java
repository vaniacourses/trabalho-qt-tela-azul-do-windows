package br. uff.ic.grupo6.banco.controller;

import br.uff.ic.grupo6.banco.model.Cliente;
import br.uff. ic.grupo6.banco.model.Conta;
import br. uff.ic.grupo6. banco.model.Transacao;
import br.uff.ic.grupo6.banco.service. TransacaoService;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org. junit.jupiter.api.Test;
import org.junit.jupiter. api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito. MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util. List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension. class)
class ExtratoServletTest {

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
        servlet = new ExtratoServlet();
        
        // Injeta o mock via reflection
        Field field = ExtratoServlet.class. getDeclaredField("transacaoService");
        field.setAccessible(true);
        field. set(servlet, transacaoService);
        
        cliente = mock(Cliente.class);
        conta = mock(Conta.class);
    }

    @Test
    void deveRedirecionarQuandoNaoHaUsuarioLogado() throws Exception {
        when(request.getSession()). thenReturn(session);
        when(session.getAttribute("usuarioLogado")).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).sendRedirect(contains("login.jsp"));
        // OU se quiser verificar a mensagem também:
        verify(response).sendRedirect(argThat(url -> 
            url.contains("login.jsp") && url.contains("Acesso não autorizado")
        ));
    }

    @Test
    void deveBuscarExtratoSemDatas() throws Exception {
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("usuarioLogado")). thenReturn(cliente);
        when(cliente.getConta()).thenReturn(conta);
        when(conta.getId()).thenReturn(10);

        when(request.getParameter("dataInicio")). thenReturn(null);
        when(request.getParameter("dataFim")).thenReturn(null);

        List<Transacao> lista = List.of(new Transacao());
        when(transacaoService.buscarExtrato(10, null, null)). thenReturn(lista);

        when(request.getRequestDispatcher("extrato.jsp")).thenReturn(dispatcher);

        servlet. doGet(request, response);

        verify(transacaoService). buscarExtrato(10, null, null);
        verify(request).setAttribute("listaTransacoes", lista);
        verify(dispatcher).forward(request, response);
    }

    @Test
    void deveBuscarPorPeriodoQuandoDatasValidas() throws Exception {
        when(request.getSession()). thenReturn(session);
        when(session.getAttribute("usuarioLogado")).thenReturn(cliente);
        when(cliente.getConta()).thenReturn(conta);
        when(conta.getId()). thenReturn(10);

        when(request.getParameter("dataInicio")).thenReturn("2024-01-01");
        when(request.getParameter("dataFim")).thenReturn("2024-12-31");

        List<Transacao> lista = List.of(new Transacao());
        when(transacaoService.buscarExtrato(10, LocalDate.parse("2024-01-01"), LocalDate. parse("2024-12-31")))
                .thenReturn(lista);

        when(request. getRequestDispatcher("extrato. jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(transacaoService).buscarExtrato(10,
                LocalDate. parse("2024-01-01"),
                LocalDate.parse("2024-12-31")
        );

        verify(request). setAttribute("listaTransacoes", lista);
        verify(dispatcher). forward(request, response);
    }

    @Test
    void deveRegistrarErroQuandoDataInvalida() throws Exception {
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("usuarioLogado")).thenReturn(cliente);
        when(cliente.getConta()).thenReturn(conta);
        when(conta. getId()).thenReturn(10);

        when(request.getParameter("dataInicio")).thenReturn("2024-99-99");
        when(request.getParameter("dataFim")). thenReturn("2024-12-31");

        when(request.getRequestDispatcher("extrato.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("erro"), contains("Formato de data inválido"));
        verify(dispatcher). forward(request, response);
    }

    @Test
    void deveTratarSQLExceptionQuandoServicoFalhar() throws Exception {
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("usuarioLogado")). thenReturn(cliente);
        when(cliente.getConta()).thenReturn(conta);
        when(conta.getId()).thenReturn(10);

        when(request.getParameter("dataInicio")). thenReturn(null);
        when(request.getParameter("dataFim")).thenReturn(null);

        when(request.getRequestDispatcher("extrato.jsp")).thenReturn(dispatcher);

        doThrow(new SQLException("Erro SQL"))
                .when(transacaoService).buscarExtrato(10, null, null);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("erro"), contains("Ocorreu um erro ao buscar o extrato"));
        verify(dispatcher).forward(request, response);
    }
}