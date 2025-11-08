package br.uff.ic.grupo6.banco.service;

import br.uff.ic.grupo6.banco.dao.ContaDAO;
import br.uff.ic.grupo6.banco.dao.TransacaoDAO;
import br.uff.ic.grupo6.banco.dao.UsuarioDAO;
import br.uff.ic.grupo6.banco.model.Cliente;
import br.uff.ic.grupo6.banco.model.Conta;
import br.uff.ic.grupo6.banco.model.Transacao;
import br.uff.ic.grupo6.banco.service.exception.ValidationException;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime; 
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Camada de Serviço para regras de negócio relacionadas a Transações (Saque,
 * Depósito, Transferência, Extrato).
 */
public class TransacaoService {

	private final ContaDAO contaDAO;
	private final TransacaoDAO transacaoDAO;
	private final UsuarioDAO usuarioDAO;

	public TransacaoService() {
		this.contaDAO = new ContaDAO();
		this.transacaoDAO = new TransacaoDAO();
		this.usuarioDAO = new UsuarioDAO();
	}

	// Construtor para injeção de dependência (facilita testes)
	public TransacaoService(ContaDAO contaDAO, TransacaoDAO transacaoDAO, UsuarioDAO usuarioDAO) {
		this.contaDAO = contaDAO;
		this.transacaoDAO = transacaoDAO;
		this.usuarioDAO = usuarioDAO;
	}

	/**
	 * Realiza um depósito na conta.
	 * 
	 * @param idConta ID da conta
	 * @param valor   Valor a depositar
	 * @return A transação de depósito gerada
	 * @throws ValidationException Se o valor for inválido
	 * @throws SQLException
	 */
	public Transacao realizarDeposito(int idConta, double valor) throws ValidationException, SQLException {
		if (valor <= 0) {
			throw new ValidationException("O valor do deposito deve ser positivo.");
		}
		return contaDAO.realizarDeposito(idConta, valor);
	}

	/**
	 * Busca o extrato (lista de transações) da conta, filtrando por período ou
	 * buscando tudo.
	 * 
	 * @param idConta    ID da conta
	 * @param dataInicio Data de início do filtro (opcional)
	 * @param dataFim    Data de fim do filtro (opcional)
	 * @return Lista de Transações
	 * @throws SQLException
	 */
	public List<Transacao> buscarExtrato(int idConta, LocalDate dataInicio, LocalDate dataFim) throws SQLException {
		if (dataInicio != null || dataFim != null) {
			// Se ao menos uma data foi fornecida, busca por período
			return transacaoDAO.buscarTransacoesPorPeriodo(idConta, dataInicio, dataFim);
		} else {
			// Se nenhuma data foi fornecida, busca todas as transações
			return transacaoDAO.buscarTodasTransacoesPorConta(idConta);
		}
	}

	/**
	 * Efetiva a transferência (chamado pela FinalizarTransferenciaServlet).
	 * 
	 * @param idContaOrigem  ID da conta de origem
	 * @param idContaDestino ID da conta de destino
	 * @param valor          Valor a transferir
	 * @return Lista com as duas transações (débito e crédito)
	 * @throws SQLException
	 */
	public List<Transacao> realizarTransferencia(int idContaOrigem, int idContaDestino, double valor)
			throws SQLException {
		return contaDAO.realizarTransferencia(idContaOrigem, idContaDestino, valor);
	}

	/**
	 * Realiza um saque da conta.
	 * 
	 * @param conta      Objeto Conta do cliente
	 * @param valorSaque Valor a sacar
	 * @return A transação de saque gerada
	 * @throws ValidationException Se qualquer regra de saque for violada
	 * @throws SQLException
	 */
	
