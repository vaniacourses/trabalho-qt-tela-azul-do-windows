package br.uff.ic.grupo6.banco.mutacao;

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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaqueServletMutacaoTest {

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
        saqueServlet = new SaqueServlet(transacaoService);

        contaCliente = new Conta("0001", "12345");
        contaCliente.setId(1);
        contaCliente.setSaldo(1000.0);

        clienteLogado = new Cliente();
        clienteLogado.setNome("Mutante Teste");
        clienteLogado.setConta(contaCliente);
    }

    @Test
    @DisplayName("Remoção do 'contaCliente.sacar()'")
    void deveAtualizarSaldoEmMemoriaAposSucesso() throws Exception {
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("usuarioLogado")).thenReturn(clienteLogado);
        when(request.getParameter("valor")).thenReturn("100.00");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
        when(transacaoService.realizarSaque(any(), anyDouble())).thenReturn(new Transacao());

        saqueServlet.doPost(request, response);

        assertEquals(900.0, contaCliente.getSaldo());
    }

    @Test
    @DisplayName("Remoção da validação de Login (if null)")
    void deveBloquearSeUsuarioNulo() throws Exception {
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("usuarioLogado")).thenReturn(null);

        saqueServlet.doPost(request, response);

        verify(response).sendRedirect(contains("login.jsp"));
        Mockito.verifyNoInteractions(transacaoService);
    }

    @Test
    @DisplayName("Remoção da validação de Conta (if conta null)")
    void deveBloquearSeContaNula() throws Exception {
        clienteLogado.setConta(null);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("usuarioLogado")).thenReturn(clienteLogado);
        when(request.getParameter("valor")).thenReturn("100.00");

        saqueServlet.doPost(request, response);

        verify(response).sendRedirect(contains("Conta+n%C3%A3o+encontrada"));
        Mockito.verifyNoInteractions(transacaoService);
    }

    @Test
    @DisplayName("Alteração na captura de ValidationException")
    void deveRedirecionarComMensagemExataDoService() throws Exception {
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("usuarioLogado")).thenReturn(clienteLogado);
        when(request.getParameter("valor")).thenReturn("5000.00");

        String msgErroEspecifica = "Saldo insuficiente para realizar o saque.";
        Mockito.doThrow(new ValidationException(msgErroEspecifica))
                .when(transacaoService).realizarSaque(any(), anyDouble());

        saqueServlet.doPost(request, response);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(captor.capture());

        String urlRedirecionamento = captor.getValue();
        assertTrue(urlRedirecionamento.contains(URLEncoder.encode(msgErroEspecifica, StandardCharsets.UTF_8)));
    }

    @Test
    @DisplayName("Remoção do setCharacterEncoding")
    void deveDefinirEncodingAntesDeLerParametros() throws Exception {
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("usuarioLogado")).thenReturn(null);

        saqueServlet.doPost(request, response);
    }
    
    @Test
    @DisplayName("Troca de parseDouble por outro método ou remoção de try/catch NumberFormat")
    void deveCapturarErroDeFormatacao() throws Exception {
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("usuarioLogado")).thenReturn(clienteLogado);
        when(request.getParameter("valor")).thenReturn("invalid_number");

        assertDoesNotThrow(() -> saqueServlet.doPost(request, response));

        verify(response).sendRedirect(contains("Valor+inv%C3%A1lido"));
    }
}