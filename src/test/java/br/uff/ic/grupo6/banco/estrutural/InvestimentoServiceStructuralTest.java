package br.uff.ic.grupo6.banco.estrutural;

import br.uff.ic.grupo6.banco.dao.ContaDAO;
import br.uff.ic.grupo6.banco.dao.InvestimentoDAO;
import br.uff.ic.grupo6.banco.model.Investimento;
import br.uff.ic.grupo6.banco.service.InvestimentoService;
import br.uff.ic.grupo6.banco.service.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes Estruturais — Critério: todas as arestas. O objetivo aqui é percorrer
 * explicitamente todos os ramos internos do método realizarInvestimento().
 */
public class InvestimentoServiceStructuralTest {

	private final ContaDAO contaDAO = mock(ContaDAO.class);
	private final InvestimentoDAO investimentoDAO = mock(InvestimentoDAO.class);

	private final InvestimentoService service = new InvestimentoService(contaDAO, investimentoDAO);

	// -----
	// TE-01 — Caminho da aresta: tipo inválido (ramo esquerda do 1º if)
	// -----
	@Test
	@DisplayName("TE-01: Caminho da aresta do tipo inválido")
	void caminhoTipoInvalido() {
		ValidationException ex = assertThrows(ValidationException.class,
				() -> service.realizarInvestimento(1, "", 100, 500));

		assertEquals("Tipo de investimento invalido.", ex.getMessage());
		verifyNoInteractions(contaDAO);
	}

	// -----
	// TE-02 — Caminho da aresta: valor positivo, mas saldo insuficiente (ramo do 3º
	// if)
	// -----
	@Test
	@DisplayName("TE-02: Caminho da aresta de saldo insuficiente")
	void caminhoSaldoInsuficiente() {
		ValidationException ex = assertThrows(ValidationException.class,
				() -> service.realizarInvestimento(1, "SELIC", 300, 200));

		assertEquals("Saldo insuficiente para realizar o investimento.", ex.getMessage());
	}

	// -----
	// TE-03 — Caminho da aresta: tipo = FII & valor < 1000 (ramo do if composto)
	// -----
	@Test
	@DisplayName("TE-03: Caminho da aresta da regra FII < 1000")
	void caminhoFIIMenorQue1000() {
		ValidationException ex = assertThrows(ValidationException.class,
				() -> service.realizarInvestimento(1, "FII", 800, 5000));

		assertEquals("O valor minimo para aplicar em FII e R$ 1.000,00.", ex.getMessage());
	}

	// -----
	// TE-04 — Caminho da aresta: tipo = CDB & valor < 100 (ramo exclusivo do
	// último if)
	// -----
	@Test
	@DisplayName("TE-04: Caminho da aresta da regra CDB < 100")
	void caminhoCDBMenorQue100() {
		ValidationException ex = assertThrows(ValidationException.class,
				() -> service.realizarInvestimento(1, "CDB", 80, 5000));

		assertEquals("O valor minimo para aplicar em CDB e R$ 100,00.", ex.getMessage());
	}

	// ------
	// TE-05 — Caminho final (caminho feliz) indo até o DAO (ramo default)
	// ------
	@Test
	@DisplayName("TE-05: Caminho que alcança o DAO (ramo positivo final)")
	void caminhoHappyPath() throws SQLException, ValidationException {

		when(contaDAO.realizarInvestimento(1, "SELIC", 200)).thenReturn(new Investimento());

		assertDoesNotThrow(() -> service.realizarInvestimento(1, "SELIC", 200, 500));

		verify(contaDAO).realizarInvestimento(1, "SELIC", 200);
	}

	// -----
	// TE-06 — Caminho da aresta: tipoInvestimento = null
	// -----
	@Test
	@DisplayName("TE-06: Caminho da aresta do tipo null")
	void caminhoTipoNull() {
		ValidationException ex = assertThrows(ValidationException.class,
				() -> service.realizarInvestimento(1, null, 200, 500));
		assertEquals("Tipo de investimento invalido.", ex.getMessage());
		verifyNoInteractions(contaDAO);
	}

	// -----
	// TE-07 — Caminho da aresta: tipo inválido não-vazio (ex: BTC)
	// -----
	@Test
	@DisplayName("TE-07: Caminho da aresta do tipo não reconhecido (BTC)")
	void caminhoTipoDesconhecido() {
		ValidationException ex = assertThrows(ValidationException.class,
				() -> service.realizarInvestimento(1, "BTC", 200, 500));
		assertEquals("Tipo de investimento invalido.", ex.getMessage());
	}

	// -----
	// TE-08 — Caminho da aresta: valorInvestimento = 0
	// -----
	@Test
	@DisplayName("TE-08: Caminho da aresta valor = 0")
	void caminhoValorZero() {
		ValidationException ex = assertThrows(ValidationException.class,
				() -> service.realizarInvestimento(1, "SELIC", 0, 500));
		assertEquals("O valor do investimento deve ser positivo.", ex.getMessage());
	}

	// -----
	// TE-09 — Caminho da aresta: valorInvestimento < 0
	// -----
	@Test
	@DisplayName("TE-09: Caminho da aresta valor negativo")
	void caminhoValorNegativo() {
		ValidationException ex = assertThrows(ValidationException.class,
				() -> service.realizarInvestimento(1, "SELIC", -10, 500));
		assertEquals("O valor do investimento deve ser positivo.", ex.getMessage());
	}

	// -----
	// TE-10 — Caminho da aresta: buscarInvestimentos com datas válidas
	// -----
	@Test
	@DisplayName("TE-10: Aresta de busca com datas válidas")
	void caminhoBuscarDatasValidas() throws SQLException, ValidationException {
		when(investimentoDAO.buscarInvestimentosPorPeriodo(anyInt(), any(), any()))
				.thenReturn(java.util.Collections.emptyList());

		assertDoesNotThrow(() -> service.buscarInvestimentos(1, java.time.LocalDate.of(2023, 1, 1),
				java.time.LocalDate.of(2023, 1, 2)));
	}

	// -----
	// TE-11 — Caminho da aresta: buscarInvestimentos com datas nulas
	// -----
	@Test
	@DisplayName("TE-11: Aresta de busca com datas nulas (sem filtro)")
	void caminhoBuscarDatasNulas() throws SQLException, ValidationException {
		when(investimentoDAO.buscarInvestimentosPorPeriodo(1, null, null))
				.thenReturn(java.util.Collections.emptyList());

		assertDoesNotThrow(() -> service.buscarInvestimentos(1, null, null));
	}

	// -----
	// TE-12 — Caminho da aresta: dataInicio > dataFim (gera exceção)
	// -----
	@Test
	@DisplayName("TE-12: Aresta de busca com dataInicio > dataFim")
	void caminhoBuscarDataInvalida() {
		ValidationException ex = assertThrows(ValidationException.class, () -> service.buscarInvestimentos(1,
				java.time.LocalDate.of(2023, 12, 31), java.time.LocalDate.of(2023, 1, 1)));
		assertEquals("A data de inicio deve ser anterior ou igual a data de fim.", ex.getMessage());
	}

}
