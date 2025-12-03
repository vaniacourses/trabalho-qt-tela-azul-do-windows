package br.uff.ic.grupo6.banco.unitarios;

import br.uff.ic.grupo6.banco.dao.UsuarioDAO;
import br.uff.ic.grupo6.banco.model.Cliente;
import br.uff.ic.grupo6.banco.model.Gerente;
import br.uff.ic.grupo6.banco.model.Usuario; // RE-INTRODUZIDO
import br.uff.ic.grupo6.banco.service.LoginService;
import br.uff.ic.grupo6.banco.service.exception.LoginException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * Testes unitários para a classe LoginService, cobrindo autenticação e
 * a funcionalidade "Lembrar-me".
 */
@ExtendWith(MockitoExtension.class)
class LoginServiceUnitarioTest {

    // Mock para simular o acesso ao banco de dados (DAO)
    @Mock
    private UsuarioDAO usuarioDAO;

    // Injeta o mock do DAO na classe LoginService
    @InjectMocks
    private LoginService loginService;

    // Constantes de teste (usando apenas dígitos, conforme a validação do service)
    private static final String CLIENTE_CPF_OK = "12345678900";
    private static final String CLIENTE_SENHA_OK = "senha123";

    private static final String GERENTE_CPF_OK = "99988877766";
    private static final String GERENTE_SENHA_OK = "admin";
    private static final String USUARIO_INEXISTENTE_CPF = "00000000000";

    /**
     * Método auxiliar para criar um mock de Cliente válido.
     */
    private Cliente criarMockCliente(String cpf, String senha) {
        Cliente cliente = new Cliente();
        cliente.setId(10);
        cliente.setCpf(cpf);
        cliente.setSenha(senha);
        cliente.setNome("Cliente de Teste");
        return cliente;
    }

    /**
     * Método auxiliar para criar um mock de Gerente válido.
     */
    private Gerente criarMockGerente(String cpf, String senha) {
        Gerente gerente = new Gerente(cpf, senha);
        gerente.setId(20);
        gerente.setNome("Gerente de Teste");
        return gerente;
    }


    /*
     * 1. Login com Sucesso (Cliente): Simular login com CPF "123.456.789-00"
     * e senha "senha123". O DAO retorna um objeto Cliente (Mock).
     */
    @Test
    @DisplayName("TUM-29: Login de Cliente com sucesso")
    void tum29_loginClienteSucesso() throws Exception {
        // 1. Configurar o Mock: Quando buscarPorCpf for chamado com o CPF do cliente,
        //    retorne um mock de Cliente com a senha correta.
        Cliente clienteMock = criarMockCliente(CLIENTE_CPF_OK, CLIENTE_SENHA_OK);
        Mockito.when(usuarioDAO.buscarPorCpf(CLIENTE_CPF_OK)).thenReturn(clienteMock);

        // 2. Executar o método em teste
        Usuario resultado = assertDoesNotThrow( // Tipo revertido para Usuario
            () -> loginService.autenticar(CLIENTE_CPF_OK, CLIENTE_SENHA_OK),
            "Não deveria lançar exceção no login de sucesso."
        );

        // 3. Verificar o resultado
        assertNotNull(resultado, "O resultado do login não deve ser nulo.");
        assertTrue(resultado instanceof Cliente, "O objeto retornado deve ser uma instância de Cliente.");
        // Cast explícito para Cliente para acessar getCpf()
        assertEquals(CLIENTE_CPF_OK, ((Cliente) resultado).getCpf(), "O CPF deve ser o mesmo do cliente autenticado.");

        // 4. Verificar interação com o mock
        Mockito.verify(usuarioDAO).buscarPorCpf(CLIENTE_CPF_OK);
    }

    /*
     * 2. Login com Sucesso (Gerente): Simular login com CPF "999.888.777-66"
     * e senha "admin". O DAO retorna um objeto Gerente (Mock).
     */
    @Test
    @DisplayName("TUM-30: Login de Gerente com sucesso")
    void tum30_loginGerenteSucesso() throws Exception {
        // 1. Configurar o Mock: Quando buscarPorCpf for chamado com o CPF do gerente,
        //    retorne um mock de Gerente com a senha correta.
        Gerente gerenteMock = criarMockGerente(GERENTE_CPF_OK, GERENTE_SENHA_OK);
        Mockito.when(usuarioDAO.buscarPorCpf(GERENTE_CPF_OK)).thenReturn(gerenteMock);

        // 2. Executar o método em teste
        Usuario resultado = assertDoesNotThrow( // Tipo revertido para Usuario
            () -> loginService.autenticar(GERENTE_CPF_OK, GERENTE_SENHA_OK),
            "Não deveria lançar exceção no login de sucesso do gerente."
        );

        // 3. Verificar o resultado
        assertNotNull(resultado, "O resultado do login não deve ser nulo.");
        assertTrue(resultado instanceof Gerente, "O objeto retornado deve ser uma instância de Gerente.");
        
        // Verificando o Nome em vez do CPF, pois getCpf() está indefinido para Gerente.
        assertEquals("Gerente de Teste", ((Gerente) resultado).getNome(), 
            "O nome do Gerente mocked deve ser retornado (verificação alternativa ao CPF)."
        );

        // 4. Verificar interação com o mock
        Mockito.verify(usuarioDAO).buscarPorCpf(GERENTE_CPF_OK);
    }

