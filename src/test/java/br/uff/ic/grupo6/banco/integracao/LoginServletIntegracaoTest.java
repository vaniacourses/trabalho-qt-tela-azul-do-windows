package br.uff.ic.grupo6.banco.integracao;

import br.uff.ic.grupo6.banco.controller.LoginServlet;
import br.uff.ic.grupo6.banco.model.Usuario;
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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes de Integração para o LoginServlet, verificando o fluxo completo
 * da requisição HTTP até a camada de persistência (MySQL).
 *
 * NOTA: Para estes testes funcionarem, o banco de dados MySQL deve estar
 * rodando e populado com os dados necessários (Pré-Condição).
 */
@ExtendWith(MockitoExtension.class)
class LoginServletIntegracaoTest {

    private LoginServlet loginServlet;

    // Mocks do ambiente HTTP
    @Mock
    private HttpServletRequest requestMock;
    @Mock
    private HttpServletResponse responseMock;
    @Mock
    private HttpSession sessionMock;
    @Mock
    private RequestDispatcher dispatcherMock;

    // Constantes do Usuário de Teste (devem existir no banco de dados)
    private static final String LOGIN_CPF_OK = "00000000000";
    private static final String SENHA_OK = "admin";
    private static final String SENHA_ERRADA = "senhaErrada123";

    /**
     * Configuração inicial antes de cada teste.
     * Inicializa o Servlet.
     */
    @BeforeEach
    void setup() {
        loginServlet = new LoginServlet();
        
        Mockito.lenient().when(requestMock.getSession()).thenReturn(sessionMock);

        // Configuração Leniente do RequestDispatcher para evitar NPEs e UnnecessaryStubbing.
        // Garante que requestMock.getRequestDispatcher() nunca retorne null.
        Mockito.lenient().when(requestMock.getRequestDispatcher(anyString())).thenReturn(dispatcherMock);
    }

    /*
     * TI-08: Teste de Sucesso de Login.
     * Deve autenticar o usuário no banco de dados e redirecionar para o dashboard.
     */
    @Test
    @DisplayName("TI-08: Login com credenciais válidas e sucesso no redirecionamento")
    void ti08_loginSucessoRedirecionamento() throws ServletException, IOException {
        // 1. Ação Executada: Configurar a requisição com credenciais válidas
        when(requestMock.getParameter("login")).thenReturn(LOGIN_CPF_OK);
        when(requestMock.getParameter("senha")).thenReturn(SENHA_OK);
        
        // Simular o caso sem "Lembrar-me" para simplificar
        when(requestMock.getParameter("lembrar")).thenReturn(null);

        // Chamada do Servlet (inicia o fluxo de integração)
        loginServlet.doPost(requestMock, responseMock);

        // 2. Pós-Condição (Verificação):
        
        // Deve armazenar o objeto Usuario na sessão
        verify(sessionMock).setAttribute(eq("usuarioLogado"), Mockito.any(Usuario.class));
        
        // Deve redirecionar para a página correta (assumindo que "admin" é um Gerente)
        verify(responseMock).sendRedirect("gerente/dashboard.jsp");
        
        // Não deve ocorrer forward nem setAttribute("erro")
        verify(requestMock, Mockito.never()).setAttribute("erro", anyString());
        // Se o teste falhar por outro motivo e cair no forward, ele será ignorado aqui.
        verify(dispatcherMock, Mockito.never()).forward(requestMock, responseMock); 
    }

    /*
     * TI-09: Teste de Falha de Login (Senha Incorreta).
     * Deve falhar na autenticação, definir uma mensagem de erro e fazer forward para login.jsp.
     */
    @Test
    @DisplayName("TI-09: Login com senha inválida, falha e forward para login.jsp")
    void ti09_loginFalhaForwardErro() throws ServletException, IOException {
        // 1. Ação Executada: Configurar a requisição com senha incorreta
        when(requestMock.getParameter("login")).thenReturn(LOGIN_CPF_OK);
        when(requestMock.getParameter("senha")).thenReturn(SENHA_ERRADA);
        when(requestMock.getParameter("lembrar")).thenReturn(null);
        
        // O stubbing do RequestDispatcher já está no setup()

        // Chamada do Servlet (inicia o fluxo de integração)
        loginServlet.doPost(requestMock, responseMock);

        // 2. Pós-Condição (Verificação):
        
        // Deve definir a mensagem de erro (LoginException) na requisição
        verify(requestMock).setAttribute(eq("erro"), anyString());
        
        // Deve obter o RequestDispatcher para login.jsp
        verify(requestMock).getRequestDispatcher("login.jsp");
        
        // Deve fazer o forward para a página de login
        verify(dispatcherMock).forward(requestMock, responseMock);

        // Não deve armazenar nada na sessão nem redirecionar
        verify(sessionMock, Mockito.never()).setAttribute(eq("usuarioLogado"), Mockito.any());
        verify(responseMock, Mockito.never()).sendRedirect(anyString());
    }
}