	// ** COMPLEXIDADE CICLOMÁTICA = 10
	public Transacao realizarSaque(Conta conta, double valorSaque) throws ValidationException, SQLException {
		double saldoAtual = conta.getSaldo();

		// Validação 1: Valor positivo
		if (valorSaque <= 0) {
			throw new ValidationException("O valor do saque deve ser positivo.");
		}
		// Validação 2: Limite máximo por transação
		if (valorSaque > 2000) {
			throw new ValidationException("O valor do saque excede o limite de R$ 2.000,00 por transacao.");
		}
		// Validação 3: Múltiplo de R$ 10,00
		if (valorSaque % 10 != 0) {
			throw new ValidationException("O valor do saque deve ser em multiplos de R$ 10,00.");
		}
		// Validação 4: Valor mínimo de saque
		if (valorSaque < 10.00) {
			throw new ValidationException("O valor minimo para saque e R$ 10,00.");
		}
		// Validação 5: Bloqueio de Contas Salário (ex: iniciadas com '9')
		if (conta.getNumero().startsWith("9")) {
			throw new ValidationException("Contas Salario nao permitem saque. Use a transferencia.");
		}

		// Validação 6: Limite de horário para valores altos
		int hora = LocalTime.now().getHour();
		if (valorSaque > 1000 && (hora < 6 || hora >= 22)) {
			throw new ValidationException("Saques acima de R$ 1.000,00 so podem ser feitos entre 06:00 e 22:00.");
		}

		// Validação 7: Saldo insuficiente
		if (saldoAtual < valorSaque) {
			throw new ValidationException("Saldo insuficiente para realizar o saque.");
		}

		return contaDAO.realizarSaque(conta.getId(), valorSaque);
	}

	/**
	 * Prepara a transferência, validando os dados e buscando as informações do
	 * destinatário para a página de confirmação.
	 * 
	 * @param clienteOrigem  Cliente que está fazendo a transferência
	 * @param agenciaDestino Agência de destino
	 * @param contaDestino   Número da conta de destino
	 * @param valor          Valor a transferir
	 * @return Um Map contendo (clienteOrigem, clienteDestino, contaDeDestino,
	 *         valor)
	 * @throws ValidationException Se qualquer validação falhar
	 * @throws SQLException
	 */
	
	// ** COMPLEXIDADE CICLOMÁTICA = 13
	public Map<String, Object> prepararTransferencia(Cliente clienteOrigem, String agenciaDestino, String contaDestino,
			double valor) throws ValidationException, SQLException {

		Conta contaOrigem = clienteOrigem.getConta();
		Conta contaDeDestino = contaDAO.buscarContaPorAgenciaENumeroDaConta(agenciaDestino, contaDestino);

		// --- VALIDAÇÕES DE NEGÓCIO ---

		// Validação 1: Valor positivo
		if (valor <= 0) {
			throw new ValidationException("Valor deve ser positivo");
		}
		// Validação 2: Saldo suficiente
		if (valor > contaOrigem.getSaldo()) {
			throw new ValidationException("Saldo insuficiente");
		}
		// Validação 3: Conta de destino existe
		if (contaDeDestino == null) {
			throw new ValidationException("Conta de destino nao encontrada");
		}
		// Validação 4: Transferência para mesma conta
		if (contaDeDestino.getId() == contaOrigem.getId()) {
			throw new ValidationException("Conta de destino nao pode ser a mesma de origem");
		}
		// Validação 5: Limite máximo por transferência
		if (valor > 5000.00) {
			throw new ValidationException("O limite maximo por transferencia e de R$ 5.000,00.");
		}
		// Validação 6: Bloqueio de agências administrativas
		if (agenciaDestino.equals("9999") || agenciaDestino.equals("0000")) {
			throw new ValidationException("Nao e possível transferir para esta agencia.");
		}

		// Validação 7: Limite para clientes de baixa renda
		if (clienteOrigem.getRenda() != null && clienteOrigem.getRenda() < 2000 && valor > 1000) {
			throw new ValidationException(
					"Clientes com renda inferior a R$ 2.000,00 tem limite de R$ 1.000,00 por transferencia.");
		}

		// Validação 8: Bloqueio de Contas Salário (ex: iniciadas com '9')
		if (contaDeDestino.getNumero().startsWith("9")) {
			throw new ValidationException("Nao e permitido transferir para Contas Salario (iniciadas com 9).");
		}

		// Validação 9: Cliente de destino existe
		Cliente clienteDestino = usuarioDAO.buscarClientePorIdConta(contaDeDestino.getId());
		if (clienteDestino == null) {
			throw new ValidationException("Cliente de destino nao encontrado");
		}

		// --- Fim das Validações ---

		// Prepara os dados para a página de confirmação
		Map<String, Object> dadosConfirmacao = new HashMap<>();
		dadosConfirmacao.put("clienteOrigem", clienteOrigem);
		dadosConfirmacao.put("clienteDestino", clienteDestino);
		dadosConfirmacao.put("contaDeDestino", contaDeDestino);
		dadosConfirmacao.put("valor", valor);

		return dadosConfirmacao;
	}
}