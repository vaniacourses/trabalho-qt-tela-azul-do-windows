package br.uff.ic.grupo6.banco.funcional;

import br.uff.ic.grupo6.banco.dao.ContaDAO;
import br.uff.ic.grupo6.banco.dao.InvestimentoDAO;
import br.uff.ic.grupo6.banco.model.Investimento;
import br.uff.ic.grupo6.banco.service.InvestimentoService;
import br.uff.ic.grupo6.banco.service.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes Funcionais para InvestimentoService. Foco: Regras de Negócio (Lógica,
 * Limites e Classes de Equivalência).
 */
@ExtendWith(MockitoExtension.class)
class InvestimentoServiceFunctionalTest {

	@Mock
	private ContaDAO contaDAO;

	@Mock
	private InvestimentoDAO investimentoDAO;

	@InjectMocks
	private InvestimentoService service;

	private final int ID_CONTA = 1;

	@BeforeEach
	void setUp() throws SQLException {
		// Mock padrão para chamadas válidas
		lenient().when(contaDAO.realizarInvestimento(anyInt(), anyString(), anyDouble()))
				.thenReturn(new Investimento());
	}

	// ---------
	// 1. TESTES - ANÁLISE DE VALOR LIMITE (AVL)
	// ---------

	@Test
	@DisplayName("CT-F-01: AVL - CDB - Limite Inferior Válido (R$ 100.00)")
	void testCDBLimiteValido() throws SQLException, ValidationException {
		assertDoesNotThrow(() -> service.realizarInvestimento(ID_CONTA, "CDB", 100.00, 500.00));
		verify(contaDAO).realizarInvestimento(ID_CONTA, "CDB", 100.00);
	}

	@Test
	@DisplayName("CT-F-02: AVL - CDB - Limite Inferior Inválido (R$ 99.99)")
	void testCDBLimiteInvalido() {
		ValidationException erro = assertThrows(ValidationException.class,
				() -> service.realizarInvestimento(ID_CONTA, "CDB", 99.99, 500.00));
		assertEquals("O valor minimo para aplicar em CDB e R$ 100,00.", erro.getMessage());
	}

	@Test
	@DisplayName("CT-F-03: AVL - FII - Limite Inferior Válido (R$ 1000.00)")
	void testFIILimiteValido() throws SQLException, ValidationException {
		assertDoesNotThrow(() -> service.realizarInvestimento(ID_CONTA, "FII", 1000.00, 2000.00));
		verify(contaDAO).realizarInvestimento(ID_CONTA, "FII", 1000.00);
	}

	@Test
	@DisplayName("CT-F-04: AVL - FII - Limite Inferior Inválido (R$ 999.99)")
	void testFIILimiteInvalido() {
		ValidationException erro = assertThrows(ValidationException.class,
				() -> service.realizarInvestimento(ID_CONTA, "FII", 999.99, 2000.00));
		assertEquals("O valor minimo para aplicar em FII e R$ 1.000,00.", erro.getMessage());
	}

	@Test
	@DisplayName("CT-F-05: AVL - Saldo - Limite Superior Válido (Valor = Saldo)")
	void testSaldoLimiteIgual() throws SQLException, ValidationException {
		assertDoesNotThrow(() -> service.realizarInvestimento(ID_CONTA, "SELIC", 500.00, 500.00));
	}

	@Test
	@DisplayName("CT-F-06: AVL - Saldo - Limite Superior Inválido (Valor > Saldo)")
	void testSaldoInsuficienteLimite() {
		ValidationException erro = assertThrows(ValidationException.class,
				() -> service.realizarInvestimento(ID_CONTA, "SELIC", 500.01, 500.00));
		assertEquals("Saldo insuficiente para realizar o investimento.", erro.getMessage());
	}

	// ---------
	// 2. TESTES - PARTICIONAMENTO EM CLASSES DE EQUIVALÊNCIA (PCE)
	// ---------

	// P C E — CLASSES INVÁLIDAS

