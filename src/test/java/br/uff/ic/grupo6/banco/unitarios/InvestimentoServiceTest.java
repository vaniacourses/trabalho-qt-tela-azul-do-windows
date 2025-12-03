package br.uff.ic.grupo6.banco.unitarios;

import br.uff.ic.grupo6.banco.dao.ContaDAO;
import br.uff.ic.grupo6.banco.dao.InvestimentoDAO;
import br.uff.ic.grupo6.banco.model.Investimento;
import br.uff.ic.grupo6.banco.service.InvestimentoService;
import br.uff.ic.grupo6.banco.service.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes Unitários para InvestimentoService. 
 * Isolamento: Mockito é usado para simular o comportamento dos 
 * DAOs (ContaDAO e InvestimentoDAO). 
 * Foco: Regras de negócio, validações e tratamento de exceções. 
 */
@ExtendWith(MockitoExtension.class)
class InvestimentoServiceTest {

	// DAO mockado, pois não queremos acessar o banco de dados real nestes
	// testes unitários
	@Mock
	private ContaDAO contaDAO;

	@Mock
	private InvestimentoDAO investimentoDAO;

	// Injetados os mocks na instância do Service que será testada
	@InjectMocks
	private InvestimentoService service;

	// 1. TESTES DE SUCESSO (Caminho Feliz)

	@Test
	@DisplayName("Deve realizar investimento com sucesso quando dados são válidos (SELIC)")
	void realizarInvestimento_DadosValidos_Sucesso() throws SQLException, ValidationException {

		int idConta = 1;
		String tipo = "SELIC";
		double valor = 500.00;
		double saldo = 1000.00;

		Investimento investimentoEsperado = new Investimento();
		investimentoEsperado.setTipoInvestimento(tipo);
		investimentoEsperado.setValorAplicado(BigDecimal.valueOf(valor));

		// Stubbing: configura o mock do DAO para retornar sucesso quando chamado
		when(contaDAO.realizarInvestimento(idConta, tipo, valor)).thenReturn(investimentoEsperado);

		// Execução
		Investimento resultado = service.realizarInvestimento(idConta, tipo, valor, saldo);

		// Verificação
		assertNotNull(resultado);
		assertEquals(tipo, resultado.getTipoInvestimento());
		// Verifica se o método do DAO foi chamado exatamente 1 vez com os parâmetros
		// corretos
		verify(contaDAO, times(1)).realizarInvestimento(idConta, tipo, valor);
	}

	@Test
	@DisplayName("Deve permitir investimento em FII com valor igual ou maior que 1000")
	void realizarInvestimento_FIIGrande_Sucesso() throws SQLException, ValidationException {

		when(contaDAO.realizarInvestimento(anyInt(), anyString(), anyDouble())).thenReturn(new Investimento());

		// Limite inferior exato: 1000
		assertDoesNotThrow(() -> service.realizarInvestimento(1, "FII", 1000.0, 5000.0));
	}

	@Test
	@DisplayName("Deve permitir investimento em CDB com valor igual ou maior que 100")
	void realizarInvestimento_CDBValido_Sucesso() throws SQLException, ValidationException {

		when(contaDAO.realizarInvestimento(anyInt(), anyString(), anyDouble())).thenReturn(new Investimento());

		// Limite inferior exato: 100
		assertDoesNotThrow(() -> service.realizarInvestimento(1, "CDB", 100.0, 5000.0));
	}

	// 2. TESTES DE VALIDAÇÃO (Regras de Negócio e Entradas Inválidas)

	@Test
	@DisplayName("Deve lançar ValidationException se o tipo de investimento for nulo")
	void realizarInvestimento_TipoNulo_LancaException() {
		ValidationException exception = assertThrows(ValidationException.class, () -> {
			service.realizarInvestimento(1, null, 100.0, 1000.0);
		});
		assertEquals("Tipo de investimento invalido.", exception.getMessage());
		// Garante que o banco nunca é chamado se a validação falhar
		verifyNoInteractions(contaDAO);
	}

	@Test
	@DisplayName("Deve lançar ValidationException se o tipo de investimento for desconhecido")
	void realizarInvestimento_TipoDesconhecido_LancaException() {
		ValidationException exception = assertThrows(ValidationException.class, () -> {
			service.realizarInvestimento(1, "BITCOIN", 100.0, 1000.0);
		});
		assertEquals("Tipo de investimento invalido.", exception.getMessage());
	}

