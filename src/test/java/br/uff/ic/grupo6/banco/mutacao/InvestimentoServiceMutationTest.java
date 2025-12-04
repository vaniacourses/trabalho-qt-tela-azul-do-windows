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

}
