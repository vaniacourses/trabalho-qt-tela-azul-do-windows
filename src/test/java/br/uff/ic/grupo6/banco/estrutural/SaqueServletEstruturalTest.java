package br.uff.ic.grupo6.banco.estrutural;

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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes estruturais (caixa-branca) cobrindo todos os fluxos da SaqueServlet.
 */
@ExtendWith(MockitoExtension.class)
class SaqueServletEstruturalTest {

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

    private SaqueServlet saqueServlet;
    private Cliente clienteLogado;
    private Conta contaCliente;

    @BeforeEach
    void init() {
        // Injeção manual via construtor (possibilitada pela refatoração)
        saqueServlet = new SaqueServlet(transacaoService);

        // Setup básico dos dados
        contaCliente = new Conta("0001", "12345-6");
        contaCliente.setId(1);
        contaCliente.setSaldo(1000.0); // Saldo inicial

        clienteLogado = new Cliente();
        clienteLogado.setNome("João Silva");
        clienteLogado.setConta(contaCliente);
    }

    @Test
    @DisplayName("Aresta 1: Usuário não logado (Redireciona para login)")
    void usuarioNaoLogado() throws Exception {
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("usuarioLogado")).thenReturn(null);

        saqueServlet.doPost(request, response);

        // Verifica o redirecionamento com mensagem de erro
        String expectedMsg = URLEncoder.encode("Acesso não autorizado. Faça login como cliente.", StandardCharsets.UTF_8);
        verify(response).sendRedirect("login.jsp?erro=" + expectedMsg);
        
        // Garante que o serviço NUNCA foi chamado
        Mockito.verifyNoInteractions(transacaoService);
    }

    @Test
    @DisplayName("Aresta 2: Erro de formato no valor (NumberFormatException)")
    void valorInvalidoFormato() throws Exception {
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("usuarioLogado")).thenReturn(clienteLogado);
        when(request.getParameter("valor")).thenReturn("dez reais"); // Valor não numérico

        saqueServlet.doPost(request, response);

        String expectedMsg = URLEncoder.encode("Valor inválido. Por favor, insira um número no formato correto.", StandardCharsets.UTF_8);
        verify(response).sendRedirect("saque.jsp?erro=" + expectedMsg);
        
        Mockito.verifyNoInteractions(transacaoService);
    }

    @Test
    @DisplayName("Aresta 3: Cliente logado mas sem Conta vinculada")
    void contaNaoEncontrada() throws Exception {
        clienteLogado.setConta(null); // Remove a conta do cliente

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("usuarioLogado")).thenReturn(clienteLogado);
        when(request.getParameter("valor")).thenReturn("100.00");

        saqueServlet.doPost(request, response);

        String expectedMsg = URLEncoder.encode("Conta não encontrada para o cliente logado.", StandardCharsets.UTF_8);
        verify(response).sendRedirect("saque.jsp?erro=" + expectedMsg);
        
        Mockito.verifyNoInteractions(transacaoService);
    }

    @Test
    @DisplayName("Aresta 4: ValidationException (Regra de Negócio do Service falhou)")
    void erroValidacaoService() throws Exception {
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("usuarioLogado")).thenReturn(clienteLogado);
        when(request.getParameter("valor")).thenReturn("5000.00"); // Valor acima do limite, por exemplo

        // Simula o serviço lançando erro (ex: saldo insuficiente ou limite excedido)
        String erroNegocio = "Saldo insuficiente para realizar o saque.";
        Mockito.doThrow(new ValidationException(erroNegocio))
                .when(transacaoService).realizarSaque(any(Conta.class), eq(5000.00));

        saqueServlet.doPost(request, response);

        String expectedMsg = URLEncoder.encode(erroNegocio, StandardCharsets.UTF_8);
        verify(response).sendRedirect("saque.jsp?erro=" + expectedMsg);
        
        // Garante que o saldo do objeto em memória NÃO foi alterado
        assertEquals(1000.0, contaCliente.getSaldo());
    }

    @Test
    @DisplayName("Aresta 5: SQLException (Erro de Banco)")
    void erroBancoDados() throws Exception {
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("usuarioLogado")).thenReturn(clienteLogado);
        when(request.getParameter("valor")).thenReturn("100.00");

        // Simula erro de conexão SQL
        Mockito.doThrow(new SQLException("Erro de conexão"))
                .when(transacaoService).realizarSaque(any(Conta.class), anyDouble());

        saqueServlet.doPost(request, response);

        String expectedMsg = URLEncoder.encode("Ocorreu um erro ao processar o saque. Tente novamente mais tarde.", StandardCharsets.UTF_8);
        verify(response).sendRedirect("saque.jsp?erro=" + expectedMsg);
    }

    @Test
    @DisplayName("Caminho Feliz: Saque realizado com sucesso")
    void saqueComSucesso() throws Exception {
        // Setup dos mocks para o caminho feliz
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("usuarioLogado")).thenReturn(clienteLogado);
        when(request.getParameter("valor")).thenReturn("100.00");
        when(request.getRequestDispatcher("comprovanteSaque.jsp")).thenReturn(dispatcher);

        Transacao transacaoSucesso = new Transacao(); // Objeto fictício de retorno
        when(transacaoService.realizarSaque(contaCliente, 100.00)).thenReturn(transacaoSucesso);

        // Executa
        saqueServlet.doPost(request, response);

        // --- Verificações (Asserts) ---
        
        // 1. Verifica se o serviço foi chamado corretamente
        verify(transacaoService).realizarSaque(contaCliente, 100.00);

        // 2. Verifica se a sessão foi atualizada (efeito colateral em contaCliente)
        // O saldo era 1000, sacou 100, deve ser 900.
        assertEquals(900.0, contaCliente.getSaldo(), "O saldo do objeto em memória deve ser atualizado");

        // 3. Verifica se o atributo 'comprovante' foi setado no request
        verify(request).setAttribute("comprovante", transacaoSucesso);

        // 4. Verifica se houve o forward para a página correta
        verify(request).getRequestDispatcher("comprovanteSaque.jsp");
        verify(dispatcher).forward(request, response);
    }
}