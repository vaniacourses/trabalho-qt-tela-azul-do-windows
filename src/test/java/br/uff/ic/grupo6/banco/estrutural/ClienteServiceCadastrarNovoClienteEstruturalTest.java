package br.uff.ic.grupo6.banco.estrutural;

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

/**
 * Testes estruturais (caixa-branca) cobrindo arestas relevantes do fluxo de cadastro.
 */
@ExtendWith(MockitoExtension.class)
class ClienteServiceCadastrarNovoClienteEstruturalTest {

    @Mock
    private UsuarioDAO usuarioDAO;

    @InjectMocks
    private ClienteService clienteService; // nome mais direto

    private Cliente clienteBase;

    @BeforeEach
    void init() {
        clienteBase = new Cliente();
        clienteBase.setCpf("12345678901");
        clienteBase.setEmail("a@b.com");
        clienteBase.setDataNascimento(LocalDate.now().minusYears(30));
        clienteBase.setRenda(500.0);
        clienteBase.setSenha("Abcd1234");
    }

    @Test
    @DisplayName("Caminho feliz: todas as validações passam")
    void caminhoFeliz() throws Exception {
        Mockito.when(usuarioDAO.buscarPorCpf(("12345678901"))).thenReturn(null);
        assertDoesNotThrow(() -> clienteService.cadastrarNovoCliente(clienteBase, "Abcd1234"));
        Mockito.verify(usuarioDAO).cadastrarCliente((clienteBase));
    }

    @Test
    @DisplayName("Aresta precoce: senhas não conferem")
    void senhasNaoConferem() throws Exception {
        assertThrows(ValidationException.class, () -> clienteService.cadastrarNovoCliente(clienteBase, "diferente"));
        Mockito.verify(usuarioDAO, Mockito.never()).cadastrarCliente(Mockito.any());
    }

    @Test
    @DisplayName("Aresta CPF duplicado")
    void cpfDuplicado() throws Exception {
        Mockito.when(usuarioDAO.buscarPorCpf(("12345678901"))).thenReturn(new Cliente());
        assertThrows(ValidationException.class, () -> clienteService.cadastrarNovoCliente(clienteBase, "Abcd1234"));
        Mockito.verify(usuarioDAO, Mockito.never()).cadastrarCliente(Mockito.any());
    }

    @Test
    @DisplayName("Aresta CPF inválido (formato)")
    void cpfInvalidoFormato() throws Exception {
        clienteBase.setCpf("123");
        Mockito.when(usuarioDAO.buscarPorCpf(("123"))).thenReturn(null);
        assertThrows(ValidationException.class, () -> clienteService.cadastrarNovoCliente(clienteBase, "Abcd1234"));
    }

    @Test
    @DisplayName("Aresta Email inválido")
    void emailInvalido() throws Exception {
        clienteBase.setEmail("sem_arroba");
        Mockito.when(usuarioDAO.buscarPorCpf(("12345678901"))).thenReturn(null);
        assertThrows(ValidationException.class, () -> clienteService.cadastrarNovoCliente(clienteBase, "Abcd1234"));
    }

    @Test
    @DisplayName("Aresta Idade menor que 18")
    void idadeMenorQue18() throws Exception {
        clienteBase.setDataNascimento(LocalDate.now().minusYears(17));
        Mockito.when(usuarioDAO.buscarPorCpf(("12345678901"))).thenReturn(null);
        assertThrows(ValidationException.class, () -> clienteService.cadastrarNovoCliente(clienteBase, "Abcd1234"));
    }

    @Test
    @DisplayName("Aresta Renda negativa")
    void rendaNegativa() throws Exception {
        clienteBase.setRenda(-1.0);
        Mockito.when(usuarioDAO.buscarPorCpf(("12345678901"))).thenReturn(null);
        assertThrows(ValidationException.class, () -> clienteService.cadastrarNovoCliente(clienteBase, "Abcd1234"));
    }

    @Test
    @DisplayName("Aresta Senha fraca/nula")
    void senhaFracaOuNula() throws Exception {
        clienteBase.setSenha("abc");
        Mockito.when(usuarioDAO.buscarPorCpf(("12345678901"))).thenReturn(null);
        assertThrows(ValidationException.class, () -> clienteService.cadastrarNovoCliente(clienteBase, "abc"));
    }
}
