package br.uff.ic.grupo6.banco.mutacao;

import br.uff.ic.grupo6.banco.dao.ContaDAO;
import br.uff.ic.grupo6.banco.dao.InvestimentoDAO;
import br.uff.ic.grupo6.banco.model.Investimento;
import br.uff.ic.grupo6.banco.service.InvestimentoService;
import br.uff.ic.grupo6.banco.service.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Casos de Testes focados em matar os 5 Mutantes sobreviventes após testes com
 * PITclipse. Inclui testes estruturais adaptados para garantir cobertura
 * completa no PIT test.
 */
public class InvestimentoServiceMutationTest {

	private final ContaDAO contaDAO = mock(ContaDAO.class);
	private final InvestimentoDAO investimentoDAO = mock(InvestimentoDAO.class);
	private final InvestimentoService service = new InvestimentoService(contaDAO, investimentoDAO);

	// -------------------------
	// MATANDO MUTANTES DE BORDA
	// O erro muda < para <=
	// -------------------------

	@Test
	@DisplayName("Matar Mutante Linha 58: Investir TODO o saldo disponível (Saldo == Valor)")
	void devePermitirInvestirValorExatoDoSaldo() throws SQLException, ValidationException {
		when(contaDAO.realizarInvestimento(anyInt(), anyString(), anyDouble())).thenReturn(new Investimento());
		assertDoesNotThrow(() -> service.realizarInvestimento(1, "SELIC", 500, 500));
	}

	@Test
	@DisplayName("Matar Mutante Linha 66: Investir MÍNIMO exato de FII (1000)")
	void devePermitirInvestirMinimoExatoFII() throws SQLException, ValidationException {
		when(contaDAO.realizarInvestimento(anyInt(), anyString(), anyDouble())).thenReturn(new Investimento());
		assertDoesNotThrow(() -> service.realizarInvestimento(1, "FII", 1000, 5000));
	}

	@Test
	@DisplayName("Matar Mutante Linha 71: Investir MÍNIMO exato de CDB (100)")
	void devePermitirInvestirMinimoExatoCDB() throws SQLException, ValidationException {
		when(contaDAO.realizarInvestimento(anyInt(), anyString(), anyDouble())).thenReturn(new Investimento());
		assertDoesNotThrow(() -> service.realizarInvestimento(1, "CDB", 100, 5000));
	}

	// ---------------------------
	// MATANDO MUTANTES DE RETORNO
	// O erro faz retornar null ou lista vazia
	// ---------------------------

	@Test
	@DisplayName("Matar Mutante Linha 76: Garantir que realizarInvestimento não retorna NULL")
	void deveRetornarObjetoInvestimentoValido() throws SQLException, ValidationException {
		Investimento investimentoMock = new Investimento();
		investimentoMock.setId(999);
		when(contaDAO.realizarInvestimento(anyInt(), anyString(), anyDouble())).thenReturn(investimentoMock);

		Investimento resultado = service.realizarInvestimento(1, "SELIC", 200, 1000);

		assertNotNull(resultado, "O serviço não pode retornar nulo em caso de sucesso");
		assertEquals(999, resultado.getId());
	}

	@Test
	@DisplayName("Matar Mutante Linha 96: Garantir que buscarInvestimentos não retorna lista vazia quando há dados")
	void deveRetornarListaPreenchida() throws SQLException, ValidationException {
		List<Investimento> listaComDados = Collections.singletonList(new Investimento());
		when(investimentoDAO.buscarInvestimentosPorPeriodo(anyInt(), any(), any())).thenReturn(listaComDados);

		List<Investimento> resultado = service.buscarInvestimentos(1, LocalDate.now(), LocalDate.now());

		assertNotNull(resultado);
		assertFalse(resultado.isEmpty(), "A lista não deveria estar vazia se o DAO retornou dados");
		assertEquals(1, resultado.size());
	}

	// ---------------------------
	// TESTES ESTRUTURAIS ADAPTADOS (para cobertura do percentual de mutação)
	// ---------------------------

	// Tipo inválido
	@Test
	@DisplayName("TE-01: Tipo inválido (estrutura)")
	void testeEstruturalTipoInvalido() {
		// Antigo: InvestimentoServiceStructuralTest.caminhoTipoInvalido()
		ValidationException ex = assertThrows(ValidationException.class,
				() -> service.realizarInvestimento(1, "", 100, 500));
		assertEquals("Tipo de investimento invalido.", ex.getMessage());
		verifyNoInteractions(contaDAO);
	}

	// Saldo insuficiente
	@Test
	@DisplayName("TE-02: Saldo insuficiente (estrutura)")
	void testeEstruturalSaldoInsuficiente() {
		// Antigo: InvestimentoServiceStructuralTest.caminhoSaldoInsuficiente()
		ValidationException ex = assertThrows(ValidationException.class,
				() -> service.realizarInvestimento(1, "SELIC", 300, 200));
		assertEquals("Saldo insuficiente para realizar o investimento.", ex.getMessage());
	}

