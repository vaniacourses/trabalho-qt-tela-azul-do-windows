package br.uff.ic.grupo6.banco.sistema;

import br.uff.ic.grupo6.banco.dao.UsuarioDAO;
import br.uff.ic.grupo6.banco.model.Usuario;
import br.uff.ic.grupo6.banco.service.LoginService;
// Assumindo que você tem uma exceção de validação similar à do seu exemplo
import br.uff.ic.grupo6.banco.service.exception.ValidationException; 

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

/**
 * Testes de Unidade (Estruturais) para a classe LoginService, usando MockitoExtension.
 * Objetivo: Testar a lógica de autenticação e validação do serviço.
 */
@ExtendWith(MockitoExtension.class)
public class LoginServiceEstruturalTest {

    // 1. Simula o DAO (banco de dados). Não precisa mais de MockitoAnnotations.openMocks().
    @Mock
    private UsuarioDAO usuarioDAO;

    // 2. Injeta o mock do DAO no LoginService.
    @InjectMocks
    private LoginService loginService;

    // Dados de teste
    private final String CPF_VALIDO = "12345678900";
    private final String SENHA_CORRETA = "senha123";
    private final String SENHA_INCORRETA = "senhaerrada";
    private final String CPF_INEXISTENTE = "99999999999";
    private final String CPF_INVALIDO_FORMATO = "123";
    
    // Objeto Usuário de teste
    // Não precisa mais de classe auxiliar porque vamos usar o Mockito para criar a instância.
    private Usuario usuarioTeste;

    /**
     * Configuração inicial antes de cada teste.
     */
    @BeforeEach
    void setup() {
        // CORREÇÃO: Cria uma instância MOCK de Usuario, o que é padrão para classes 'abstract'
        // em testes estruturais e evita a necessidade da subclasse auxiliar.
        usuarioTeste = Mockito.mock(Usuario.class);
        
        // Configura o mock do usuário (stubbing): 
        // Define o que os métodos getLogin() e getSenha() devem retornar quando forem chamados
        // pelo LoginService.
        when(usuarioTeste.getLogin()).thenReturn(CPF_VALIDO);
        when(usuarioTeste.getSenha()).thenReturn(SENHA_CORRETA);
    }


    /**
     * CT-E-01: Login com Sucesso (Caminho Feliz).
     * Tecnica: Particionamento em Classes de Equivalência (PCE)
     */
    @Test
    @DisplayName("CT-E-01: Login com Sucesso (Caminho Feliz)")
    void cte01_loginSucesso() {
        // ARRANJAR (Setup):
        // 1. Simula que o DAO encontrou o usuário, retornando o mock configurado.
        when(usuarioDAO.buscarPorCpf(CPF_VALIDO)).thenReturn(usuarioTeste);

        // AGIR (Executar) e AVALIAR (Verificar):
        // 2. Testa que a execução não lança exceção e retorna o usuário
        assertDoesNotThrow(() -> {
            Usuario resultado = loginService.logar(CPF_VALIDO, SENHA_CORRETA);
            assertNotNull(resultado, "O login deveria ser bem-sucedido.");
            assertEquals(CPF_VALIDO, resultado.getLogin(), "O login retornado deve ser o CPF correto.");
        });

        // 3. Verifica se o método de busca do DAO foi chamado
        Mockito.verify(usuarioDAO, Mockito.times(1)).buscarPorCpf(CPF_VALIDO);
    }

    /**
     * CT-E-02: Falha de Login (Senha Incorreta).
     * Tecnica: PCE / Regra de Negócio
     */
    @Test
    @DisplayName("CT-E-02: Falha de Login (Senha Incorreta)")
    void cte02_falhaSenhaIncorreta() {
        // ARRANJAR (Setup):
        // 1. Simula que o DAO encontrou o usuário (o Service chamará getSenha() no mock e a comparação falhará).
        when(usuarioDAO.buscarPorCpf(CPF_VALIDO)).thenReturn(usuarioTeste);

        // AGIR (Executar):
        Usuario resultado = loginService.logar(CPF_VALIDO, SENHA_INCORRETA);

        // AVALIAR (Verificar):
        assertNull(resultado, "O login deveria falhar, pois a senha está incorreta.");
        
        // Verifica se o método de busca do DAO foi chamado para validar a existência do CPF
        Mockito.verify(usuarioDAO, Mockito.times(1)).buscarPorCpf(CPF_VALIDO);
    }

    /**
     * CT-E-03: Falha de Login (Usuário Inexistente).
     * Tecnica: PCE / Conta Inexistente
     */
    @Test
    @DisplayName("CT-E-03: Falha de Login (Usuário Inexistente)")
    void cte03_falhaUsuarioInexistente() {
        // ARRANJAR (Setup):
        // 1. Simula que o DAO NÃO encontrou o CPF.
        when(usuarioDAO.buscarPorCpf(CPF_INEXISTENTE)).thenReturn(null);

        // AGIR (Executar):
        Usuario resultado = loginService.logar(CPF_INEXISTENTE, SENHA_CORRETA);

        // AVALIAR (Verificar):
        assertNull(resultado, "O login deveria falhar, pois o usuário não existe.");
        
        Mockito.verify(usuarioDAO, Mockito.times(1)).buscarPorCpf(CPF_INEXISTENTE);
    }
    
    /**
     * CT-E-04: Falha de Validação: CPF em Formato Inválido (Ex: Tamanho menor)
     * Tecnica: Validação de Dados / Formato Inválido
     */
    @Test
    @DisplayName("CT-E-04: Deve lançar ValidationException se o CPF tem formato inválido")
    void cte04_cpfInvalidoFormato() {
        // ARRANJAR/AGIR (Executar):
        // 1. Tenta logar com CPF inválido
        // 2. Espera-se que lance a exceção antes de chamar o DAO
        assertThrows(ValidationException.class, 
                     () -> loginService.logar(CPF_INVALIDO_FORMATO, SENHA_CORRETA));

        // AVALIAR (Verificar):
        // 3. Garante que o método do DAO nunca foi chamado, pois a validação falhou
        Mockito.verify(usuarioDAO, Mockito.never()).buscarPorCpf(Mockito.anyString());
    }

    /**
     * CT-E-05: Falha de Validação: Senha Vazia/Nula
     * Tecnica: Validação de Dados / Valor Limite
     */
    @Test
    @DisplayName("CT-E-05: Deve lançar ValidationException se a senha for vazia")
    void cte05_senhaVazia() {
        // ARRANJAR/AGIR (Executar):
        // 1. Tenta logar com senha vazia
        // 2. Espera-se que lance a exceção antes de chamar o DAO
        assertThrows(ValidationException.class, 
                     () -> loginService.logar(CPF_VALIDO, ""));

        // AVALIAR (Verificar):
        // 3. Garante que o método do DAO nunca foi chamado.
        Mockito.verify(usuarioDAO, Mockito.never()).buscarPorCpf(Mockito.anyString());
    }
}