	@Test
	@DisplayName("CT-F-07: PCE - Tipo inválido (classe inválida I1)")
	void testTipoInvalido() {
		ValidationException e = assertThrows(ValidationException.class,
				() -> service.realizarInvestimento(ID_CONTA, "BTC", 100, 500));
		assertEquals("Tipo de investimento invalido.", e.getMessage());
	}

	@Test
	@DisplayName("CT-F-08: PCE - Tipo null (classe inválida I2)")
	void testTipoNull() {
		ValidationException e = assertThrows(ValidationException.class,
				() -> service.realizarInvestimento(ID_CONTA, null, 100, 500));
		assertEquals("Tipo de investimento invalido.", e.getMessage());
	}

	@Test
	@DisplayName("CT-F-09: PCE - Tipo vazio (classe inválida I3)")
	void testTipoVazio() {
		ValidationException e = assertThrows(ValidationException.class,
				() -> service.realizarInvestimento(ID_CONTA, "  ", 100, 500));
		assertEquals("Tipo de investimento invalido.", e.getMessage());
	}

	@Test
	@DisplayName("CT-F-10: PCE - Valor = 0 (classe inválida I4)")
	void testValorZero() {
		ValidationException e = assertThrows(ValidationException.class,
				() -> service.realizarInvestimento(ID_CONTA, "SELIC", 0, 500));
		assertEquals("O valor do investimento deve ser positivo.", e.getMessage());
	}

	@Test
	@DisplayName("CT-F-11: PCE - Valor negativo (classe inválida I5)")
	void testValorNegativo() {
		ValidationException e = assertThrows(ValidationException.class,
				() -> service.realizarInvestimento(ID_CONTA, "SELIC", -10, 500));
		assertEquals("O valor do investimento deve ser positivo.", e.getMessage());
	}

	@Test
	@DisplayName("CT-F-12: PCE - Saldo insuficiente (classe inválida I6)")
	void testSaldoInsuficiente() {
		ValidationException e = assertThrows(ValidationException.class,
				() -> service.realizarInvestimento(ID_CONTA, "SELIC", 200, 100));
		assertEquals("Saldo insuficiente para realizar o investimento.", e.getMessage());
	}

	@Test
	@DisplayName("CT-F-13: PCE - FII com valor abaixo do mínimo (classe inválida I7)")
	void testFIIValorMinimoInvalido() {
		ValidationException e = assertThrows(ValidationException.class,
				() -> service.realizarInvestimento(ID_CONTA, "FII", 500, 2000));
		assertEquals("O valor minimo para aplicar em FII e R$ 1.000,00.", e.getMessage());
	}

	@Test
	@DisplayName("CT-F-14: PCE - CDB com valor abaixo do mínimo (classe inválida I8)")
	void testCDBValorMinimoInvalido() {
		ValidationException e = assertThrows(ValidationException.class,
				() -> service.realizarInvestimento(ID_CONTA, "CDB", 50, 500));
		assertEquals("O valor minimo para aplicar em CDB e R$ 100,00.", e.getMessage());
	}

	// P C E — CLASSES VÁLIDAS

	@Test
	@DisplayName("CT-F-15: PCE - Caso válido Tesouro SELIC")
	void testValidoSELIC() throws Exception {
		assertDoesNotThrow(() -> service.realizarInvestimento(ID_CONTA, "SELIC", 200, 500));
	}

	@Test
	@DisplayName("CT-F-16: PCE - Caso válido POUPANCA")
	void testValidoPoupanca() throws Exception {
		assertDoesNotThrow(() -> service.realizarInvestimento(ID_CONTA, "POUPANCA", 50, 200));
	}

	@Test
	@DisplayName("CT-F-17: PCE - Caso válido CDB (classe válida)")
	void testValidoCDB() throws Exception {
		assertDoesNotThrow(() -> service.realizarInvestimento(ID_CONTA, "CDB", 200, 500));
	}

	@Test
	@DisplayName("CT-F-18: PCE - Caso válido FII (classe válida)")
	void testValidoFII() throws Exception {
		assertDoesNotThrow(() -> service.realizarInvestimento(ID_CONTA, "FII", 1500, 3000));
	}

}
