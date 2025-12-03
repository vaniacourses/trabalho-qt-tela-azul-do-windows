package br.uff.ic.grupo6.banco.estrutural;

import br.uff.ic.grupo6.banco.dao.UsuarioDAO;
import br.uff.ic.grupo6.banco.model.Cliente; // Necessário para simular conta bloqueada
import br.uff.ic.grupo6.banco.model.Usuario;
import br.uff.ic.grupo6.banco.service.LoginService;
import br.uff.ic.grupo6.banco.service.exception.LoginException; // O service agora lança LoginException

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.SQLException;

/**
 * Testes de Unidade (Estruturais) para a classe LoginService, usando MockitoExtension.
 * Objetivo: Testar a lógica de autenticação e validação do serviço.
 * Cobre as arestas do método autenticar(cpf, senha) com complexidade ciclomática 10.
 */
@ExtendWith(MockitoExtension.class)
class LoginServiceEstruturalTest {

    // 1. Simula o DAO (banco de dados)
    @Mock
    private UsuarioDAO usuarioDAO;

    // 2. Injeta o mock do DAO no LoginService
    @InjectMocks
    private LoginService loginService;

    // Dados de teste
    private final String CPF_VALIDO = "12345678900";
    private final String SENHA_CORRETA = "senha123";
    private final String SENHA_INCORRETA = "senhaerrada";
    private final String SENHA_TEMPORARIA = "temp123"; // Aresta 7
    private final String CPF_INEXISTENTE = "99999999999";
    private final String CPF_INVALIDO_FORMATO = "123";
    
    // Objeto Usuário de teste
    private Usuario usuarioTeste;

    /**
     * Configuração inicial antes de cada teste.
     * Deve declarar throws SQLException porque o método buscarPorCpf() do DAO lança essa exceção (checked exception).
     */
    @BeforeEach
    void setup() throws SQLException { 
        // Cria uma instância MOCK de Usuario (para a maioria dos testes)
        usuarioTeste = Mockito.mock(Usuario.class);
        
        // Configura o mock do usuário (stubbing): 
        // USANDO MOCKITO.LENIENT() para permitir que estes stubs sejam ignorados ou sobrescritos
        Mockito.lenient().doReturn(CPF_VALIDO).when(usuarioTeste).getLogin();
        Mockito.lenient().doReturn(SENHA_CORRETA).when(usuarioTeste).getSenha();
        
        // Configura o comportamento padrão do DAO para o CPF_VALIDO (lenient)
        Mockito.lenient().doReturn(usuarioTeste).when(usuarioDAO).buscarPorCpf(CPF_VALIDO); 
    }


    /**
     * CT-E-37: Login com Sucesso (Caminho Feliz).
     * Cobre: Passa Validações 1 a 7.
     * @throws SQLException 
     */
    @Test
    @DisplayName("CT-E-37: Login com Sucesso (Caminho Feliz)")
    void cte01_loginSucesso() throws SQLException {
        // ARRANJAR (Setup):
        // O setup já configurou o comportamento de sucesso.

        // AGIR (Executar) e AVALIAR (Verificar):
        assertDoesNotThrow(() -> {
            Usuario resultado = loginService.autenticar(CPF_VALIDO, SENHA_CORRETA); 
            assertNotNull(resultado, "O login deveria ser bem-sucedido.");
        }, "Nenhuma exceção esperada deve ser lançada.");

        // Verifica se o método de busca do DAO foi chamado
        Mockito.verify(usuarioDAO, Mockito.times(1)).buscarPorCpf(CPF_VALIDO);
    }

    /**
     * CT-E-38: Falha de Login (Senha Incorreta).
     * Cobre: Falha na Validação 5.
     * @throws SQLException 
     */
    @Test
    @DisplayName("CT-E-38: Falha de Login (Senha Incorreta)")
    void cte02_falhaSenhaIncorreta() throws SQLException {
        // ARRANJAR: O setup configurou que o DAO retorna o usuário, mas a senha passada é incorreta.

        // AGIR (Executar) e AVALIAR (Verificar):
        assertThrows(LoginException.class, 
                     () -> loginService.autenticar(CPF_VALIDO, SENHA_INCORRETA));

        // Verifica se o método de busca do DAO foi chamado (para buscar o CPF)
        Mockito.verify(usuarioDAO, Mockito.times(1)).buscarPorCpf(CPF_VALIDO);
    }

    /**
     * CT-E-39: Falha de Login (Usuário Inexistente).
     * Cobre: Falha na Validação 4.
     * @throws SQLException 
     */
    @Test
    @DisplayName("CT-E-39: Falha de Login (Usuário Inexistente)")
    void cte03_falhaUsuarioInexistente() throws SQLException {
        // ARRANJAR (Setup):
        // 1. Simula que o DAO NÃO encontrou o CPF_INEXISTENTE (usando doReturn)
        doReturn(null).when(usuarioDAO).buscarPorCpf(CPF_INEXISTENTE);

        // AGIR (Executar) e AVALIAR (Verificar):
        assertThrows(LoginException.class, 
                     () -> loginService.autenticar(CPF_INEXISTENTE, SENHA_CORRETA));
        
        Mockito.verify(usuarioDAO, Mockito.times(1)).buscarPorCpf(CPF_INEXISTENTE);
    }
    
