package br.uff.ic.grupo6.banco.mutacao;

import br.uff.ic.grupo6.banco.dao.UsuarioDAO;
import br.uff.ic.grupo6.banco.model.Cliente;
import br.uff.ic.grupo6.banco.model.Usuario;
import br.uff.ic.grupo6.banco.service.LoginService;
import br.uff.ic.grupo6.banco.service.exception.LoginException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings; 
import org.mockito.quality.Strictness; 

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes de Mutação para LoginService, focando no método autenticar(cpf, senha)
 * para garantir que os testes existentes (estruturais/funcionais) são robustos.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LoginServiceMutacaoTest {

    @Mock
    private UsuarioDAO usuarioDAO;

    @InjectMocks
    private LoginService loginService;

    // Dados de Teste
    private final String CPF_VALIDO = "12345678900";
    private final String SENHA_CORRETA = "senha123";
    private final String SENHA_TEMPORARIA = "temp123";
    
    // Objeto Usuário de teste
    private Usuario usuarioMock;

    @BeforeEach
    void setUp() throws SQLException {
        usuarioMock = Mockito.mock(Usuario.class);
        when(usuarioMock.getLogin()).thenReturn(CPF_VALIDO);
        when(usuarioMock.getSenha()).thenReturn(SENHA_CORRETA);
        
        when(usuarioDAO.buscarPorCpf(CPF_VALIDO)).thenReturn(usuarioMock);
    }

    @Test
    @DisplayName("TM-29: Deve falhar se CPF for nulo ou vazio (protege contra remoção de validações de entrada)")
    void tm29_deveFalharCpfNuloOuVazio() throws SQLException {
        // CPF Nulo
        assertThrows(LoginException.class, 
                     () -> loginService.autenticar(null, SENHA_CORRETA), 
                     "Mutante SDL/CRCR na validação de CPF nulo deve ser morto.");
        
        // CPF Vazio
        assertThrows(LoginException.class, 
                     () -> loginService.autenticar("123", SENHA_CORRETA), 
                     "Mutante SDL/CRCR na validação de formato de CPF deve ser morto.");
        
        verify(usuarioDAO, never()).buscarPorCpf(anyString());
    }

    @Test
    @DisplayName("TM-30: Deve falhar se Senha for nula ou vazia (protege contra remoção de validações de entrada)")
    void tm30_deveFalharSenhaNulaOuVazia() throws SQLException {
        // Senha Nula
        assertThrows(LoginException.class, 
                     () -> loginService.autenticar(CPF_VALIDO, null), 
                     "Mutante SDL/CRCR na validação de senha nula deve ser morto.");
        
        // Senha Vazia
        assertThrows(LoginException.class, 
                     () -> loginService.autenticar(CPF_VALIDO, ""), 
                     "Mutante SDL/CRCR na validação de senha vazia deve ser morto.");
        
        verify(usuarioDAO, never()).buscarPorCpf(anyString());
    }
    
    @Test
    @DisplayName("TM-31: Deve falhar quando Usuário Inexistente (protege contra remoção da busca no DAO)")
    void tm31_deveFalharUsuarioInexistente() throws SQLException {
        when(usuarioDAO.buscarPorCpf("99999999999")).thenReturn(null);
        
        assertThrows(LoginException.class, 
                     () -> loginService.autenticar("99999999999", SENHA_CORRETA), 
                     "Mutante SDL na busca do DAO ou mutante de condição `if (usuario == null)` deve ser morto.");

        verify(usuarioDAO, times(1)).buscarPorCpf("99999999999");
    }

    @Test
    @DisplayName("TM-32: Deve falhar quando Senha Incorreta (protege contra SDL/CRCR na comparação de senha)")
    void tm32_deveFalharSenhaIncorreta() throws SQLException {
        assertThrows(LoginException.class, 
                     () -> loginService.autenticar(CPF_VALIDO, "senhaErrada"),
                     "Mutante SDL/CRCR/AOR na comparação de senha deve ser morto.");

        verify(usuarioDAO, times(1)).buscarPorCpf(CPF_VALIDO);
    }
    
    @Test
    @DisplayName("TM-33: Deve falhar quando Senha for Temporária (protege contra remoção da regra de negócio)")
    void tm33_deveFalharSenhaTemporaria() throws SQLException {
        Usuario usuarioTemp = Mockito.mock(Usuario.class);
        when(usuarioTemp.getSenha()).thenReturn(SENHA_TEMPORARIA);
        when(usuarioDAO.buscarPorCpf(CPF_VALIDO)).thenReturn(usuarioTemp);
        
        assertThrows(LoginException.class, 
                     () -> loginService.autenticar(CPF_VALIDO, SENHA_TEMPORARIA),
                     "Mutante SDL/CRCR na validação de senha temporária deve ser morto.");

        verify(usuarioDAO, times(1)).buscarPorCpf(CPF_VALIDO);
    }
    
    @Test
    @DisplayName("TM-34: Deve falhar quando Conta Bloqueada (protege contra remoção da regra de bloqueio)")
    void tm34_deveFalharContaBloqueada() throws SQLException {
        Cliente clienteBloqueado = Mockito.mock(Cliente.class); 
        when(clienteBloqueado.getSenha()).thenReturn(SENHA_CORRETA);
        // Simulação de que a propriedade de bloqueio está ativada
        when(clienteBloqueado.getNome()).thenReturn("Usuario Bloqueado"); 

        when(usuarioDAO.buscarPorCpf(CPF_VALIDO)).thenReturn(clienteBloqueado);
        
        assertThrows(LoginException.class, 
                     () -> loginService.autenticar(CPF_VALIDO, SENHA_CORRETA),
                     "Mutante SDL/CRCR na validação de conta bloqueada deve ser morto.");

        verify(usuarioDAO, times(1)).buscarPorCpf(CPF_VALIDO);
    }
    
    @Test
    @DisplayName("TM-35: Deve relançar SQLException em caso de falha de acesso a dados (protege contra remoção do bloco catch)")
    void tm35_deveRelancarEmCasoDeSQLException() throws SQLException {
        // Simula uma falha de conexão/DB durante a busca
        doThrow(new SQLException("DB Offline")).when(usuarioDAO).buscarPorCpf(CPF_VALIDO);
        
        // A SQLException deve ser lançada diretamente pelo service
        assertThrows(SQLException.class,
                     () -> loginService.autenticar(CPF_VALIDO, SENHA_CORRETA),
                     "Mutante SDL no bloco catch que impede o relançamento da exceção deve ser morto.");

        verify(usuarioDAO, times(1)).buscarPorCpf(CPF_VALIDO);
    }
    
    @Test
    @DisplayName("TM-36: Deve retornar o objeto Usuario (protege contra SDL/SVR no retorno de sucesso)")
    void tm36_deveRetornarUsuarioComSucesso() throws SQLException, LoginException {
        Usuario resultado = loginService.autenticar(CPF_VALIDO, SENHA_CORRETA);
        
        assertSame(usuarioMock, resultado, "Mutante SVR/SDL que altera o objeto retornado deve ser morto.");
        
        verify(usuarioDAO, times(1)).buscarPorCpf(CPF_VALIDO);
    }
}