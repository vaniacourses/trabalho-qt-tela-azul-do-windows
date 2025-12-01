package br.uff.ic.grupo6.banco.integracao;

import br.uff.ic.grupo6.banco.dao.ConexaoDB;
import br.uff.ic.grupo6.banco.model.Investimento;
import br.uff.ic.grupo6.banco.service.InvestimentoService;
import br.uff.ic.grupo6.banco.service.exception.ValidationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DRIVER DE TESTE DE INTEGRAÇÃO (Bottom-Up) * Este teste atua como o "Driver"
 * (substituindo o Servlet) para testar a integração ascendente entre: 1.
 * InvestimentoService (Módulo sendo testado) 2. ContaDAO e InvestimentoDAO
 * (Módulos inferiores reais) 3. Banco de Dados MySQL (Infraestrutura real) *
 * NÃO HÁ STUBS/MOCKS: Todos os componentes abaixo do Service são reais.
 */
class InvestimentoServiceIntegrationTest {

	// A unidade que estamos "pilotando"
	private InvestimentoService investimentoService;

	// Conexão real para preparar o cenário no banco
	private Connection conexao;

	// IDs gerados para limpeza posterior
	private int idUsuarioTeste;
	private int idContaTeste;

	@BeforeEach
	void setUp() throws SQLException {
		// 1. Instancia o serviço REAL, confirmando a abordagem Bottom-Up (sem stubs
		// para a base).
		investimentoService = new InvestimentoService();

		// 2. Conecta ao banco
		conexao = ConexaoDB.getConexao();

		// 3. Cria dados reais no banco para o teste funcionar
		criarCenarioNoBanco();
	}

	@AfterEach
	void tearDown() throws SQLException {
		// 4. Limpeza: Remove os dados criados para não sujar o banco real
		limparCenarioNoBanco();
		if (conexao != null && !conexao.isClosed()) {
			conexao.close();
		}
	}

	@Test
	@DisplayName("Integração Bottom-Up: Service deve alterar saldo e gravar investimento no Banco Real")
	void testRealizarInvestimentoFluxoCompleto() throws SQLException, ValidationException {
		// CENÁRIO (Estado do Banco Real)
		// Conta criada no setUp() com saldo de R$ 2.000,00
		double valorInvestimento = 500.00;
		double saldoInicial = 2000.00;

		// O Driver chama o Módulo
		Investimento resultado = investimentoService.realizarInvestimento(idContaTeste, "CDB", valorInvestimento,
				saldoInicial);

		// Consulta direta no Banco
		// 1. Verifica retorno do objeto
		assertNotNull(resultado);
		assertNotNull(resultado.getId(), "O ID deve vir preenchido do banco de dados");

		// 2. Verifica se o SALDO mudou na tabela CONTA (Service -> ContaDAO -> MySQL)
		double saldoAtual = buscarSaldoNoBanco(idContaTeste);
		assertEquals(1500.00, saldoAtual, 0.01, "O saldo no banco deveria ter caído para 1500.00");

		// 3. Verifica se o registro foi criado na tabela INVESTIMENTO (Service ->
		// InvestimentoDAO -> MySQL)
		boolean investimentoExiste = verificarSeInvestimentoExiste(resultado.getId());
		assertTrue(investimentoExiste, "O registro deve existir na tabela INVESTIMENTO do banco real");
	}

	// ===
	// MÉTODOS AUXILIARES DO DRIVER (Manipulação direta do SQL)
	// ===

	private void criarCenarioNoBanco() throws SQLException {
		// Cria Usuário
		String sqlUser = "INSERT INTO USUARIO (nome, cpf, login, senha, tipo) VALUES ('Driver Bot', '99988877700', 'driver.bot', '123', 'CLIENTE')";
		PreparedStatement psUser = conexao.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS);
		psUser.executeUpdate();
		ResultSet rsUser = psUser.getGeneratedKeys();
		if (rsUser.next())
			idUsuarioTeste = rsUser.getInt(1);

		// Cria Conta com saldo inicial de 2000
		String sqlConta = "INSERT INTO CONTA (agencia, numero, saldo, id_usuario) VALUES ('0001', 'TEST-UP', 2000.00, ?)";
		PreparedStatement psConta = conexao.prepareStatement(sqlConta, Statement.RETURN_GENERATED_KEYS);
		psConta.setInt(1, idUsuarioTeste);
		psConta.executeUpdate();
		ResultSet rsConta = psConta.getGeneratedKeys();
		if (rsConta.next())
			idContaTeste = rsConta.getInt(1);
	}

	private void limparCenarioNoBanco() throws SQLException {
		if (idContaTeste > 0) {
			conexao.createStatement().execute("DELETE FROM INVESTIMENTO WHERE id_conta = " + idContaTeste);
			conexao.createStatement().execute("DELETE FROM CONTA WHERE id = " + idContaTeste);
		}
		if (idUsuarioTeste > 0) {
			conexao.createStatement().execute("DELETE FROM USUARIO WHERE id = " + idUsuarioTeste);
		}
	}

	private double buscarSaldoNoBanco(int idConta) throws SQLException {
		String sql = "SELECT saldo FROM CONTA WHERE id = ?";
		PreparedStatement ps = conexao.prepareStatement(sql);
		ps.setInt(1, idConta);
		ResultSet rs = ps.executeQuery();
		if (rs.next())
			return rs.getDouble("saldo");
		return -1.0;
	}

	private boolean verificarSeInvestimentoExiste(int idInvestimento) throws SQLException {
		String sql = "SELECT count(*) FROM INVESTIMENTO WHERE id = ?";
		PreparedStatement ps = conexao.prepareStatement(sql);
		ps.setInt(1, idInvestimento);
		ResultSet rs = ps.executeQuery();
		if (rs.next())
			return rs.getInt(1) > 0;
		return false;
	}
}