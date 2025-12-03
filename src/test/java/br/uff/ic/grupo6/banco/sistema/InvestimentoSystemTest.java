package br.uff.ic.grupo6.banco.sistema;

import br.uff.ic.grupo6.banco.dao.ConexaoDB;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes de Sistema Automatizados com Selenium WebDriver
 */
class InvestimentoSystemTest {

	private WebDriver driver;
	private WebDriverWait wait;
	private String baseUrl = "http://localhost:8080/banco-atm";

	// Credenciais de teste
	private static final String USUARIO_LOGIN = "11122233355"; // CPF
	private static final String USUARIO_SENHA = "111111aa"; // SENHA

	// TEMPO DE DELAY EM MILISSEGUNDOS (1000ms = 1 segundo)
	private static final long TEMPO_DELAY = 1000;

	@BeforeEach
	void setUp() throws SQLException {
		prepararBancoDeDados();
		driver = new ChromeDriver();
		wait = new WebDriverWait(driver, Duration.ofSeconds(10));
		driver.manage().window().maximize();
		driver.manage().deleteAllCookies();
	}

	@AfterEach
	void tearDown() {
		if (driver != null) {
			pausaVisual();
			driver.quit();
		}
	}

	// ---
	// TESTES
	// ---

	@Test
	@DisplayName("CT-S-01: Bloqueio de valor <= 0")
	void testValorInvalido() {
		realizarLoginEIrParaInvestimentos();
		clicarInvestimentoPorHref("CDB");

		WebElement campo = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("valor")));
		((JavascriptExecutor) driver).executeScript("arguments[0].removeAttribute('min');", campo);

		preencherValorEConfirmar("0");
		verificarMensagemOuUrlErro("Bloqueio de valor zero falhou");
	}

	@Test
	@DisplayName("CT-S-02: Bloqueio de saldo insuficiente")
	void testSaldoInsuficiente() {
		realizarLoginEIrParaInvestimentos();
		clicarInvestimentoPorHref("CDB");
		preencherValorEConfirmar("1500.00");

		WebElement msgErro = wait.until(
				ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(),'Saldo insuficiente')]")));

		pausaVisual();
		assertTrue(msgErro.isDisplayed(), "Erro: Deveria exibir mensagem 'Saldo insuficiente'.");
	}

	@Test
	@DisplayName("CT-S-03: Bloqueio FII < 1000")
	void testFIIMenorQue1000() {
		realizarLoginEIrParaInvestimentos();
		clicarInvestimentoPorHref("FII");
		preencherValorEConfirmar("900.00");
		verificarMensagemOuUrlErro("Deveria bloquear FII < 1000");
	}

	@Test
	@DisplayName("CT-S-04: Bloqueio CDB < 100")
	void testCDBMenorQue100() {
		realizarLoginEIrParaInvestimentos();
		clicarInvestimentoPorHref("CDB");
		preencherValorEConfirmar("50.00");
		verificarMensagemOuUrlErro("Deveria bloquear CDB < 100");
	}

	@Test
	@DisplayName("CT-S-05: Investimento SELIC válido")
	void testInvestimentoSELIC() {
		realizarLoginEIrParaInvestimentos();
		clicarInvestimentoPorHref("SELIC");
		preencherValorEConfirmar("100.00");
		verificarSucesso();
	}

	@Test
	@DisplayName("CT-S-06: Investimento POUPANCA válido")
	void testInvestimentoPOUPANCA() {
		realizarLoginEIrParaInvestimentos();
		clicarInvestimentoPorHref("POUPANCA");
		preencherValorEConfirmar("50.00");
		verificarSucesso();
	}

	@Test
	@DisplayName("CT-S-07: Investimento FII válido")
	void testInvestimentoFII() {
		realizarLoginEIrParaInvestimentos();
		clicarInvestimentoPorHref("FII");
		preencherValorEConfirmar("1000.00");
		verificarSucesso();
	}

	@Test
	@DisplayName("CT-S-08: Acesso a investimento.jsp sem login")
	void testAcessoSemLogin() {
		driver.get(baseUrl + "/investimento.jsp");

		WebElement btnEntrar = wait
				.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[contains(text(), 'Entrar')]")));

		pausaVisual();
		pausaVisual();

		assertTrue(btnEntrar.isDisplayed(), "Erro: Usuário deveria ser redirecionado para o login.");
		assertTrue(driver.getCurrentUrl().contains("login.jsp"), "URL deveria ser a de login.");
	}

	// ---
	// MÉTODOS AUXILIARES COM PAUSAS
	// ---

	private void realizarLoginEIrParaInvestimentos() {
		driver.get(baseUrl + "/login.jsp");
		pausaVisual();

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("login"))).sendKeys(USUARIO_LOGIN);
		driver.findElement(By.name("senha")).sendKeys(USUARIO_SENHA);

		pausaVisual();
		driver.findElement(By.xpath("//button[contains(text(), 'Entrar')]")).click();

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h3[contains(text(),'Bem-vindo')]")));

		pausaVisual();
		driver.get(baseUrl + "/investimento.jsp");

		wait.until(ExpectedConditions
				.visibilityOfElementLocated(By.xpath("//h3[contains(text(),'Opções de Investimento')]")));
		pausaVisual();
	}

	private void clicarInvestimentoPorHref(String tipo) {
		By seletor = By.cssSelector("a[href*='tipoInvestimento=" + tipo + "']");
		WebElement card = wait.until(ExpectedConditions.elementToBeClickable(seletor));

		pausaVisual();
		card.click();
	}

	private void preencherValorEConfirmar(String valor) {
		WebElement campoValor = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("valor")));
		campoValor.clear();
		campoValor.sendKeys(valor);

		pausaVisual();
		driver.findElement(By.xpath("//button[contains(text(), 'Confirmar Investimento')]")).click();
	}

	private void verificarSucesso() {
		WebElement msg = wait.until(ExpectedConditions
				.visibilityOfElementLocated(By.xpath("//*[contains(text(),'Investimento Realizado com Sucesso')]")));
		pausaVisual();
		assertTrue(msg.isDisplayed());
	}

	private void verificarMensagemOuUrlErro(String mensagemFalhaAssert) {
		pausaVisual();
		try {
			boolean temErroUrl = wait.until(ExpectedConditions.urlContains("erro="));
			assertTrue(temErroUrl, mensagemFalhaAssert);
		} catch (Exception e) {
			WebElement alerta = driver.findElement(By.cssSelector(".alert-danger"));
			assertTrue(alerta.isDisplayed(), mensagemFalhaAssert);
		}
	}

	private void pausaVisual() {
		try {
			Thread.sleep(TEMPO_DELAY);
		} catch (InterruptedException e) {
		}
	}

	// ---
	// BANCO DE DADOS
	// ---
	private void prepararBancoDeDados() throws SQLException {
		try (Connection conn = ConexaoDB.getConexao()) {
			String sqlId = "SELECT id FROM USUARIO WHERE cpf = ?";
			int idUsuario = 0;
			try (PreparedStatement ps = conn.prepareStatement(sqlId)) {
				ps.setString(1, USUARIO_LOGIN);
				ResultSet rs = ps.executeQuery();
				if (rs.next())
					idUsuario = rs.getInt("id");
			}

			if (idUsuario > 0) {
				String sqlConta = "SELECT id FROM CONTA WHERE id_usuario = ?";
				int idConta = 0;
				try (PreparedStatement ps = conn.prepareStatement(sqlConta)) {
					ps.setInt(1, idUsuario);
					ResultSet rs = ps.executeQuery();
					if (rs.next())
						idConta = rs.getInt("id");
				}

				if (idConta > 0) {
					try (PreparedStatement ps = conn.prepareStatement("DELETE FROM INVESTIMENTO WHERE id_conta = ?")) {
						ps.setInt(1, idConta);
						ps.executeUpdate();
					}
					try (PreparedStatement ps = conn
							.prepareStatement("UPDATE CONTA SET saldo = 1000.00 WHERE id = ?")) {
						ps.setInt(1, idConta);
						ps.executeUpdate();
					}
				}
			}
		}
	}
}