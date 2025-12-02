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

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de mutação para ClienteService, focando em cadastrarNovoCliente e outros métodos.
 */
@ExtendWith(MockitoExtension.class)
class ClienteServiceCadastrarNovoClienteMutacaoTest {

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
        Mockito.when(usuarioDAO.buscarPorCpf(("12345678901"))).thenReturn(null);
        assertDoesNotThrow(() -> clienteService.cadastrarNovoCliente(clienteBase, "Abcd1234"));
        Mockito.verify(usuarioDAO).cadastrarCliente((clienteBase));
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
        Mockito.when(usuarioDAO.buscarPorCpf(("12345678901"))).thenReturn(new Cliente());
        assertThrows(ValidationException.class, () -> clienteService.cadastrarNovoCliente(clienteBase, "Abcd1234"));
    }

    @Test
    @DisplayName("CPF com formato errado deve falhar (boundary simples)")
    void deveFalharCpfFormato() throws Exception {
        clienteBase.setCpf("0000000000");
        Mockito.when(usuarioDAO.buscarPorCpf(("0000000000"))).thenReturn(null);
        assertThrows(ValidationException.class, () -> clienteService.cadastrarNovoCliente(clienteBase, "Abcd1234"));
    }

    @Test
    @DisplayName("CPF nulo deve falhar")
    void deveFalharCpfNulo() throws Exception {
        clienteBase.setCpf(null);
        Mockito.when(usuarioDAO.buscarPorCpf((Mockito.isNull()))).thenReturn(null);
        assertThrows(ValidationException.class, () -> clienteService.cadastrarNovoCliente(clienteBase, "Abcd1234"));
        Mockito.verify(usuarioDAO, Mockito.never()).cadastrarCliente(Mockito.any());
    }

    @Test
    @DisplayName("Email sem @ deve falhar")
    void deveFalharEmailInvalido() throws Exception {
        clienteBase.setEmail("ab.com");
        Mockito.when(usuarioDAO.buscarPorCpf(("12345678901"))).thenReturn(null);
        assertThrows(ValidationException.class, () -> clienteService.cadastrarNovoCliente(clienteBase, "Abcd1234"));
    }

    @Test
    @DisplayName("Email nulo deve falhar")
    void deveFalharEmailNulo() throws Exception {
        clienteBase.setEmail(null);
        Mockito.when(usuarioDAO.buscarPorCpf(("12345678901"))).thenReturn(null);
        assertThrows(ValidationException.class, () -> clienteService.cadastrarNovoCliente(clienteBase, "Abcd1234"));
        Mockito.verify(usuarioDAO, Mockito.never()).cadastrarCliente(Mockito.any());
    }

    @Test
    @DisplayName("Menor que 18 anos deve falhar (limite)")
    void deveFalharIdadeMenorQue18() throws Exception {
        // Aqui usamos 18 anos + 1 dia para garantir que é menor
        clienteBase.setDataNascimento(LocalDate.now().minusYears(18).plusDays(1));
        Mockito.when(usuarioDAO.buscarPorCpf(("12345678901"))).thenReturn(null);
        assertThrows(ValidationException.class, () -> clienteService.cadastrarNovoCliente(clienteBase, "Abcd1234"));
    }

    @Test
    @DisplayName("Renda negativa deve falhar")
    void deveFalharRendaNegativa() throws Exception {
        clienteBase.setRenda(-0.0001);
        Mockito.when(usuarioDAO.buscarPorCpf(("12345678901"))).thenReturn(null);
        assertThrows(ValidationException.class, () -> clienteService.cadastrarNovoCliente(clienteBase, "Abcd1234"));
    }

    @Test
    @DisplayName("Renda zero deve passar")
    void devePassarRendaZero() throws Exception {
        clienteBase.setRenda(0.0);
        Mockito.when(usuarioDAO.buscarPorCpf(("12345678901"))).thenReturn(null);
        assertDoesNotThrow(() -> clienteService.cadastrarNovoCliente(clienteBase, "Abcd1234"));
        Mockito.verify(usuarioDAO).cadastrarCliente(clienteBase);
    }

    @Test
    @DisplayName("Senha fraca deve falhar (sem número)")
    void deveFalharSenhaFraca() throws Exception {
        clienteBase.setSenha("abcdefgh"); // simples: sem número
        Mockito.when(usuarioDAO.buscarPorCpf(("12345678901"))).thenReturn(null);
        assertThrows(ValidationException.class, () -> clienteService.cadastrarNovoCliente(clienteBase, "abcdefgh"));
    }

    @Test
    @DisplayName("Exatamente 18 anos deve passar (boundary)")
    void devePassarIdadeExatamente18() throws Exception {
        clienteBase.setDataNascimento(LocalDate.now().minusYears(18));
        Mockito.when(usuarioDAO.buscarPorCpf(("12345678901"))).thenReturn(null);
        assertDoesNotThrow(() -> clienteService.cadastrarNovoCliente(clienteBase, "Abcd1234"));
    }

    // === Testes para outros métodos (aumentar cobertura de mutação) ===

    @Test
    @DisplayName("buscarClientes: acao 'listarTodos' retorna lista")
    void buscarClientesListarTodos() throws Exception {
        List<Cliente> mockList = Arrays.asList(new Cliente(), new Cliente());
        Mockito.when(usuarioDAO.buscarTodosClientesOrdenados()).thenReturn(mockList);
        
        List<Cliente> result = clienteService.buscarClientes(null, "listarTodos");
        
        assertEquals(2, result.size());
        Mockito.verify(usuarioDAO).buscarTodosClientesOrdenados();
    }

    @Test
    @DisplayName("buscarClientes: termo não vazio busca por termo")
    void buscarClientesPorTermo() throws Exception {
        List<Cliente> mockList = Arrays.asList(new Cliente());
        Mockito.when(usuarioDAO.buscarClientesPorTermo("João")).thenReturn(mockList);
        
        List<Cliente> result = clienteService.buscarClientes("João", "");
        
        assertEquals(1, result.size());
        Mockito.verify(usuarioDAO).buscarClientesPorTermo("João");
    }

    @Test
    @DisplayName("buscarClientes: sem ação e termo vazio retorna lista vazia")
    void buscarClientesSemCriterio() throws Exception {
        List<Cliente> result = clienteService.buscarClientes("  ", null);
        
        assertTrue(result.isEmpty());
        Mockito.verify(usuarioDAO, Mockito.never()).buscarTodosClientesOrdenados();
        Mockito.verify(usuarioDAO, Mockito.never()).buscarClientesPorTermo(Mockito.anyString());
    }

    @Test
    @DisplayName("buscarClienteParaEdicao retorna cliente")
    void buscarClienteParaEdicao() throws Exception {
        Cliente mock = new Cliente();
        Mockito.when(usuarioDAO.buscarClientePorId(10)).thenReturn(mock);
        
        Cliente result = clienteService.buscarClienteParaEdicao(10);
        
        assertNotNull(result);
        assertSame(mock, result);
        Mockito.verify(usuarioDAO).buscarClientePorId(10);
    }

    @Test
    @DisplayName("atualizarCliente executa atualização")
    void atualizarCliente() throws Exception {
        Cliente cliente = new Cliente();
        clienteService.atualizarCliente(cliente);
        
        Mockito.verify(usuarioDAO).atualizarCliente(cliente);
    }

    @Test
    @DisplayName("excluirCliente executa exclusão")
    void excluirCliente() throws Exception {
        clienteService.excluirCliente(5);
        
        Mockito.verify(usuarioDAO).excluirClientePorId(5);
    }

    @Test
    @DisplayName("listarTodosClientes retorna lista")
    void listarTodosClientes() throws Exception {
        List<Cliente> mockList = Arrays.asList(new Cliente());
        Mockito.when(usuarioDAO.buscarTodosClientes()).thenReturn(mockList);
        
        List<Cliente> result = clienteService.listarTodosClientes();
        
        assertEquals(1, result.size());
        Mockito.verify(usuarioDAO).buscarTodosClientes();
    }

    @Test
    @DisplayName("atualizarSenha: falha quando senha atual incorreta")
    void atualizarSenhaFalhaSenhaAtual() throws Exception {
        assertThrows(ValidationException.class, () ->
                clienteService.atualizarSenha(1, "errada", "Nova1234", "Nova1234", "Correta1"));
        
        Mockito.verify(usuarioDAO, Mockito.never()).atualizarSenha(Mockito.anyInt(), Mockito.anyString());
    }

    @Test
    @DisplayName("atualizarSenha: falha quando nova senha igual à antiga")
    void atualizarSenhaFalhaMesmaSenha() throws Exception {
        assertThrows(ValidationException.class, () ->
                clienteService.atualizarSenha(1, "Correta1", "Correta1", "Correta1", "Correta1"));
        
        Mockito.verify(usuarioDAO, Mockito.never()).atualizarSenha(Mockito.anyInt(), Mockito.anyString());
    }

    @Test
    @DisplayName("atualizarSenha: falha quando confirmação diferente")
    void atualizarSenhaFalhaConfirmacao() throws Exception {
        assertThrows(ValidationException.class, () ->
                clienteService.atualizarSenha(1, "Correta1", "Nova1234", "Outra1234", "Correta1"));
        
        Mockito.verify(usuarioDAO, Mockito.never()).atualizarSenha(Mockito.anyInt(), Mockito.anyString());
    }

    @Test
    @DisplayName("atualizarSenha: falha por senha fraca")
    void atualizarSenhaFalhaFraca() throws Exception {
        assertThrows(ValidationException.class, () ->
                clienteService.atualizarSenha(1, "Correta1", "abcdefgh", "abcdefgh", "Correta1"));
        
        Mockito.verify(usuarioDAO, Mockito.never()).atualizarSenha(Mockito.anyInt(), Mockito.anyString());
    }

    @Test
    @DisplayName("atualizarSenha: sucesso quando todas validações passam")
    void atualizarSenhaOk() throws Exception {
        clienteService.atualizarSenha(2, "Correta1", "Nova1234", "Nova1234", "Correta1");
        
        Mockito.verify(usuarioDAO).atualizarSenha(2, "Nova1234");
    }

    @Test
    @DisplayName("buscarClientePorCpf retorna cliente")
    void buscarClientePorCpf() throws Exception {
        Cliente mock = new Cliente();
        Mockito.when(usuarioDAO.buscarPorCpf("99999999999")).thenReturn(mock);
        
        Cliente result = clienteService.buscarClientePorCpf("99999999999");
        
        assertNotNull(result);
        assertSame(mock, result);
        Mockito.verify(usuarioDAO).buscarPorCpf("99999999999");
    }
}
