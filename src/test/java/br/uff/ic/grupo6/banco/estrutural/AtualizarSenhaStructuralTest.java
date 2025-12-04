package br.uff.ic.grupo6.banco.estrutural;

import br.uff.ic.grupo6.banco.dao.UsuarioDAO;
import br.uff.ic.grupo6.banco.service.ClienteService;
import br.uff.ic.grupo6.banco.service.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class AtualizarSenhaStructuralTest {

	private ClienteService clienteService;
	private UsuarioDAO usuarioDAOMock;

	@BeforeEach
	public void setUp() {
		usuarioDAOMock = Mockito.mock(UsuarioDAO.class);
		clienteService = new ClienteService(usuarioDAOMock);
	}

	@DisplayName("Teste 1: Deve lançar exceção se a senha atual informada estiver incorreta")
	@Test
	public void testAtualizarSenha_SenhaAtualIncorreta_DeveLancarExcecao() {
		int idCliente = 1;
		String senhaAtual = "errada";
		String senhaCorretaNaSessao = "correta123";
		String novaSenha = "NovaSenha123";
		String confirmarNovaSenha = "NovaSenha123";

		ValidationException exception = assertThrows(ValidationException.class, () -> {
			clienteService.atualizarSenha(idCliente, senhaAtual, novaSenha, confirmarNovaSenha, senhaCorretaNaSessao);
		});

		assertEquals("A senha atual esta incorreta.", exception.getMessage());
		verifyNoInteractions(usuarioDAOMock);
	}

	@DisplayName("Teste 2: Deve lançar exceção se a nova senha for igual à senha antiga")
	@Test
	public void testAtualizarSenha_NovaSenhaIgualAntiga_DeveLancarExcecao() {
		int idCliente = 1;
		String senhaCorretaNaSessao = "Senha123";
		String senhaAtual = "Senha123"; // Passa no 1º IF
		String novaSenha = "Senha123"; // Falha no 2º IF
		String confirmarNovaSenha = "Senha123";

		ValidationException exception = assertThrows(ValidationException.class, () -> {
			clienteService.atualizarSenha(idCliente, senhaAtual, novaSenha, confirmarNovaSenha, senhaCorretaNaSessao);
		});

		assertEquals("A nova senha não pode ser igual à senha antiga.", exception.getMessage());
		verifyNoInteractions(usuarioDAOMock);
	}

	@DisplayName("Teste 3: Deve lançar exceção se a confirmação de senha não corresponder à nova senha")
	@Test
	public void testAtualizarSenha_ConfirmacaoDiferente_DeveLancarExcecao() {
		int idCliente = 1;
		String senhaCorretaNaSessao = "SenhaAntiga1";
		String senhaAtual = "SenhaAntiga1";
		String novaSenha = "NovaSenha123";
		String confirmarNovaSenha = "OutraCoisa"; // Falha no 3º IF

		ValidationException exception = assertThrows(ValidationException.class, () -> {
			clienteService.atualizarSenha(idCliente, senhaAtual, novaSenha, confirmarNovaSenha, senhaCorretaNaSessao);
		});

		assertEquals("A nova senha e a confirmacao nao correspondem.", exception.getMessage());
		verifyNoInteractions(usuarioDAOMock);
	}

	@DisplayName("Teste 4: Deve lançar exceção se a nova senha for fraca (falha no Regex)")
	@Test
	public void testAtualizarSenha_SenhaFraca_DeveLancarExcecao() {
		int idCliente = 1;
		String senhaCorretaNaSessao = "SenhaAntiga1";
		String senhaAtual = "SenhaAntiga1";
		// Senha sem números ou muito curta, falha
		String novaSenha = "senhafraca";
		String confirmarNovaSenha = "senhafraca";

		ValidationException exception = assertThrows(ValidationException.class, () -> {
			clienteService.atualizarSenha(idCliente, senhaAtual, novaSenha, confirmarNovaSenha, senhaCorretaNaSessao);
		});

		assertEquals("A senha deve ter no mínimo 8 caracteres, com pelo menos uma letra e um número.",
				exception.getMessage());
		verifyNoInteractions(usuarioDAOMock);
	}

	@DisplayName("Teste 5: Caminho Feliz - Deve atualizar a senha com sucesso quando todas as validações passam")
	@Test
	public void testAtualizarSenha_TudoValido_DeveAtualizar() throws ValidationException, SQLException {
		int idCliente = 1;
		String senhaCorretaNaSessao = "SenhaAntiga1";
		String senhaAtual = "SenhaAntiga1";
		String novaSenha = "NovaSenha123"; // Atende ao regex: >8 chars, letras e números
		String confirmarNovaSenha = "NovaSenha123";

		clienteService.atualizarSenha(idCliente, senhaAtual, novaSenha, confirmarNovaSenha, senhaCorretaNaSessao);

		// Deve chamar o DAO
		verify(usuarioDAOMock, times(1)).atualizarSenha(idCliente, novaSenha);
	}
}