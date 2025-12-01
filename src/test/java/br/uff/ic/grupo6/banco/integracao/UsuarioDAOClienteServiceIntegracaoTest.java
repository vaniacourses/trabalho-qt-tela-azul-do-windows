package br.uff.ic.grupo6.banco.integracao;

import br.uff.ic.grupo6.banco.dao.UsuarioDAO;
import br.uff.ic.grupo6.banco.model.Cliente;
import br.uff.ic.grupo6.banco.service.ClienteService;
import br.uff.ic.grupo6.banco.service.exception.ValidationException;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Teste de integração REAL entre UsuarioDAO e ClienteService.
 *
 * Pré-requisitos do ambiente:
 * - MySQL rodando e acessível conforme `ConexaoDB` (jdbc:mysql://localhost:3306/banco_atm, user "grupo", senha "123").
 * - Banco com schema criado. Você pode usar `scripts/banco_inicial.sql`.
 *   Exemplo rápido:
 *   1) crie DB: CREATE DATABASE banco_atm;
 *   2) USE banco_atm; e execute o conteúdo do arquivo SQL.
 *
 * Este teste insere um cliente, recupera, valida campos, atualiza email e senha
 * e por fim exclui o cliente (opcional), tudo usando DAO/Service reais.
 */
public class UsuarioDAOClienteServiceIntegracaoTest {

    private UsuarioDAO usuarioDAO;
    private ClienteService clienteService;

    @BeforeEach
    void setup() {
        usuarioDAO = new UsuarioDAO();
        clienteService = new ClienteService(usuarioDAO);
    }

    @Test
    @DisplayName("Fluxo completo: cadastrar, buscar, atualizar email/senha e excluir")
    void fluxoCompletoCliente() throws Exception {
        // Massa de dados única
        String cpf = gerarCpf();
        String nome = "Cliente Integracao " + UUID.randomUUID();
        String email = "cli." + UUID.randomUUID() + "@example.com";
        String telefone = "21999999999";
        String senha = "Senha123"; // atende padrão (>=8, letra e número)

        // 1) Monta um cliente válido e cadastra pelo Service (vai usar UsuarioDAO real)
        Cliente c = new Cliente(cpf, senha, nome, cpf);
        c.setDataNascimento(LocalDate.now().minusYears(25));
        c.setEmail(email);
        c.setTelefone(telefone);
        c.setCep("20000000");
        c.setEndereco("Rua Teste, 123");
        c.setBairro("Centro");
        c.setCidade("Rio de Janeiro");
        c.setEstado("RJ");
        c.setRenda(1000.0);
        c.setOcupacao("Engenheiro");

        assertDoesNotThrow(() -> clienteService.cadastrarNovoCliente(c, senha));

        // 2) Recupera pelo CPF e valida campos essenciais
        Cliente recuperado = usuarioDAO.buscarClientePorId(buscarIdPorCpf(cpf));
        assertNotNull(recuperado);
        assertEquals(cpf, recuperado.getCpf());
        assertEquals(nome, recuperado.getNome());
        assertEquals(email, recuperado.getEmail());
        assertEquals("RJ", recuperado.getEstado());
        assertNotNull(recuperado.getConta()); // conta criada junto

        // 3) Atualiza email pelo Service (chama UsuarioDAO.atualizarCliente)
        String novoEmail = "novo." + UUID.randomUUID() + "@example.com";
        recuperado.setEmail(novoEmail);
        clienteService.atualizarCliente(recuperado);

        Cliente aposEmail = usuarioDAO.buscarClientePorId(recuperado.getId());
        assertEquals(novoEmail, aposEmail.getEmail());

        // 4) Atualiza senha pelo Service (valida atual, força, etc.)
        String novaSenha = "NovaSenha123";
        assertDoesNotThrow(() -> clienteService.atualizarSenha(recuperado.getId(), senha, novaSenha, novaSenha, senha));

        // Confirma senha no banco
        Cliente aposSenha = usuarioDAO.buscarClientePorId(recuperado.getId());
        assertEquals(novaSenha, aposSenha.getSenha());

        // 5) Opcional: excluir e garantir que não existe mais
        usuarioDAO.excluirClientePorId(recuperado.getId());
        Cliente depoisExclusao = usuarioDAO.buscarClientePorId(recuperado.getId());
        assertNull(depoisExclusao);
    }

    // Utilitário: busca o ID do usuário a partir do CPF
    private int buscarIdPorCpf(String cpf) throws SQLException {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/banco_atm?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                "grupo",
                "123");
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id FROM USUARIO WHERE cpf='" + cpf + "'")) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            throw new SQLException("Usuário não encontrado para CPF: " + cpf);
        }
    }

    private String gerarCpf() {
        String base = UUID.randomUUID().toString().replaceAll("[^0-9]", "");
        if (base.length() < 11) {
            base = (base + "00000000000").substring(0, 11);
        }
        return base.substring(0, 11);
    }
}
