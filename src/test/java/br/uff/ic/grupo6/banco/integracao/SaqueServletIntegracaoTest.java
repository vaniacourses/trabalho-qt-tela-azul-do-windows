package br.uff.ic.grupo6.banco.integracao;

import br.uff.ic.grupo6.banco.controller.SaqueServlet;
import br.uff.ic.grupo6.banco.dao.ContaDAO;
import br.uff.ic.grupo6.banco.dao.TransacaoDAO;
import br.uff.ic.grupo6.banco.dao.UsuarioDAO;
import br.uff.ic.grupo6.banco.model.Cliente;
import br.uff.ic.grupo6.banco.model.Conta;
import br.uff.ic.grupo6.banco.model.Transacao;
import br.uff.ic.grupo6.banco.service.TransacaoService;
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
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaqueServletIntegracaoTest {

    @Mock
    private ContaDAO contaDAO;
    @Mock
    private TransacaoDAO transacaoDAO;
    @Mock
    private UsuarioDAO usuarioDAO;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpSession session;
    @Mock
    private RequestDispatcher dispatcher;

    private TransacaoService transacaoServiceReal;
    private SaqueServlet saqueServlet;

    private Cliente clienteLogado;
    private Conta contaCliente;

    @BeforeEach
    void setup() {
        transacaoServiceReal = new TransacaoService(contaDAO, transacaoDAO, usuarioDAO);
        saqueServlet = new SaqueServlet(transacaoServiceReal);

        contaCliente = new Conta("0001", "12345");
        contaCliente.setId(10);
        contaCliente.setSaldo(2000.0);

        clienteLogado = new Cliente();
        clienteLogado.setNome("Teste Integração");
        clienteLogado.setConta(contaCliente);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("usuarioLogado")).thenReturn(clienteLogado);
    }

    @Test
    @DisplayName("Integração Completa: Servlet -> Service -> DAO (Sucesso)")
    void fluxoCompletoComSucesso() throws Exception {
        String valorSaqueStr = "100.00";
        when(request.getParameter("valor")).thenReturn(valorSaqueStr);
        when(request.getRequestDispatcher("comprovanteSaque.jsp")).thenReturn(dispatcher);

        Transacao transacaoEsperada = new Transacao();
        transacaoEsperada.setValor(BigDecimal.valueOf(100.00)); // Correção: uso de BigDecimal
        when(contaDAO.realizarSaque(anyInt(), eq(100.00))).thenReturn(transacaoEsperada);

        saqueServlet.doPost(request, response);

        verify(contaDAO).realizarSaque(10, 100.00);
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Integração: Regra de Negócio do Service (Múltiplo de 10) capturada pela Servlet")
    void validaRegraDeNegocioDoService() throws Exception {
        when(request.getParameter("valor")).thenReturn("55.00");

        saqueServlet.doPost(request, response);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(captor.capture());

        String redirectUrl = captor.getValue();
        assertTrue(redirectUrl.contains("multiplos+de+R%24+10%2C00"));
        verify(contaDAO, never()).realizarSaque(anyInt(), anyDouble());
    }

    @Test
    @DisplayName("Integração: Validação de Hora do Service (Bloqueio noturno)")
    void validaRegraDeHorarioDoService() throws Exception {
        when(request.getParameter("valor")).thenReturn("-50.00");

        saqueServlet.doPost(request, response);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(captor.capture());
        
        assertTrue(captor.getValue().contains("positivo"));
        verify(contaDAO, never()).realizarSaque(anyInt(), anyDouble());
    }

    @Test
    @DisplayName("Integração: Dados inválidos no input não chegam ao DAO")
    void integracaoDadosInvalidos() throws Exception {
        when(request.getParameter("valor")).thenReturn("texto");

        saqueServlet.doPost(request, response);

        verify(response).sendRedirect(argThat(url -> url.contains("Valor+inv%C3%A1lido")));
        verifyNoInteractions(contaDAO);
    }
}