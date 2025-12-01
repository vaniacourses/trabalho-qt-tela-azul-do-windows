package br.uff.ic.grupo6.banco.unitarios;

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
 * Testes unitários gerais para cadastrarNovoCliente().
 */
@ExtendWith(MockitoExtension.class)
public class ClienteServiceCadastrarNovoClienteUnitarioTest {

    @Mock
    private UsuarioDAO usuarioDAO;

    @InjectMocks
    private ClienteService service;

    private Cliente c;

    @BeforeEach
    void init() {
        c = new Cliente();
        c.setCpf("12345678901");
        c.setEmail("a@b.com");
        c.setDataNascimento(LocalDate.now().minusYears(40));
        c.setRenda(1200.0);
        c.setSenha("Senha123");
    }

    @Test
    @DisplayName("Cadastro positivo com mocks")
    void cadastroPositivo() throws Exception {
        Mockito.when(usuarioDAO.buscarPorCpf(eq("12345678901"))).thenReturn(null);
        assertDoesNotThrow(() -> service.cadastrarNovoCliente(c, "Senha123"));
        Mockito.verify(usuarioDAO).cadastrarCliente(eq(c));
    }

    @Test
    @DisplayName("Negativo: email inválido")
    void negativoEmail() throws Exception {
        c.setEmail("sem_arroba");
        Mockito.when(usuarioDAO.buscarPorCpf(eq("12345678901"))).thenReturn(null);
        assertThrows(ValidationException.class, () -> service.cadastrarNovoCliente(c, "Senha123"));
        Mockito.verify(usuarioDAO, Mockito.never()).cadastrarCliente(Mockito.any());
    }

    @Test
    @DisplayName("Negativo: CPF duplicado")
    void negativoCpfDuplicado() throws Exception {
        Mockito.when(usuarioDAO.buscarPorCpf(eq("12345678901"))).thenReturn(new Cliente());
        assertThrows(ValidationException.class, () -> service.cadastrarNovoCliente(c, "Senha123"));
    }

    @Test
    @DisplayName("Negativo: senha fraca")
    void negativoSenhaFraca() throws Exception {
        c.setSenha("abcdefghi");
        Mockito.when(usuarioDAO.buscarPorCpf(eq("12345678901"))).thenReturn(null);
        assertThrows(ValidationException.class, () -> service.cadastrarNovoCliente(c, "abcdefghi"));
    }
}
