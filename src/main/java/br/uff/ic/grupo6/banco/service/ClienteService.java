package br.uff.ic.grupo6.banco.service;

import br.uff.ic.grupo6.banco.dao.UsuarioDAO;
import br.uff.ic.grupo6.banco.model.Cliente;
import br.uff.ic.grupo6.banco.service.exception.ValidationException;

import java.sql.SQLException;
import java.time.LocalDate; 
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern; 

/**
 * Camada de Serviço para regras de negócio relacionadas a Clientes.
 */
public class ClienteService {

	// Padrão de regex para senha: Pelo menos 8 chars, 1 letra, 1 numero
	private static final Pattern SENHA_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$");

	private final UsuarioDAO usuarioDAO;

	public ClienteService() {
		this.usuarioDAO = new UsuarioDAO();
	}

	// Construtor para injeção de dependência (facilita testes)
	public ClienteService(UsuarioDAO usuarioDAO) {
		this.usuarioDAO = usuarioDAO;
	}

	/**
	 * Busca clientes com base em um termo ou lista todos.
	 * 
	 * @param termoBusca Termo para filtrar (nome ou CPF)
	 * @param acao       Ação (ex: "listarTodos")
	 * @return Lista de clientes encontrados
	 * @throws SQLException
	 */
	public List<Cliente> buscarClientes(String termoBusca, String acao) throws SQLException {
		if ("listarTodos".equals(acao)) {
			return usuarioDAO.buscarTodosClientesOrdenados();
		} else if (termoBusca != null && !termoBusca.trim().isEmpty()) {
			return usuarioDAO.buscarClientesPorTermo(termoBusca);
		}
		return new ArrayList<>(); // Retorna lista vazia se nenhum critério for atendido
	}

	/**
	 * Busca um cliente específico pelo ID.
	 * 
	 * @param idCliente ID do cliente
	 * @return Objeto Cliente
	 * @throws SQLException
	 */
	public Cliente buscarClienteParaEdicao(int idCliente) throws SQLException {
		return usuarioDAO.buscarClientePorId(idCliente);
	}

	/**
	 * Atualiza os dados cadastrais de um cliente.
	 * 
	 * @param cliente Objeto cliente com dados atualizados
	 * @throws SQLException
	 */
	public void atualizarCliente(Cliente cliente) throws SQLException {
		usuarioDAO.atualizarCliente(cliente);
	}

	/**
	 * Exclui um cliente pelo ID.
	 * 
	 * @param idCliente ID do cliente
	 * @throws SQLException
	 */
	public void excluirCliente(int idCliente) throws SQLException {
		usuarioDAO.excluirClientePorId(idCliente);
	}

	/**
	 * Lista todos os clientes.
	 * 
	 * @return Lista de todos os clientes
	 * @throws SQLException
	 */
	public List<Cliente> listarTodosClientes() throws SQLException {
		return usuarioDAO.buscarTodosClientes();
	}

	/**
	 * Atualiza a senha do cliente após validar a senha atual.
	 * 
	 * @param idCliente            ID do cliente
	 * @param senhaAtual           Senha digitada pelo usuário
	 * @param novaSenha            Nova senha desejada
	 * @param confirmarNovaSenha   Confirmação da nova senha
	 * @param senhaCorretaNaSessao A senha real do usuário (armazenada na sessão)
	 * @throws ValidationException Se a validação falhar
	 * @throws SQLException
	 */
	public void atualizarSenha(int idCliente, String senhaAtual, String novaSenha, String confirmarNovaSenha,
			String senhaCorretaNaSessao) throws ValidationException, SQLException {

		if (!senhaAtual.equals(senhaCorretaNaSessao)) {
			throw new ValidationException("A senha atual esta incorreta.");
		}

		// NOVA REGRA (CC +1): Não pode redefinir para a mesma senha
		if (novaSenha.equals(senhaCorretaNaSessao)) {
			throw new ValidationException("A nova senha não pode ser igual à senha antiga.");
		}

		if (!novaSenha.equals(confirmarNovaSenha)) {
			throw new ValidationException("A nova senha e a confirmacao nao correspondem.");
		}

		// NOVA REGRA (CC +1): Validação de força da senha
		if (!SENHA_PATTERN.matcher(novaSenha).matches()) {
			throw new ValidationException(
					"A senha deve ter no mínimo 8 caracteres, com pelo menos uma letra e um número.");
		}

		// Se passou nas validações, atualiza no banco
		usuarioDAO.atualizarSenha(idCliente, novaSenha);
	}

	/**
	 * Cadastra um novo cliente após validações.
	 * 
	 * @param novoCliente   Objeto cliente preenchido com dados do formulário
	 * @param confirmaSenha Confirmação da senha
	 * @throws ValidationException Se a validação falhar
	 * @throws SQLException
	 */
	
	// ** COMPLEXIDADE CICLOMÁTICA = 10
	public void cadastrarNovoCliente(Cliente novoCliente, String confirmaSenha)
			throws ValidationException, SQLException {

		// CC Original: 1 (base)

		if (!novoCliente.getSenha().equals(confirmaSenha)) { // CC +1 = 2
			throw new ValidationException("Erro: As senhas não conferem!");
		}

		if (usuarioDAO.buscarPorCpf(novoCliente.getCpf()) != null) { // CC +1 = 3
			throw new ValidationException("Erro: Este CPF já está cadastrado!");
		}

		// --- NOVAS REGRAS ---

		// NOVA REGRA (CC +1 = 4): Validar formato do CPF (apenas números e 11 dígitos)
		if (novoCliente.getCpf() == null || !novoCliente.getCpf().matches("\\d{11}")) {
			throw new ValidationException("Erro: CPF inválido. Deve conter 11 números.");
		}

		// NOVA REGRA (CC +2 = 6): Validar email
		if (novoCliente.getEmail() == null || !novoCliente.getEmail().contains("@")) {
			throw new ValidationException("Erro: Formato de e-mail inválido.");
		}

		// NOVA REGRA (CC +1 = 7): Validar idade (maior que 18 anos)
		if (novoCliente.getDataNascimento().isAfter(LocalDate.now().minusYears(18))) {
			throw new ValidationException("Erro: O cliente deve ser maior de 18 anos.");
		}

		// NOVA REGRA (CC +1 = 8): Validar renda (não pode ser negativa)
		if (novoCliente.getRenda() < 0) {
			throw new ValidationException("Erro: A renda não pode ser um valor negativo.");
		}

		// NOVA REGRA (CC +2 = 10): Validar força da senha (pelo menos 8 chars, 1 letra,
		// 1 numero)
		if (novoCliente.getSenha() == null || !SENHA_PATTERN.matcher(novoCliente.getSenha()).matches()) {
			throw new ValidationException(
					"Erro: A senha deve ter no mínimo 8 caracteres, com pelo menos uma letra e um número.");
		}

		// Se passou nas validações, cadastra
		usuarioDAO.cadastrarCliente(novoCliente);
	}

	/**
	 * Busca dados atualizados de um cliente pelo CPF.
	 * 
	 * @param cpf CPF do cliente
	 * @return Objeto Cliente
	 * @throws SQLException
	 */
	public Cliente buscarClientePorCpf(String cpf) throws SQLException {
		return (Cliente) usuarioDAO.buscarPorCpf(cpf);
	}
}