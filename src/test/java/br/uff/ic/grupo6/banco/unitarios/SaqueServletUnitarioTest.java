package br.uff.ic.grupo6.banco.unitarios;

import br.uff.ic.grupo6.banco.controller.SaqueServlet;
import br.uff.ic.grupo6.banco.model.Cliente;
import br.uff.ic.grupo6.banco.model.Conta;
import br.uff.ic.grupo6.banco.model.Transacao;
import br.uff.ic.grupo6.banco.service.TransacaoService;
import br.uff.ic.grupo6.banco.service.exception.ValidationException;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SaqueServletUnitarioTest {

    @Mock private TransacaoService transacaoService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    // SUT (System Under Test)
    private SaqueServlet saqueServlet;

    private Cliente clienteLogado;
    private Conta contaCliente;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // Injeção de dependência via construtor (conforme refatoração anterior)
        saqueServlet = new SaqueServlet(transacaoService);

        // Massa de dados padrão
        contaCliente = new Conta("0001", "12345");
        contaCliente.setId(1);
        contaCliente.setSaldo(1000.0);

        clienteLogado = new Cliente();
        clienteLogado.setNome("Cliente Teste");
        clienteLogado.setConta(contaCliente);
    }

    private void mockSessaoValida() {
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("usuarioLogado")).thenReturn(clienteLogado);
    }

    // ============================================================
    //                         TESTES DE SUCESSO
    // ============================================================

    @Test
    @DisplayName("Deve realizar saque com sucesso e encaminhar para comprovante")
    void deveRealizarSaqueComSucesso() throws Exception {
        mockSessaoValida();
        when(request.getParameter("valor")).thenReturn("100.00");
        when(request.getRequestDispatcher("comprovanteSaque.jsp")).thenReturn(dispatcher);

        Transacao transacaoSucesso = new Transacao();
        when(transacaoService.realizarSaque(any(Conta.class), eq(100.00)))
                .thenReturn(transacaoSucesso);

        saqueServlet.doPost(request, response);

        // Verifica chamada ao serviço
        verify(transacaoService).realizarSaque(contaCliente, 100.00);

        // Verifica atualização do modelo em memória (sessão)
        assertEquals(900.0, contaCliente.getSaldo(), "Saldo na sessão deve ser atualizado");

        // Verifica setAttribute e forward
        verify(request).setAttribute("comprovante", transacaoSucesso);
        verify(dispatcher).forward(request, response);
    }

    // ============================================================
    //                  TESTES DE VALIDAÇÃO DE SESSÃO
    // ============================================================

    @Test
    @DisplayName("Deve redirecionar para login se usuário não estiver logado")
    void deveRedirecionarQuandoUsuarioNaoLogado() throws Exception {
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("usuarioLogado")).thenReturn(null);

        saqueServlet.doPost(request, response);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(captor.capture());
        
        String url = captor.getValue();
        assertTrue(url.contains("login.jsp"));
        assertTrue(url.contains("Acesso+n%C3%A3o+autorizado")); // URL Encoded
        
        verifyNoInteractions(transacaoService);
    }

    @Test
    @DisplayName("Deve redirecionar com erro se cliente não tiver conta")
    void deveRedirecionarQuandoContaNaoEncontrada() throws Exception {
        clienteLogado.setConta(null); // Cliente sem conta
        mockSessaoValida();
        when(request.getParameter("valor")).thenReturn("100.00");

        saqueServlet.doPost(request, response);

        verify(response).sendRedirect(argThat(url -> url.contains("Conta+n%C3%A3o+encontrada")));
        verifyNoInteractions(transacaoService);
    }

    // ============================================================
    //                  TESTES DE ERROS DE ENTRADA
    // ============================================================

    @Test
    @DisplayName("Deve tratar erro de formatação numérica")
    void deveTratarNumberFormatException() throws Exception {
        mockSessaoValida();
        when(request.getParameter("valor")).thenReturn("abc"); // Valor inválido

        saqueServlet.doPost(request, response);

        verify(response).sendRedirect(argThat(url -> url.contains("Valor+inv%C3%A1lido")));
        verifyNoInteractions(transacaoService);
    }

    // ============================================================
    //               TESTES DE EXCEÇÕES DO SERVIÇO
    // ============================================================

    @Test
    @DisplayName("Deve tratar ValidationException do serviço")
    void deveTratarValidationException() throws Exception {
        mockSessaoValida();
        when(request.getParameter("valor")).thenReturn("5000.00");

        String msgErro = "Saldo insuficiente";
        doThrow(new ValidationException(msgErro))
                .when(transacaoService).realizarSaque(any(Conta.class), anyDouble());

        saqueServlet.doPost(request, response);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(captor.capture());
        
        // Verifica se a mensagem da exceção foi passada na URL
        assertTrue(captor.getValue().contains(URLEncoder.encode(msgErro, StandardCharsets.UTF_8)));
    }

    @Test
    @DisplayName("Deve tratar SQLException do serviço")
    void deveTratarSQLException() throws Exception {
        mockSessaoValida();
        when(request.getParameter("valor")).thenReturn("100.00");

        doThrow(new SQLException("Erro de conexão"))
                .when(transacaoService).realizarSaque(any(Conta.class), anyDouble());

        saqueServlet.doPost(request, response);

        verify(response).sendRedirect(argThat(url -> url.contains("Ocorreu+um+erro+ao+processar")));
    }
}