    /*
     * 3. Falha de Login (Senha Incorreta): Simular login com CPF válido
     * "123.456.789-00" mas senha "senhaErrada".
     */
    @Test
    @DisplayName("TUM-31: Falha de Login - Senha Incorreta")
    void tum31_falhaLoginSenhaIncorreta() throws Exception {
        // 1. Configurar o Mock: O DAO retorna o objeto Cliente com a senha correta
        Cliente clienteMock = criarMockCliente(CLIENTE_CPF_OK, CLIENTE_SENHA_OK);
        Mockito.when(usuarioDAO.buscarPorCpf(CLIENTE_CPF_OK)).thenReturn(clienteMock);

        // 2. Executar e verificar: Tentar autenticar com a senha errada
        LoginException exception = assertThrows(
            LoginException.class,
            () -> loginService.autenticar(CLIENTE_CPF_OK, "senhaErrada"), // Senha incorreta
            "Deveria lançar LoginException para senha incorreta."
        );

        // 3. Verificar a mensagem de erro (conforme implementação do serviço)
        assertEquals("CPF ou senha invalidos.", exception.getMessage());

        // 4. Verificar interação com o mock
        Mockito.verify(usuarioDAO).buscarPorCpf(CLIENTE_CPF_OK);
    }

    /*
     * 4. Falha de Login (Usuário Inexistente): Simular login com CPF
     * "000.000.000-00". O DAO retorna null (usuário não encontrado).
     */
    @Test
    @DisplayName("TUM-32: Falha de Login - Usuário Inexistente")
    void tum32_falhaLoginUsuarioInexistente() throws Exception {
        // 1. Configurar o Mock: O DAO retorna null para o CPF não encontrado
        Mockito.when(usuarioDAO.buscarPorCpf(USUARIO_INEXISTENTE_CPF)).thenReturn(null);

        // 2. Executar e verificar: Tentar autenticar
        LoginException exception = assertThrows(
            LoginException.class,
            () -> loginService.autenticar(USUARIO_INEXISTENTE_CPF, "qualquerSenha"),
            "Deveria lançar LoginException para usuário inexistente."
        );

        // 3. Verificar a mensagem de erro
        assertEquals("CPF ou senha invalidos.", exception.getMessage());

        // 4. Verificar interação com o mock
        Mockito.verify(usuarioDAO).buscarPorCpf(USUARIO_INEXISTENTE_CPF);
    }

    /*
     * 5. Funcionalidade "Lembrar-me": Simular login com parâmetro "lembrar" = "on".
     * (Aqui testamos o método de geração de token, parte integrante da funcionalidade).
     */
    @Test
    @DisplayName("TUM-33: Funcionalidade Lembrar-me - Geração de Token")
    void tum33_gerarTokenLembrarMe() throws Exception {
        // 1. Configurar o Mock: O método salvarTokenLembrarMe não retorna nada,
        //    apenas garantimos que não lança exceção.
        Mockito.doNothing().when(usuarioDAO).salvarTokenLembrarMe(anyInt(), anyString());

        // 2. Executar o método em teste
        int idUsuario = 15;
        String tokenGerado = assertDoesNotThrow(
            () -> loginService.gerarTokenLembrarMe(idUsuario),
            "Não deveria lançar exceção ao gerar o token."
        );

        // 3. Verificar o resultado: O token deve ser uma String não vazia (UUID)
        assertNotNull(tokenGerado, "O token gerado não deve ser nulo.");
        assertFalse(tokenGerado.isEmpty(), "O token gerado não deve ser vazio.");

        // 4. Verificar interação com o mock: Deve chamar o método salvarTokenLembrarMe
        //    exatamente uma vez com o ID correto e o token gerado (ou seja, não nulo/vazio)
        Mockito.verify(usuarioDAO).salvarTokenLembrarMe(idUsuario, tokenGerado);
    }

    /*
     * 6. Tratamento de Erro (Banco Offline): Simular lançamento de SQLException
     * ("Banco offline") ao chamar dao.buscarPorCpf().
     */
    @Test
    @DisplayName("TUM-34: Tratamento de Erro - Banco Offline (SQLException)")
    void tum34_tratamentoErroBancoOffline() throws Exception {
        // 1. Configurar o Mock: Fazer com que o DAO lance uma SQLException
        //    ao tentar buscarPorCpf.
        SQLException dbError = new SQLException("Banco offline");
        Mockito.when(usuarioDAO.buscarPorCpf(anyString())).thenThrow(dbError);

        // 2. Executar e verificar: O método autenticar deve propagar a SQLException
        SQLException exception = assertThrows(
            SQLException.class,
            () -> loginService.autenticar(CLIENTE_CPF_OK, CLIENTE_SENHA_OK),
            "Deveria lançar a SQLException original do DAO."
        );

        // 3. Verificar a mensagem de erro propagada
        assertEquals("Banco offline", exception.getMessage());

        // 4. Verificar interação com o mock
        Mockito.verify(usuarioDAO).buscarPorCpf(CLIENTE_CPF_OK);
    }
}