package br.uff.ic.grupo6.banco.controller;

import br.uff.ic.grupo6.banco.dao.UsuarioDAO;
import br.uff.ic.grupo6.banco.model.Cliente;
import br.uff.ic.grupo6.banco.model.Conta;
import jakarta.servlet.ServletException;
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
import org.mockito.MockedConstruction;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AtualizarDadosServletTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpSession session;

    private AtualizarDadosServlet servlet;

    @BeforeEach
    void setUp() {
        servlet = new AtualizarDadosServlet();
        Mockito.lenient().when(request.getParameter(anyString())).thenReturn(null);
    }

    @Test
    @DisplayName("Deve redirecionar para login se nao houver sessao")
    void deveRedirecionarParaLoginQuandoNaoHouverSessao() throws ServletException, IOException {
        when(request.getSession(false)).thenReturn(null);

        servlet.doPost(request, response);

        verify(response).sendRedirect("login.jsp");
    }

    @Test
    @DisplayName("Deve redirecionar para login se usuarioLogado nao for Cliente")
    void deveRedirecionarParaLoginQuandoUsuarioNaoForCliente() throws ServletException, IOException {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("usuarioLogado")).thenReturn(new Object());

        servlet.doPost(request, response);

        verify(response).sendRedirect("login.jsp");
    }

    @Test
    @DisplayName("Deve redirecionar com erro se senha atual estiver incorreta")
    void deveRedirecionarComErroQuandoSenhaAtualEstiverIncorreta() throws ServletException, IOException {
        when(request.getSession(false)).thenReturn(session);

        Cliente cliente = new Cliente();
        cliente.setSenha("senhaCerta");
        when(session.getAttribute("usuarioLogado")).thenReturn(cliente);

        when(request.getParameter("senhaAtual")).thenReturn("senhaErrada");
        when(request.getParameter("novaSenha")).thenReturn("nova");
        when(request.getParameter("confirmarNovaSenha")).thenReturn("nova");

        servlet.doPost(request, response);

        verify(response).sendRedirect(contains("A+senha+atual+esta+incorreta"));
    }

    @Test
    @DisplayName("Deve redirecionar com erro se confirmacao de nova senha nao bater")
    void deveRedirecionarComErroQuandoConfirmacaoDeNovaSenhaForDiferente() throws ServletException, IOException {
        when(request.getSession(false)).thenReturn(session);

        Cliente cliente = new Cliente();
        cliente.setSenha("senhaCerta");
        when(session.getAttribute("usuarioLogado")).thenReturn(cliente);

        when(request.getParameter("senhaAtual")).thenReturn("senhaCerta");
        when(request.getParameter("novaSenha")).thenReturn("nova");
        when(request.getParameter("confirmarNovaSenha")).thenReturn("outra");

        servlet.doPost(request, response);

        verify(response).sendRedirect(contains("nova+senha+e+a+confirmacao"));
    }

    @Test
    @DisplayName("Deve redirecionar com erro se UsuarioDAO.atualizarSenha lancar SQLException")
    void deveRedirecionarComErroQuandoUsuarioDAOAtualizarSenhaLancarSQLException() throws Exception {
        when(request.getSession(false)).thenReturn(session);

        Cliente cliente = new Cliente();
        cliente.setId(5);
        cliente.setSenha("senhaCerta");
        when(session.getAttribute("usuarioLogado")).thenReturn(cliente);

        when(request.getParameter("senhaAtual")).thenReturn("senhaCerta");
        when(request.getParameter("novaSenha")).thenReturn("nova");
        when(request.getParameter("confirmarNovaSenha")).thenReturn("nova");
        try (MockedConstruction<UsuarioDAO> mocked = Mockito.mockConstruction(UsuarioDAO.class,
                (mock, context) -> {
                    doThrow(new SQLException("erro simuladopc")).when(mock).atualizarSenha(anyInt(), anyString());
                })) {

            servlet.doPost(request, response);

            verify(response).sendRedirect(contains("Ocorreu+um+erro+ao+tentar+atualizar+a+senha"));
        }
    }

    @Test
    @DisplayName("Deve atualizar dados e senha com sucesso e redirecionar para PerfilServlet")
    void deveAtualizarDadosESenhaComSucessoERedirecionarParaPerfilServlet() throws Exception {
        when(request.getSession(false)).thenReturn(session);

        Cliente cliente = new Cliente();
        cliente.setId(10);
        cliente.setLogin("login");
        cliente.setSenha("senhaCerta");
        Conta conta = new Conta("0001", "123", 100.0);
        cliente.setConta(conta);
        when(session.getAttribute("usuarioLogado")).thenReturn(cliente);
        
        when(request.getParameter("nome")).thenReturn("Nome Teste");
        when(request.getParameter("dataNascimento")).thenReturn("2000-01-01");
        when(request.getParameter("email")).thenReturn("test@example.com");
        when(request.getParameter("telefone")).thenReturn("99999999");
        when(request.getParameter("cep")).thenReturn("00000-000");
        when(request.getParameter("endereco")).thenReturn("Rua Teste");
        when(request.getParameter("bairro")).thenReturn("Bairro");
        when(request.getParameter("cidade")).thenReturn("Cidade");
        when(request.getParameter("estado")).thenReturn("ST");
        when(request.getParameter("renda")).thenReturn("1234.56");
        when(request.getParameter("ocupacao")).thenReturn("Dev");
        
        when(request.getParameter("senhaAtual")).thenReturn("senhaCerta");
        when(request.getParameter("novaSenha")).thenReturn("novaSenha");
        when(request.getParameter("confirmarNovaSenha")).thenReturn("novaSenha");
        try (MockedConstruction<UsuarioDAO> mocked = Mockito.mockConstruction(UsuarioDAO.class,
                (mock, context) -> {
                })) {

            servlet.doPost(request, response);

            UsuarioDAO created = mocked.constructed().get(0);

            verify(created).atualizarSenha(eq(10), eq("novaSenha"));

            ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
            verify(created).atualizarCliente(captor.capture());

            Cliente capturado = captor.getValue();
            assertEquals("Nome Teste", capturado.getNome());
            assertEquals("test@example.com", capturado.getEmail());

            verify(session).setAttribute(eq("usuarioLogado"), any(Cliente.class));

            verify(response).sendRedirect(contains("PerfilServlet?msg="));
        }
    }
}
