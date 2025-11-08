package br.uff.ic.grupo6.banco.service;

import br.uff.ic.grupo6.banco.dao.ContaDAO;
import br.uff.ic.grupo6.banco.dao.InvestimentoDAO;
import br.uff.ic.grupo6.banco.model.Investimento;
import br.uff.ic.grupo6.banco.service.exception.ValidationException;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Camada de Serviço para regras de negócio relacionadas a Investimentos.
 */
public class InvestimentoService {

	private final ContaDAO contaDAO;
	private final InvestimentoDAO investimentoDAO;

	public InvestimentoService() {
		this.contaDAO = new ContaDAO();
		this.investimentoDAO = new InvestimentoDAO();
	}

	// Construtor para injeção de dependência (facilita testes)
	public InvestimentoService(ContaDAO contaDAO, InvestimentoDAO investimentoDAO) {
		this.contaDAO = contaDAO;
		this.investimentoDAO = investimentoDAO;
	}

	/**
	 * Realiza um novo investimento.
	 * 
	 * @param idConta           ID da conta do cliente
	 * @param tipoInvestimento  Tipo do investimento (SELIC, CDB, etc.)
	 * @param valorInvestimento Valor a ser aplicado
	 * @param saldoAtual        Saldo atual da conta para validação
	 * @return O objeto Investimento criado
	 * @throws ValidationException Se a validação falhar
	 * @throws SQLException
	 */
	
	// ** COMPLEXIDADE CICLOMÁTICA = 10
	public Investimento realizarInvestimento(int idConta, String tipoInvestimento, double valorInvestimento,
			double saldoAtual) throws ValidationException, SQLException {

		// CC Original: 1 (base)

		if (tipoInvestimento == null || tipoInvestimento.trim().isEmpty() // CC +2 = 3
				|| !tipoInvestimento.matches("SELIC|CDB|FII|POUPANCA")) { // CC +1 = 4
			throw new ValidationException("Tipo de investimento invalido.");
		}

		if (valorInvestimento <= 0) { // CC +1 = 5
			throw new ValidationException("O valor do investimento deve ser positivo.");
		}

		if (saldoAtual < valorInvestimento) { // CC +1 = 6
			throw new ValidationException("Saldo insuficiente para realizar o investimento.");
		}

		// --- NOVAS REGRAS ---

		// NOVA REGRA (CC +2 = 8): FII (Fundos Imobiliários) exige um valor mínimo de R$
		// 1000
		if (tipoInvestimento.equals("FII") && valorInvestimento < 1000) {
			throw new ValidationException("O valor minimo para aplicar em FII e R$ 1.000,00.");
		}

		// NOVA REGRA (CC +2 = 10): CDB exige um valor mínimo de R$ 100
		if (tipoInvestimento.equals("CDB") && valorInvestimento < 100) {
			throw new ValidationException("O valor minimo para aplicar em CDB e R$ 100,00.");
		}

		// Se passou nas validações, chama o DAO
		return this.contaDAO.realizarInvestimento(idConta, tipoInvestimento, valorInvestimento);
	}

	/**
	 * Busca o historico de investimentos por periodo.
	 * 
	 * @param idConta    ID da conta
	 * @param dataInicio Data de inicio do filtro
	 * @param dataFim    Data de fim do filtro
	 * @return Lista de investimentos
	 * @throws ValidationException Se a data de inicio for posterior a data de fim
	 * @throws SQLException
	 */
	public List<Investimento> buscarInvestimentos(int idConta, LocalDate dataInicio, LocalDate dataFim)
			throws ValidationException, SQLException {

		if (dataInicio != null && dataFim != null && dataInicio.isAfter(dataFim)) {
			throw new ValidationException("A data de inicio deve ser anterior ou igual a data de fim.");
		}

		return investimentoDAO.buscarInvestimentosPorPeriodo(idConta, dataInicio, dataFim);
	}
}