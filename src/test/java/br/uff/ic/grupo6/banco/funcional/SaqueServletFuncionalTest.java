package br.uff.ic.grupo6.banco.funcional;

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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes funcionais (caixa-preta) para a funcionalidade de Realizar Saque via Servlet.
 * Foco: Classes de Equivalência (Valores válidos, inválidos, limites) e Regras de Negócio.
 */
@ExtendWith(MockitoExtension.class)
class SaqueServletFuncionalTest {

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
    void setUp() {
        // Injeção do Mock no Controller
        saqueServlet = new SaqueServlet(transacaoService);

        // Cenário base: Cliente logado com saldo suficiente
        contaCliente = new Conta("0001", "12345-6");
        contaCliente.setId(10);
        contaCliente.setSaldo(5000.0);

        clienteLogado = new Cliente();
        clienteLogado.setNome("Maria Funcional");
        clienteLogado.setConta(contaCliente);
    }

    @Test
    @DisplayName("Classe Válida: Saque dentro das regras (R$ 100,00)")
    void deveProcessarSaqueValido() throws Exception {
        // Arrange
        configurarSessaoUsuario();
        when(request.getParameter("valor")).thenReturn("100.00");
        when(request.getRequestDispatcher("comprovanteSaque.jsp")).thenReturn(dispatcher);
        
        Transacao transacaoSucesso = new Transacao();
        when(transacaoService.realizarSaque(any(Conta.class), eq(100.00))).thenReturn(transacaoSucesso);

        // Act
        saqueServlet.doPost(request, response);

        // Assert
        verify(request).setAttribute("comprovante", transacaoSucesso);
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Entrada Inválida: Valor não numérico (ex: 'abc')")
    void deveRejeitarValorNaoNumerico() throws Exception {
        // Arrange
        configurarSessaoUsuario();
        when(request.getParameter("valor")).thenReturn("abc");

        // Act
        saqueServlet.doPost(request, response);

        // Assert
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(captor.capture());
        assertTrue(captor.getValue().contains("Valor+inv%C3%A1lido"), "Deve redirecionar com erro de formato");
    }

    @Test
    @DisplayName("Regra de Negócio: Saldo Insuficiente")
    void deveRejeitarSaqueAcimaDoSaldo() throws Exception {
        // Arrange
        configurarSessaoUsuario();
        double valorSaque = 6000.00; // Saldo é 5000
        when(request.getParameter("valor")).thenReturn(String.valueOf(valorSaque));

        // Simulamos o comportamento do Service validando a regra
        Mockito.doThrow(new ValidationException("Saldo insuficiente para realizar o saque."))
                .when(transacaoService).realizarSaque(any(Conta.class), eq(valorSaque));

        // Act
        saqueServlet.doPost(request, response);

        // Assert
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(captor.capture());
        assertTrue(captor.getValue().contains("Saldo+insuficiente"), "URL deve conter mensagem de saldo insuficiente");
    }

    @Test
    @DisplayName("Valor Limite: Saque máximo permitido (R$ 2.000,00)")
    void deveAceitarLimiteMaximoTransacao() throws Exception {
        // Arrange
        configurarSessaoUsuario();
        double valorLimite = 2000.00;
        when(request.getParameter("valor")).thenReturn("2000.00");
        when(request.getRequestDispatcher("comprovanteSaque.jsp")).thenReturn(dispatcher);

        when(transacaoService.realizarSaque(any(Conta.class), eq(valorLimite))).thenReturn(new Transacao());

        // Act
        saqueServlet.doPost(request, response);

        // Assert
        verify(transacaoService).realizarSaque(contaCliente, valorLimite);
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Valor Limite Excedido: Saque acima de R$ 2.000,00")
    void deveRejeitarAcimaDoLimiteTransacao() throws Exception {
        // Arrange
        configurarSessaoUsuario();
        double valorAcima = 2010.00; // Apenas R$ 10 acima do limite
        when(request.getParameter("valor")).thenReturn("2010.00");

        Mockito.doThrow(new ValidationException("O valor do saque excede o limite"))
                .when(transacaoService).realizarSaque(any(Conta.class), eq(valorAcima));

        // Act
        saqueServlet.doPost(request, response);

        // Assert
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(captor.capture());
        assertTrue(captor.getValue().contains("excede+o+limite"), "Deve informar erro de limite excedido");
    }

    @Test
    @DisplayName("Regra de Negócio: Saque Negativo ou Zero")
    void deveRejeitarValorNegativo() throws Exception {
        // Arrange
        configurarSessaoUsuario();
        when(request.getParameter("valor")).thenReturn("-50.00");

        Mockito.doThrow(new ValidationException("O valor do saque deve ser positivo"))
                .when(transacaoService).realizarSaque(any(Conta.class), eq(-50.00));

        // Act
        saqueServlet.doPost(request, response);

        // Assert
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(captor.capture());
        assertTrue(captor.getValue().contains("deve+ser+positivo"), "Deve rejeitar valores negativos");
    }

    @Test
    @DisplayName("Segurança: Tentativa de acesso sem Login")
    void deveBloquearAcessoSemLogin() throws Exception {
        // Arrange
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("usuarioLogado")).thenReturn(null); // Simula usuário não logado

        // Act
        saqueServlet.doPost(request, response);

        // Assert
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(captor.capture());
        assertTrue(captor.getValue().contains("login.jsp"), "Deve redirecionar para página de login");
        assertTrue(captor.getValue().contains("Acesso+n%C3%A3o+autorizado"), "Deve conter mensagem de autorização");
        
        Mockito.verifyNoInteractions(transacaoService);
    }

    // Método auxiliar para configurar o comportamento padrão da sessão
    private void configurarSessaoUsuario() {
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("usuarioLogado")).thenReturn(clienteLogado);
    }
}