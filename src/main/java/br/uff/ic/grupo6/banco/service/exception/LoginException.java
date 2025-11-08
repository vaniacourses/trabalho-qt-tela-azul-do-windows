package br.uff.ic.grupo6.banco.service.exception;

/**
 * Exceção personalizada para erros de lógica de login (ex: senha incorreta,
 * usuário não encontrado).
 */
public class LoginException extends Exception {
	public LoginException(String message) {
		super(message);
	}
}