    /**
     * CT-E-40: Falha de Validação: CPF em Formato Inválido.
     * Cobre: Falha na Validação 3.
     * @throws SQLException 
     */
    @Test
    @DisplayName("CT-E-40: Deve lançar LoginException se o CPF tem formato inválido")
    void cte04_cpfInvalidoFormato() throws SQLException {
        // AGIR/AVALIAR: Espera-se que a exceção seja lançada ANTES de chamar o DAO
        assertThrows(LoginException.class, 
                     () -> loginService.autenticar(CPF_INVALIDO_FORMATO, SENHA_CORRETA)); 

        // 3. Garante que o método do DAO nunca foi chamado.
        Mockito.verify(usuarioDAO, Mockito.never()).buscarPorCpf(Mockito.anyString());
    }

    /**
     * CT-E-41: Falha de Validação: Senha Vazia.
     * Cobre: Falha na Validação 2.
     * @throws SQLException 
     */
    @Test
    @DisplayName("CT-E-41: Deve lançar LoginException se a senha for vazia")
    void cte05_senhaVazia() throws SQLException {
        // AGIR/AVALIAR: Espera-se que a exceção seja lançada ANTES de chamar o DAO
        assertThrows(LoginException.class, 
                     () -> loginService.autenticar(CPF_VALIDO, ""));

        // 3. Garante que o método do DAO nunca foi chamado.
        Mockito.verify(usuarioDAO, Mockito.never()).buscarPorCpf(Mockito.anyString());
    }

    /**
     * CT-E-42: Falha de Validação: CPF nulo.
     * Cobre: Falha na Validação 1.
     * @throws SQLException 
     */
    @Test
    @DisplayName("CT-E-42: Deve lançar LoginException se o CPF for nulo")
    void cte06_cpfNulo() throws SQLException {
        // AGIR/AVALIAR: Espera-se que a exceção seja lançada ANTES de chamar o DAO
        assertThrows(LoginException.class, 
                     () -> loginService.autenticar(null, SENHA_CORRETA));

        // 3. Garante que o método do DAO nunca foi chamado.
        Mockito.verify(usuarioDAO, Mockito.never()).buscarPorCpf(Mockito.anyString());
    }

    /**
     * CT-E-43: Falha de Regra de Negócio: Conta Bloqueada.
     * Cobre: Falha na Validação 6. Requer o uso de um mock de Cliente.
     * @throws SQLException 
     */
    @Test
    @DisplayName("CT-E-43: Deve lançar LoginException se a conta estiver bloqueada")
    void cte07_contaBloqueada() throws SQLException {
        // ARRANJAR: Configura um mock de Cliente (que estende Usuario)
        Cliente clienteBloqueado = Mockito.mock(Cliente.class);

        // 1. SOBRESCREVE o comportamento do DAO (lenient para evitar UnnecessaryStubbingException)
        Mockito.lenient().doReturn(clienteBloqueado).when(usuarioDAO).buscarPorCpf(CPF_VALIDO);
        
        // 2. Configura as propriedades necessárias para o mock de Cliente (lenient para evitar UnnecessaryStubbingException)
        Mockito.lenient().doReturn(SENHA_CORRETA).when(clienteBloqueado).getSenha();
        Mockito.lenient().doReturn("Usuario Bloqueado").when(clienteBloqueado).getNome(); // Ativa a regra de bloqueio

        // AGIR/AVALIAR:
        // Deve lançar LoginException mesmo com CPF e Senha corretos.
        assertThrows(LoginException.class, 
                     () -> loginService.autenticar(CPF_VALIDO, SENHA_CORRETA));

        Mockito.verify(usuarioDAO, Mockito.times(1)).buscarPorCpf(CPF_VALIDO);
    }
    
    /**
     * CT-E-44: Falha de Regra de Negócio: Senha Temporária.
     * Cobre: Falha na Validação 7.
     * @throws SQLException 
     */
    @Test
    @DisplayName("CT-E-44: Deve lançar LoginException se a senha for temporária")
    void cte08_senhaTemporaria() throws SQLException {
        // ARRANJAR:
        // 1. Configura um usuário mock com a senha temporária
        Usuario usuarioTemp = Mockito.mock(Usuario.class);
        // Configurando propriedades (lenient para evitar UnnecessaryStubbingException)
        Mockito.lenient().doReturn(CPF_VALIDO).when(usuarioTemp).getLogin();
        Mockito.lenient().doReturn(SENHA_TEMPORARIA).when(usuarioTemp).getSenha();
        
        // 2. SOBRESCREVE o comportamento do DAO para retornar o usuário temporário
        Mockito.lenient().doReturn(usuarioTemp).when(usuarioDAO).buscarPorCpf(CPF_VALIDO);

        // AGIR/AVALIAR:
        // Deve lançar LoginException se a senha for "temp123", mesmo que ela "bata" com o campo.
        assertThrows(LoginException.class, 
                     () -> loginService.autenticar(CPF_VALIDO, SENHA_TEMPORARIA));

        Mockito.verify(usuarioDAO, Mockito.times(1)).buscarPorCpf(CPF_VALIDO);
    }
}