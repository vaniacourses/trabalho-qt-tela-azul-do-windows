package br.uff.ic.grupo6.banco.service.exception;

/**
 * Exceção personalizada para erros de validação de regras de negócio (ex: saldo
 * insuficiente, valor inválido).
 */
public class ValidationException extends Exception {
	public ValidationException(String message) {
		super(message);
	}
}