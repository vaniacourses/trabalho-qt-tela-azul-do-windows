package br.uff.ic.grupo6.banco.mutacao;

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

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;

/**
 * Testes de mutação para cadastrarNovoCliente(), cobrindo validações principais.
 */
@ExtendWith(MockitoExtension.class)
public class ClienteServiceCadastrarNovoClienteMutacaoTest {

    @Mock
    private UsuarioDAO usuarioDAO;

    @InjectMocks
    private ClienteService clienteService; // nome mais direto

    private Cliente clienteBase;

    @BeforeEach
    void setUp() {
        clienteBase = new Cliente();
        clienteBase.setCpf("12345678901");
        clienteBase.setEmail("a@b.com");
        clienteBase.setDataNascimento(LocalDate.now().minusYears(19));
        clienteBase.setRenda(100.0);
        clienteBase.setSenha("Abcd1234");
    }

    @Test
    @DisplayName("Caminho feliz funciona (protege contra remoção de validações)")
    void deveCadastrarQuandoTudoValido() throws Exception {
        Mockito.when(usuarioDAO.buscarPorCpf(eq("12345678901"))).thenReturn(null);
        assertDoesNotThrow(() -> clienteService.cadastrarNovoCliente(clienteBase, "Abcd1234"));
        Mockito.verify(usuarioDAO).cadastrarCliente(eq(clienteBase));
    }

    @Test
    @DisplayName("Senhas diferentes devem falhar (evita negar condicional)")
    void deveFalharQuandoSenhasNaoConferem() throws Exception {
        assertThrows(ValidationException.class, () -> clienteService.cadastrarNovoCliente(clienteBase, "diferente"));
        Mockito.verify(usuarioDAO, Mockito.never()).cadastrarCliente(Mockito.any());
    }

    @Test
    @DisplayName("CPF duplicado deve falhar (evita remover condicional)")
    void deveFalharQuandoCpfDuplicado() throws Exception {
        Mockito.when(usuarioDAO.buscarPorCpf(eq("12345678901"))).thenReturn(new Cliente());
        assertThrows(ValidationException.class, () -> clienteService.cadastrarNovoCliente(clienteBase, "Abcd1234"));
    }

    @Test
    @DisplayName("CPF com formato errado deve falhar (boundary simples)")
    void deveFalharCpfFormato() throws Exception {
        clienteBase.setCpf("0000000000");
        Mockito.when(usuarioDAO.buscarPorCpf(eq("0000000000"))).thenReturn(null);
        assertThrows(ValidationException.class, () -> clienteService.cadastrarNovoCliente(clienteBase, "Abcd1234"));
    }

    @Test
    @DisplayName("Email sem @ deve falhar")
    void deveFalharEmailInvalido() throws Exception {
        clienteBase.setEmail("ab.com");
        Mockito.when(usuarioDAO.buscarPorCpf(eq("12345678901"))).thenReturn(null);
        assertThrows(ValidationException.class, () -> clienteService.cadastrarNovoCliente(clienteBase, "Abcd1234"));
    }

    @Test
    @DisplayName("Menor que 18 anos deve falhar (limite)")
    void deveFalharIdadeMenorQue18() throws Exception {
        // Aqui usamos 18 anos + 1 dia para garantir que é menor
        clienteBase.setDataNascimento(LocalDate.now().minusYears(18).plusDays(1));
        Mockito.when(usuarioDAO.buscarPorCpf(eq("12345678901"))).thenReturn(null);
        assertThrows(ValidationException.class, () -> clienteService.cadastrarNovoCliente(clienteBase, "Abcd1234"));
    }

    @Test
    @DisplayName("Renda negativa deve falhar")
    void deveFalharRendaNegativa() throws Exception {
        clienteBase.setRenda(-0.0001);
        Mockito.when(usuarioDAO.buscarPorCpf(eq("12345678901"))).thenReturn(null);
        assertThrows(ValidationException.class, () -> clienteService.cadastrarNovoCliente(clienteBase, "Abcd1234"));
    }

    @Test
    @DisplayName("Senha fraca deve falhar (sem número)")
    void deveFalharSenhaFraca() throws Exception {
        clienteBase.setSenha("abcdefgh"); // simples: sem número
        Mockito.when(usuarioDAO.buscarPorCpf(eq("12345678901"))).thenReturn(null);
        assertThrows(ValidationException.class, () -> clienteService.cadastrarNovoCliente(clienteBase, "abcdefgh"));
    }
}
