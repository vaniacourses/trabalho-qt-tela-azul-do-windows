package br.uff.ic.grupo6.banco.service;

import br.uff.ic.grupo6.banco.dao.UsuarioDAO;
import br.uff.ic.grupo6.banco.model.Cliente;
import br.uff.ic.grupo6.banco.model.Gerente; 
import br.uff.ic.grupo6.banco.model.Usuario;
import br.uff.ic.grupo6.banco.service.exception.LoginException;

import java.sql.SQLException;
import java.util.UUID;

/**
 * Camada de Serviço para regras de negócio relacionadas a Autenticação e Login.
 */
public class LoginService {

	private final UsuarioDAO usuarioDAO;

	public LoginService() {
		this.usuarioDAO = new UsuarioDAO();
	}

	// Construtor para injeção de dependência (facilita testes)
	public LoginService(UsuarioDAO usuarioDAO) {
		this.usuarioDAO = usuarioDAO;
	}

	/**
	 * Autentica um usuário com base no CPF e senha.
	 * 
	 * @param cpf   Login (CPF)
	 * @param senha Senha
	 * @return O objeto Usuario (Cliente ou Gerente)
	 * @throws LoginException Se o CPF ou senha estiverem inválidos
	 * @throws SQLException
	 */
	
	// ** COMPLEXIDADE CICLOMÁTICA = 10
	public Usuario autenticar(String cpf, String senha) throws LoginException, SQLException {

		// Validação 1: CPF nulo or vazio
		if (cpf == null || cpf.trim().isEmpty()) {
			throw new LoginException("O campo CPF e obrigatorio.");
		}
		// Validação 2: Senha nula ou vazia
		if (senha == null || senha.isEmpty()) {
			throw new LoginException("O campo Senha e obrigatorio.");
		}
		// Validação 3: Formato do CPF (simplificado)
		if (!cpf.matches("\\d{11}")) {
			throw new LoginException("CPF ou senha invalidos.");
		}

		Usuario usuario = usuarioDAO.buscarPorCpf(cpf);

		// Validação 4: Usuário existe
		if (usuario == null) {
			throw new LoginException("CPF ou senha invalidos.");
		}

		// Validação 5: Senha correta
		if (!usuario.getSenha().equals(senha)) {
			throw new LoginException("CPF ou senha invalidos.");
		}

		// Validação 6: Simulação de regra de conta bloqueada
		String nomeUsuario = "";
		if (usuario instanceof Cliente) {
			nomeUsuario = ((Cliente) usuario).getNome();
		} else if (usuario instanceof Gerente) {
			nomeUsuario = ((Gerente) usuario).getNome();
		}

		if (nomeUsuario != null && nomeUsuario.equalsIgnoreCase("Usuario Bloqueado")) { // check de nulo
			throw new LoginException("Esta conta esta bloqueada. Contate o gerente.");
		}

		// Validação 7: Simulação de regra de senha temporária
		if (usuario.getSenha().equals("temp123")) {
			throw new LoginException("Senha expirada. Redefina sua senha.");
		}

		return usuario; // Sucesso
	}

	/**
	 * Gera e salva um novo token "Lembrar-me".
	 * 
	 * @param idUsuario ID do usuário
	 * @return O token gerado
	 * @throws SQLException
	 */
	public String gerarTokenLembrarMe(int idUsuario) throws SQLException {
		String token = UUID.randomUUID().toString();
		usuarioDAO.salvarTokenLembrarMe(idUsuario, token);
		return token;
	}

	/**
	 * Limpa o token "Lembrar-me" do banco de dados.
	 * 
	 * @param idUsuario ID do usuário
	 * @throws SQLException
	 */
	public void limparTokenLembrarMe(int idUsuario) throws SQLException {
		usuarioDAO.salvarTokenLembrarMe(idUsuario, null);
	}

	/**
	 * Busca um usuário pelo token "Lembrar-me".
	 * 
	 * @param token O token
	 * @return O objeto Usuario
	 * @throws SQLException
	 */
	public Usuario buscarUsuarioPorToken(String token) throws SQLException {
		if (token != null && !token.isEmpty()) {
			return usuarioDAO.buscarUsuarioPorToken(token);
		}
		return null;
	}
}