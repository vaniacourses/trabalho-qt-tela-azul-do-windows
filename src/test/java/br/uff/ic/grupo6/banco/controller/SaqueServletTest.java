package br.uff.ic.grupo6.banco.controller;

import br.uff.ic.grupo6.banco.dao.ContaDAO;
import br.uff.ic.grupo6.banco.model.Cliente;
import br.uff.ic.grupo6.banco.model.Conta;
import br.uff.ic.grupo6.banco.model.Transacao;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.sql.SQLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaqueServletTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpSession session;
    @Mock
    private RequestDispatcher dispatcher;

    private SaqueServlet servlet;
    private Cliente clienteLogado;
    private Conta contaCliente;

    @BeforeEach
    void setUp() {
        servlet = new SaqueServlet();
        
        when(request.getSession()).thenReturn(session);
        Mockito.lenient().when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
        Mockito.lenient().when(request.getParameter(anyString())).thenReturn(null);

        contaCliente = new Conta("1", "1", 5000); // Saldo inicial alto para cobrir os testes
        contaCliente.setId(1);
        clienteLogado = new Cliente("cliente1", "123456789", "Cliente Teste", "57086723070");
        clienteLogado.setConta(contaCliente); 
    }

    @Test
    @DisplayName("Deve redirecionar com erro quando o usuário não está logado")
    void deveRedirecionarComErroQuandoUsuarioNaoEstaLogado() throws ServletException, IOException {
        when(session.getAttribute("usuarioLogado")).thenReturn(null);
        servlet.doPost(request, response);
        String expectedErrorUrl = "login.jsp?erro=" + URLEncoder.encode("Acesso não autorizado. Faça login como cliente.", StandardCharsets.UTF_8);
        verify(response).sendRedirect(expectedErrorUrl);
    }

    @Test
    @DisplayName("Deve redirecionar com erro quando o valor do saque é negativo")
    void deveRedirecionarComErroQuandoValorDoSaqueEInvalidoOuNegativo() throws ServletException, IOException {
        when(session.getAttribute("usuarioLogado")).thenReturn(clienteLogado);
        when(request.getParameter("valor")).thenReturn("-100.0");
        servlet.doPost(request, response);
        String expectedErrorUrl = "saque.jsp?erro=" + URLEncoder.encode("O valor do saque deve ser positivo.", StandardCharsets.UTF_8);
        verify(response).sendRedirect(expectedErrorUrl);
    }

    @Test
    @DisplayName("Deve redirecionar com erro quando o valor do saque não é numérico")
    void deveRedirecionarComErroQuandoValorDoSaqueNaoENumerico() throws ServletException, IOException {
        when(session.getAttribute("usuarioLogado")).thenReturn(clienteLogado);
        when(request.getParameter("valor")).thenReturn("valor_invalido");
        servlet.doPost(request, response);
        String expectedErrorUrl = "saque.jsp?erro=" + URLEncoder.encode("Valor inválido. Por favor, insira um número no formato correto.", StandardCharsets.UTF_8);
        verify(response).sendRedirect(expectedErrorUrl);
    }

    @Test
    @DisplayName("Deve redirecionar com erro quando o cliente não possui conta associada")
    void deveRedirecionarComErroQuandoClienteNaoPossuiConta() throws ServletException, IOException {
        clienteLogado.setConta(null);
        when(session.getAttribute("usuarioLogado")).thenReturn(clienteLogado);
        when(request.getParameter("valor")).thenReturn("100.0");
        servlet.doPost(request, response);
        String expectedErrorUrl = "saque.jsp?erro=" + URLEncoder.encode("Conta não encontrada para o cliente logado.", StandardCharsets.UTF_8);
        verify(response).sendRedirect(expectedErrorUrl);
    }

    @Test
    @DisplayName("Deve redirecionar com erro quando o saldo é insuficiente")
    void deveRedirecionarComErroQuandoSaldoEInsuficiente() throws ServletException, IOException {
        contaCliente.setSaldo(100.0);
        when(session.getAttribute("usuarioLogado")).thenReturn(clienteLogado);
        when(request.getParameter("valor")).thenReturn("200.0");
        servlet.doPost(request, response);
        String expectedErrorUrl = "saque.jsp?erro=" + URLEncoder.encode("Saldo insuficiente para realizar o saque.", StandardCharsets.UTF_8);
        verify(response).sendRedirect(expectedErrorUrl);
    }

    @Test
    @DisplayName("Deve redirecionar com erro quando o DAO lança uma SQLException")
    void deveRedirecionarComErroQuandoDAOLancaSQLException() throws ServletException, IOException, SQLException {
        when(session.getAttribute("usuarioLogado")).thenReturn(clienteLogado);
        when(request.getParameter("valor")).thenReturn("100.0");

        try (MockedConstruction<ContaDAO> mockedDAO = Mockito.mockConstruction(ContaDAO.class,
                (mock, context) -> {
                    when(mock.realizarSaque(anyInt(), anyDouble())).thenThrow(new SQLException("Erro simulado no banco"));
                })) {
            
            servlet.doPost(request, response);
            
            String expectedErrorUrl = "saque.jsp?erro=" + URLEncoder.encode("Ocorreu um erro ao processar o saque. Tente novamente mais tarde.", StandardCharsets.UTF_8);
            verify(response).sendRedirect(expectedErrorUrl);
        }
    }

    @Test
    @DisplayName("Deve realizar o saque com sucesso e encaminhar para o comprovante")
    void deveRealizarSaqueComSucessoQuandoDadosSaoValidos() throws ServletException, IOException, SQLException {
        when(session.getAttribute("usuarioLogado")).thenReturn(clienteLogado);
        when(request.getParameter("valor")).thenReturn("200.0");

        Transacao transacaoMock = new Transacao(); 

        try (MockedConstruction<ContaDAO> mockedDAO = Mockito.mockConstruction(ContaDAO.class,
                (mock, context) -> {
                    when(mock.realizarSaque(1, 200.0)).thenReturn(transacaoMock);
                })) {
            
            servlet.doPost(request, response);

            ContaDAO daoInstance = mockedDAO.constructed().get(0);
            verify(daoInstance).realizarSaque(1, 200.0); 
            
            // O saldo inicial é 5000, sacando 200, o esperado é 4800
            assertEquals(4800.0, contaCliente.getSaldo(), "O saldo do objeto na sessão deve ser atualizado.");
            
            verify(request).setAttribute("comprovante", transacaoMock);
            verify(dispatcher).forward(request, response);
        }
    }

    // NOVO TESTE 1: Validar o limite máximo de saque
    @Test
    @DisplayName("Deve redirecionar com erro quando o valor do saque excede o limite máximo")
    void deveRedirecionarComErroQuandoValorDoSaqueExcedeLimiteMaximo() throws ServletException, IOException {
        // Arrange
        when(session.getAttribute("usuarioLogado")).thenReturn(clienteLogado);
        when(request.getParameter("valor")).thenReturn("2500.0"); // Valor acima do limite de 2000

        // Act
        servlet.doPost(request, response);

        // Assert
        String expectedErrorUrl = "saque.jsp?erro=" + URLEncoder.encode("O valor do saque excede o limite de R$ 2.000,00 por transação.", StandardCharsets.UTF_8);
        verify(response).sendRedirect(expectedErrorUrl);
    }

    // NOVO TESTE 2: Validar se o valor é múltiplo de 10
    @Test
    @DisplayName("Deve redirecionar com erro quando o valor do saque não é múltiplo de R$ 10,00")
    void deveRedirecionarComErroQuandoValorNaoForMultiploDeDez() throws ServletException, IOException {
        // Arrange
        when(session.getAttribute("usuarioLogado")).thenReturn(clienteLogado);
        when(request.getParameter("valor")).thenReturn("125.0"); // Valor não múltiplo de 10

        // Act
        servlet.doPost(request, response);

        // Assert
        String expectedErrorUrl = "saque.jsp?erro=" + URLEncoder.encode("O valor do saque deve ser em múltiplos de R$ 10,00.", StandardCharsets.UTF_8);
        verify(response).sendRedirect(expectedErrorUrl);
    }
    
    @Test
    @DisplayName("Deve redirecionar com erro quando o usuário logado não é um Cliente")
    void deveRedirecionarComErroQuandoUsuarioLogadoNaoECliente() throws ServletException, IOException {
        // Arrange: Coloca um objeto genérico na sessão, que não é instância de Cliente
        when(session.getAttribute("usuarioLogado")).thenReturn(new Object());

        // Act
        servlet.doPost(request, response);

        // Assert: Garante que o redirecionamento de "não autorizado" foi acionado
        String expectedErrorUrl = "login.jsp?erro=" + URLEncoder.encode("Acesso não autorizado. Faça login como cliente.", StandardCharsets.UTF_8);
        verify(response).sendRedirect(expectedErrorUrl);
    }
    
}