	@Test
	@DisplayName("Deve lançar ValidationException se o valor do investimento for zero ou negativo")
	void realizarInvestimento_ValorZeroOuNegativo_LancaException() {
		ValidationException exception = assertThrows(ValidationException.class, () -> {
			service.realizarInvestimento(1, "CDB", 0.0, 1000.0);
		});
		assertEquals("O valor do investimento deve ser positivo.", exception.getMessage());
	}

	@Test
	@DisplayName("Deve lançar ValidationException se o saldo for insuficiente")
	void realizarInvestimento_SaldoInsuficiente_LancaException() {
		// Tenta investir 1000 tendo apenas 500
		ValidationException exception = assertThrows(ValidationException.class, () -> {
			service.realizarInvestimento(1, "CDB", 1000.0, 500.0);
		});
		assertEquals("Saldo insuficiente para realizar o investimento.", exception.getMessage());
	}

	@Test
	@DisplayName("Deve lançar ValidationException para FII com valor menor que 1000")
	void realizarInvestimento_FIIMenorQueMil_LancaException() {
		ValidationException exception = assertThrows(ValidationException.class, () -> {
			service.realizarInvestimento(1, "FII", 999.99, 5000.0);
		});
		assertEquals("O valor minimo para aplicar em FII e R$ 1.000,00.", exception.getMessage());
	}

	@Test
	@DisplayName("Deve lançar ValidationException para CDB com valor menor que 100")
	void realizarInvestimento_CDBMenorQueCem_LancaException() {
		ValidationException exception = assertThrows(ValidationException.class, () -> {
			service.realizarInvestimento(1, "CDB", 99.99, 5000.0);
		});
		assertEquals("O valor minimo para aplicar em CDB e R$ 100,00.", exception.getMessage());
	}

	// 3. TESTE DE TRATAMENTO DE EXCEÇÃO (Simulação de Erro do Banco)

	@Test
	@DisplayName("Deve propagar SQLException quando o DAO falhar")
	void realizarInvestimento_ErroBanco_PropagaException() throws SQLException {
		// Simula erro de conexão no DAO
		when(contaDAO.realizarInvestimento(anyInt(), anyString(), anyDouble()))
				.thenThrow(new SQLException("Erro de conexão com o banco"));

		// Verifica se a exceção sobe para o Controller
		assertThrows(SQLException.class, () -> {
			service.realizarInvestimento(1, "SELIC", 500.0, 1000.0);
		});
	}

	// 4. TESTES DE BUSCA (Extrato de Investimentos)

	@Test
	@DisplayName("Deve buscar investimentos com sucesso quando datas são válidas")
	void buscarInvestimentos_DatasValidas_Sucesso() throws SQLException, ValidationException {

		LocalDate inicio = LocalDate.of(2023, 1, 1);
		LocalDate fim = LocalDate.of(2023, 12, 31);
		List<Investimento> listaEsperada = Arrays.asList(new Investimento(), new Investimento());

		when(investimentoDAO.buscarInvestimentosPorPeriodo(1, inicio, fim)).thenReturn(listaEsperada);

		List<Investimento> resultado = service.buscarInvestimentos(1, inicio, fim);

		assertEquals(2, resultado.size());
		verify(investimentoDAO).buscarInvestimentosPorPeriodo(1, inicio, fim);
	}

	@Test
	@DisplayName("Deve lançar ValidationException se Data Inicio for posterior a Data Fim")
	void buscarInvestimentos_DataInicioMaiorQueFim_LancaException() {
		LocalDate inicio = LocalDate.of(2023, 12, 31);
		LocalDate fim = LocalDate.of(2023, 1, 1);

		ValidationException exception = assertThrows(ValidationException.class, () -> {
			service.buscarInvestimentos(1, inicio, fim);
		});
		assertEquals("A data de inicio deve ser anterior ou igual a data de fim.", exception.getMessage());
	}

	@Test
	@DisplayName("Deve buscar investimentos se datas forem nulas (sem filtro)")
	void buscarInvestimentos_DatasNulas_Sucesso() throws SQLException, ValidationException {
		// DAO aceita nulls
		when(investimentoDAO.buscarInvestimentosPorPeriodo(1, null, null)).thenReturn(Arrays.asList());

		assertDoesNotThrow(() -> service.buscarInvestimentos(1, null, null));
		verify(investimentoDAO).buscarInvestimentosPorPeriodo(1, null, null);
	}
}