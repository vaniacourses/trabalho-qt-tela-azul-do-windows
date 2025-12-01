package br.uff.ic.grupo6.banco.sistema;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

/**
 * Teste de sistema (fim-a-fim) exercitando cadastro, login e atualização de senha.
 */
public class ClienteServiceSistemaTest {

    // URL padrão; pode ser sobrescrita via propriedade/variável BASE_URL
    private static final String DEFAULT_URL = "http://localhost:8080/banco-atm";
    private static String BASE_URL; // variável usada no teste

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeAll
    static void setupClass() {
        WebDriverManager.chromedriver().setup();
        BASE_URL = System.getProperty("BASE_URL", System.getenv().getOrDefault("BASE_URL", DEFAULT_URL));
    }

    @BeforeEach
    void setup() {
        driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        driver.manage().window().setSize(new Dimension(1280, 900));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @DisplayName("Cadastro, login e atualização de senha do cliente (ClienteService)")
    void cadastroLoginAtualizacaoSenha() {
        // dados únicos para evitar colisão
        String cpf = gerarCpfFake();
        String senhaInicial = "Senha123"; // atende regex: 8+ chars, letra e número
        String senhaNova = "NovaSenha123";
        String nome = "Cliente QA " + UUID.randomUUID();
        String email = "qa+" + UUID.randomUUID() + "@example.com";

        // cadastro
        driver.get(BASE_URL + "/cadastro.jsp");
        preencherCampo(By.id("nome"), nome);
        preencherCampo(By.id("cpf"), cpf);
        preencherCampo(By.id("dataNascimento"), LocalDate.of(1990, 1, 1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        preencherCampo(By.id("email"), email);
        preencherCampo(By.id("telefone"), "21999999999");
        preencherCampo(By.id("cep"), "20000000");
        preencherCampo(By.id("endereco"), "Rua Teste, 123");
        preencherCampo(By.id("bairro"), "Centro");
        selecionar(By.id("estado"), "RJ");
        preencherCampo(By.id("cidade"), "Rio de Janeiro");
        preencherCampo(By.id("renda"), "10000");
        selecionar(By.id("ocupacao"), "Engenheiro");
        preencherCampo(By.id("senha"), senhaInicial);
        preencherCampo(By.id("confirmaSenha"), senhaInicial);
        clicar(By.cssSelector("button[type='submit']"));

        // deve retornar ao login
        aguardarCarregamento();
        Assertions.assertTrue(driver.getCurrentUrl().contains("login.jsp"),
                "Após cadastro, deveria voltar ao login.jsp. URL atual: " + driver.getCurrentUrl());

        // login inicial
        preencherCampo(By.id("login"), cpf);
        preencherCampo(By.id("senha"), senhaInicial);
        clicar(By.cssSelector("button[type='submit']"));

        // deve ir para dashboard
        aguardarCarregamento();
        Assertions.assertTrue(driver.getCurrentUrl().contains("dashboard.jsp"),
                "Login do cliente deveria ir para dashboard.jsp. URL atual: " + driver.getCurrentUrl());

        // atualiza senha em Dados Cadastrais
        driver.get(BASE_URL + "/dadosCadastrais.jsp");
        aguardarCarregamento();
        clicar(By.id("btnEditar"));
        preencherCampo(By.id("senhaAtual"), senhaInicial);
        preencherCampo(By.id("novaSenha"), senhaNova);
        preencherCampo(By.id("confirmarNovaSenha"), senhaNova);
        clicar(By.id("btnSalvar"));
        aguardarCarregamento();

        // valida pelo novo login
        clicarLogout();

        // login com nova senha
        driver.get(BASE_URL + "/login.jsp");
        preencherCampo(By.id("login"), cpf);
        preencherCampo(By.id("senha"), senhaNova);
        clicar(By.cssSelector("button[type='submit']"));
        aguardarCarregamento();
        Assertions.assertTrue(driver.getCurrentUrl().contains("dashboard.jsp"),
                "Após atualizar senha (ClienteService), login com nova senha deve funcionar.");
    }

    // ----------------- Helpers -----------------

    private void preencherCampo(By locator, String valor) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        el.clear();
        el.sendKeys(valor);
    }

    private void selecionar(By selectLocator, String visibleText) {
        WebElement select = wait.until(ExpectedConditions.elementToBeClickable(selectLocator));
        select.sendKeys(visibleText);
    }

    private void clicar(By locator) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center', inline:'center'});", el);
        } catch (Exception ignored) {}
        wait.until(ExpectedConditions.elementToBeClickable(el));
        try {
            el.click();
        } catch (ElementClickInterceptedException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }
    }

    private void aguardarCarregamento() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
        }
    }

    private void clicarLogout() {
        try {
            clicar(By.linkText("Sair"));
        } catch (Exception e) {
            driver.get(BASE_URL + "/LogoutServlet");
        }
        aguardarCarregamento();
    }

    private String gerarCpfFake() {
        String base = UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 11);
        return base;
    }
}