	// FII menor que mínimo
	@Test
	@DisplayName("TE-03: FII menor que 1000 (estrutura)")
	void testeEstruturalFIIMenorQue1000() {
		// Antigo: InvestimentoServiceStructuralTest.caminhoFIIMenorQue1000()
		ValidationException ex = assertThrows(ValidationException.class,
				() -> service.realizarInvestimento(1, "FII", 800, 5000));
		assertEquals("O valor minimo para aplicar em FII e R$ 1.000,00.", ex.getMessage());
	}

	// CDB menor que mínimo
	@Test
	@DisplayName("TE-04: CDB menor que 100 (estrutura)")
	void testeEstruturalCDBMenorQue100() {
		// Antigo: InvestimentoServiceStructuralTest.caminhoCDBMenorQue100()
		ValidationException ex = assertThrows(ValidationException.class,
				() -> service.realizarInvestimento(1, "CDB", 80, 5000));
		assertEquals("O valor minimo para aplicar em CDB e R$ 100,00.", ex.getMessage());
	}

	// Caminho feliz (happy path)
	@Test
	@DisplayName("TE-05: Caminho feliz (estrutura)")
	void testeEstruturalHappyPath() throws SQLException, ValidationException {
		// Antigo: InvestimentoServiceStructuralTest.caminhoHappyPath()
		when(contaDAO.realizarInvestimento(1, "SELIC", 200)).thenReturn(new Investimento());
		assertDoesNotThrow(() -> service.realizarInvestimento(1, "SELIC", 200, 500));
		verify(contaDAO).realizarInvestimento(1, "SELIC", 200);
	}

	// Tipo null
	@Test
	@DisplayName("TE-06: Tipo null (estrutura)")
	void testeEstruturalTipoNull() {
		// Antigo: InvestimentoServiceStructuralTest.caminhoTipoNull()
		ValidationException ex = assertThrows(ValidationException.class,
				() -> service.realizarInvestimento(1, null, 200, 500));
		assertEquals("Tipo de investimento invalido.", ex.getMessage());
		verifyNoInteractions(contaDAO);
	}

	// Tipo desconhecido
	@Test
	@DisplayName("TE-07: Tipo desconhecido (BTC)")
	void testeEstruturalTipoDesconhecido() {
		// Antigo: InvestimentoServiceStructuralTest.caminhoTipoDesconhecido()
		ValidationException ex = assertThrows(ValidationException.class,
				() -> service.realizarInvestimento(1, "BTC", 200, 500));
		assertEquals("Tipo de investimento invalido.", ex.getMessage());
	}

	// Valor zero
	@Test
	@DisplayName("TE-08: Valor zero (estrutura)")
	void testeEstruturalValorZero() {
		// Antigo: InvestimentoServiceStructuralTest.caminhoValorZero()
		ValidationException ex = assertThrows(ValidationException.class,
				() -> service.realizarInvestimento(1, "SELIC", 0, 500));
		assertEquals("O valor do investimento deve ser positivo.", ex.getMessage());
	}

	// Valor negativo
	@Test
	@DisplayName("TE-09: Valor negativo (estrutura)")
	void testeEstruturalValorNegativo() {
		// Antigo: InvestimentoServiceStructuralTest.caminhoValorNegativo()
		ValidationException ex = assertThrows(ValidationException.class,
				() -> service.realizarInvestimento(1, "SELIC", -10, 500));
		assertEquals("O valor do investimento deve ser positivo.", ex.getMessage());
	}

	// Buscar investimentos com datas válidas
	@Test
	@DisplayName("TE-10: Buscar investimentos com datas válidas (estrutura)")
	void testeEstruturalBuscarDatasValidas() throws SQLException, ValidationException {
		// Antigo: InvestimentoServiceStructuralTest.caminhoBuscarDatasValidas()
		when(investimentoDAO.buscarInvestimentosPorPeriodo(anyInt(), any(), any())).thenReturn(Collections.emptyList());
		assertDoesNotThrow(() -> service.buscarInvestimentos(1, LocalDate.of(2023, 1, 1), LocalDate.of(2023, 1, 2)));
	}

	// Buscar investimentos com datas nulas
	@Test
	@DisplayName("TE-11: Buscar investimentos com datas nulas (estrutura)")
	void testeEstruturalBuscarDatasNulas() throws SQLException, ValidationException {
		// Antigo: InvestimentoServiceStructuralTest.caminhoBuscarDatasNulas()
		when(investimentoDAO.buscarInvestimentosPorPeriodo(1, null, null)).thenReturn(Collections.emptyList());
		assertDoesNotThrow(() -> service.buscarInvestimentos(1, null, null));
	}

	// Data início maior que data fim
	@Test
	@DisplayName("TE-12: Data início > data fim (estrutura)")
	void testeEstruturalBuscarDataInvalida() {
		// Antigo: InvestimentoServiceStructuralTest.caminhoBuscarDataInvalida()
		ValidationException ex = assertThrows(ValidationException.class,
				() -> service.buscarInvestimentos(1, LocalDate.of(2023, 12, 31), LocalDate.of(2023, 1, 1)));
		assertEquals("A data de inicio deve ser anterior ou igual a data de fim.", ex.getMessage());
	}
}
