package br.uff.ic.grupo6.banco.sistema;

import br.uff.ic.grupo6.banco.dao.ContaDAO;
import br.uff.ic.grupo6.banco.dao.UsuarioDAO;
import br.uff.ic.grupo6.banco.model.Cliente;
import br.uff.ic.grupo6.banco.model.Conta;
import br.uff.ic.grupo6.banco.service.TransacaoService;
import br.uff.ic.grupo6.banco.service.exception.ValidationException;

import br.uff.ic.grupo6.banco.dao.ConexaoDB;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TransacaoServiceSistemaTest {

    private ContaDAO contaDAO;
    private UsuarioDAO usuarioDAO;
    private TransacaoService service;

    @BeforeEach
    void limparBanco() throws Exception {
        try (Connection conn = ConexaoDB.getConexao();
             Statement st = conn.createStatement()) {

            st.executeUpdate("DELETE FROM CLIENTE");
            st.executeUpdate("DELETE FROM CONTA");
        }
    }

    @BeforeEach
    void setup() {
        contaDAO = new ContaDAO();
        usuarioDAO = new UsuarioDAO();
        service = new TransacaoService(contaDAO, null, usuarioDAO);
    }

    // ---------------------------
    // FUNÇÃO AUXILIAR
    // ---------------------------

    private Cliente inserirCliente(String agencia, String numeroConta, double saldo,
                                   String nome, double renda) throws Exception {

        try (Connection conn = ConexaoDB.getConexao();
             Statement st = conn.createStatement()) {

            st.executeUpdate(
                    "INSERT INTO CONTA (agencia, numero, saldo) " +
                    "VALUES ('" + agencia + "', '" + numeroConta + "', " + saldo + ")"
            );

            var rs = st.executeQuery("SELECT id FROM CONTA WHERE numero = '" + numeroConta + "'");
            rs.next();
            int idConta = rs.getInt("id");

            st.executeUpdate(
                    "INSERT INTO CLIENTE (nome, renda, id_conta) " +
                    "VALUES ('" + nome + "', " + renda + ", " + idConta + ")"
            );

            Cliente c = new Cliente();  // usa o construtor vazio
			c.setNome(nome);
			c.setRenda(renda);
			c.setCpf("00000000000");   // obrigatório caso o sistema exija
			c.setLogin("login_" + nome); 
			c.setSenha("123");         // ajustável conforme a classe Usuario
			
			Conta conta = new Conta(agencia, numeroConta, saldo);
			conta.setId(idConta);
			
			c.setConta(conta);
			
			return c;
        }
    }

    // ------------------------------------------------
    // TESTES DE SISTEMA
    // ------------------------------------------------

    @Test
    @Order(1)
    void devePermitirTransferenciaComSucesso() throws Exception {

        Cliente origem = inserirCliente("1111", "12345", 3000, "Origem", 5000);
        Cliente destino = inserirCliente("2222", "56789", 2000, "Destino", 4000);

        Map<String, Object> dados = service.prepararTransferencia(
                origem, "2222", "56789", 500
        );

        assertNotNull(dados);
        assertEquals(destino.getConta().getNumero(), 
                     ((Conta) dados.get("contaDeDestino")).getNumero());
        assertEquals(500.0, dados.get("valor"));
    }

    @Test
    @Order(2)
    void deveFalharQuandoSaldoInsuficiente() throws Exception {
        Cliente origem = inserirCliente("1111", "12345", 100, "Origem", 5000);
        inserirCliente("2222", "56789", 2000, "Destino", 4000);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.prepararTransferencia(origem, "2222", "56789", 300)
        );

        assertEquals("Saldo insuficiente", ex.getMessage());
    }

    @Test
    @Order(3)
    void deveFalharQuandoContaDestinoNaoExiste() throws Exception {
        Cliente origem = inserirCliente("1111", "12345", 3000, "Origem", 5000);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.prepararTransferencia(origem, "9999", "0000", 200)
        );

        assertEquals("Conta de destino nao encontrada", ex.getMessage());
    }

    @Test
    @Order(4)
    void deveFalharQuandoAgenciaBloqueada() throws Exception {
        Cliente origem = inserirCliente("1111", "12345", 3000, "Origem", 5000);

        // Criar conta real (existente) para não cair na validação de "conta não encontrada"
        inserirCliente("9999", "91234", 1000, "Fake", 3000);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.prepararTransferencia(origem, "9999", "91234", 200)
        );

        assertEquals("Nao e possível transferir para esta agencia.", ex.getMessage());
    }

    @Test
    @Order(5)
    void deveFalharQuandoClienteDestinoNaoExiste() throws Exception {

        Cliente origem = inserirCliente("1111", "12345", 3000, "Origem", 5000);

        // Somente cria a conta, mas não cria o cliente vinculado → cliente destino inexistente
        try (Connection conn = ConexaoDB.getConexao();
             Statement st = conn.createStatement()) {

            st.executeUpdate(
                    "INSERT INTO CONTA(agencia, numero, saldo) VALUES ('2222', '56789', 2000)"
            );
        }

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.prepararTransferencia(origem, "2222", "56789", 200)
        );

        assertEquals("Cliente de destino nao encontrado", ex.getMessage());
    }
}
