package br.uff.ic.grupo6.banco.funcional;

import br.uff.ic.grupo6.banco.dao.UsuarioDAO;
import br.uff.ic.grupo6.banco.model.Cliente;
import br.uff.ic.grupo6.banco.service.ClienteService;
import br.uff.ic.grupo6.banco.service.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

/**
 * Testes funcionais (caixa-preta) para cadastrarNovoCliente(),
 * usando classes de equivalência e valores limite.
 */
@ExtendWith(MockitoExtension.class)
public class ClienteServiceCadastrarNovoClienteFuncionalTest {

    @Mock
    private UsuarioDAO usuarioDAO;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente clienteBaseValido;

    @BeforeEach
    void setUp() {
        clienteBaseValido = new Cliente();
        clienteBaseValido.setCpf("12345678901");
        clienteBaseValido.setEmail("user@example.com");
        clienteBaseValido.setDataNascimento(LocalDate.now().minusYears(25));
        clienteBaseValido.setRenda(1000.0);
        clienteBaseValido.setSenha("Senha123");
    }

    @Test
    @DisplayName("Classe válida: cadastro bem-sucedido")
    void deveCadastrarClienteValido() throws Exception {
        Mockito.when(usuarioDAO.buscarPorCpf(eq("12345678901"))).thenReturn(null);

        assertDoesNotThrow(() -> clienteService.cadastrarNovoCliente(clienteBaseValido, "Senha123"));
        Mockito.verify(usuarioDAO).cadastrarCliente(eq(clienteBaseValido));
    }

    @Test
    @DisplayName("Senhas diferentes: falha de validação")
    void deveFalharQuandoSenhasNaoConferem() throws Exception {
        Mockito.when(usuarioDAO.buscarPorCpf(eq("12345678901"))).thenReturn(null);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> clienteService.cadastrarNovoCliente(clienteBaseValido, "Outra123"));
        assertTrue(ex.getMessage().contains("senhas"));
        Mockito.verify(usuarioDAO, Mockito.never()).cadastrarCliente(any());
    }

    @Test
    @DisplayName("CPF duplicado: falha de validação")
    void deveFalharQuandoCpfDuplicado() throws Exception {
        Mockito.when(usuarioDAO.buscarPorCpf(eq("12345678901"))).thenReturn(new Cliente());

        ValidationException ex = assertThrows(ValidationException.class,
                () -> clienteService.cadastrarNovoCliente(clienteBaseValido, "Senha123"));
        assertTrue(ex.getMessage().contains("CPF"));
        Mockito.verify(usuarioDAO, Mockito.never()).cadastrarCliente(any());
    }

    @Test
    @DisplayName("CPF formato inválido: limite e equivalência")
    void deveFalharCpfFormatoInvalido() throws Exception {
        clienteBaseValido.setCpf("12345");
        Mockito.when(usuarioDAO.buscarPorCpf(eq("12345"))).thenReturn(null);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> clienteService.cadastrarNovoCliente(clienteBaseValido, "Senha123"));
        assertTrue(ex.getMessage().contains("CPF inválido"));
        Mockito.verify(usuarioDAO, Mockito.never()).cadastrarCliente(any());
    }

    @Test
    @DisplayName("Email inválido: sem arroba")
    void deveFalharEmailInvalido() throws Exception {
        clienteBaseValido.setEmail("email.invalido.com");
        Mockito.when(usuarioDAO.buscarPorCpf(eq("12345678901"))).thenReturn(null);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> clienteService.cadastrarNovoCliente(clienteBaseValido, "Senha123"));
        assertTrue(ex.getMessage().contains("e-mail"));
    }

    @Test
    @DisplayName("Idade limite: exatamente 18 anos é válido")
    void deveAceitarExatamente18Anos() throws Exception {
        // Limite aceito: 18 anos exatos
        clienteBaseValido.setDataNascimento(LocalDate.now().minusYears(18));
        Mockito.when(usuarioDAO.buscarPorCpf(eq("12345678901"))).thenReturn(null);

        assertDoesNotThrow(() -> clienteService.cadastrarNovoCliente(clienteBaseValido, "Senha123"));
        Mockito.verify(usuarioDAO).cadastrarCliente(eq(clienteBaseValido));
    }

    @Test
    @DisplayName("Idade menor que 18: falha de validação")
    void deveFalharMenorQue18() throws Exception {
        clienteBaseValido.setDataNascimento(LocalDate.now().minusYears(17));
        Mockito.when(usuarioDAO.buscarPorCpf(eq("12345678901"))).thenReturn(null);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> clienteService.cadastrarNovoCliente(clienteBaseValido, "Senha123"));
        assertTrue(ex.getMessage().contains("maior de 18"));
    }

    @Test
    @DisplayName("Renda negativa: falha de validação")
    void deveFalharRendaNegativa() throws Exception {
        clienteBaseValido.setRenda(-0.01);
        Mockito.when(usuarioDAO.buscarPorCpf(eq("12345678901"))).thenReturn(null);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> clienteService.cadastrarNovoCliente(clienteBaseValido, "Senha123"));
        assertTrue(ex.getMessage().contains("renda"));
    }

    @Test
    @DisplayName("Senha fraca: não atende ao padrão")
    void deveFalharSenhaFraca() throws Exception {
        clienteBaseValido.setSenha("abcdefg"); // sem número e <8
        Mockito.when(usuarioDAO.buscarPorCpf(eq("12345678901"))).thenReturn(null);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> clienteService.cadastrarNovoCliente(clienteBaseValido, "abcdefg"));
        assertTrue(ex.getMessage().contains("senha"));
    }
}
