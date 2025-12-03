package br.uff.ic.grupo6.banco.naoFuncional;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Teste Não Funcional: Eficiência de Desempenho (Time Behaviour).
 * Objetivo: Garantir que a transação de saque responda dentro de um limite de tempo aceitável (SLA).
 */
class DesempenhoTest {

    private static final String DEFAULT_URL = "http://localhost:8080/banco-atm";
    private static String BASE_URL;
    
    // Limite aceitável para o processamento do saque (em milissegundos)
    // Ex: 2000ms = 2 segundos. Se demorar mais que isso, o teste falha.
    private static final long MAX_TIME_MS = 2000;

    private WebDriver driver;
    private WebDriverWait wait;

    // Estado compartilhado
    private static String sharedCpf;
    private static String sharedSenha;
    private static boolean usuarioRegistrado = false;

    @BeforeAll
    static void setupClass() {
        WebDriverManager.chromedriver().setup();
        BASE_URL = System.getProperty("BASE_URL", System.getenv().getOrDefault("BASE_URL", DEFAULT_URL));
    }

    @BeforeEach
    void setup() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-features=PasswordLeakDetection");
        options.addArguments("--disable-save-password-bubble");
        options.addArguments("--password-store=basic");
        options.addArguments("--disable-notifications");
        options.addArguments("--no-default-browser-check");
        // Rodar em modo headless (sem interface) pode deixar o teste de performance mais preciso/leve
        // options.addArguments("--headless"); 
        
        options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("profile.password_manager_leak_detection", false);
        prefs.put("safebrowsing.enabled", false);
        
        options.setExperimentalOption("prefs", prefs);

        driver = new ChromeDriver(options);
        // Timeout global
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Preparação de massa de dados (Executa uma vez)
        if (!usuarioRegistrado) {
            sharedCpf = gerarCpfFake();
            sharedSenha = "Senha123";
            registrarCliente(sharedCpf, sharedSenha);
            usuarioRegistrado = true;
        }
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @DisplayName("Performance: Tempo de resposta do Saque deve ser < 2 segundos")
    void validaPerformanceDoSaque() {
        // 1. Pré-condição: Estar logado e com saldo
        logarUsuario(sharedCpf, sharedSenha);
        realizarDeposito("500.00"); // Garante saldo para não falhar na regra de negócio

        // 2. Preparação
        driver.get(BASE_URL + "/saque.jsp");
        WebElement campoValor = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("valor")));
        campoValor.clear();
        preencherCampo(By.id("valor"), "10.00");
        
        // Localiza o botão antes de iniciar o cronômetro
        WebElement btnSacar;
        try {
            btnSacar = driver.findElement(By.xpath("//button[contains(text(), 'Sacar')]"));
        } catch (Exception e) {
            btnSacar = driver.findElement(By.cssSelector("button[type='submit']"));
        }

        // 3. EXECUÇÃO MEDIDA
        long startTime = System.currentTimeMillis();
        
        // Ação: Clicar no botão
        try {
            btnSacar.click();
        } catch (ElementClickInterceptedException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btnSacar);
        }

        // Espera a resposta do servidor (Página de sucesso aparecer)
        try {
            wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("SaqueServlet"),
                ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), "Sucesso"),
                ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), "Comprovante")
            ));
        } catch (TimeoutException e) {
            fail("Timeout: O sistema não respondeu a tempo ou ocorreu erro.");
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // 4. Validação (Assert)
        System.out.println("Tempo de Resposta do Saque: " + duration + "ms");
        
        assertTrue(duration <= MAX_TIME_MS, 
            "Performance Falhou! O saque demorou " + duration + "ms, o limite é " + MAX_TIME_MS + "ms.");
    }

    // --- Helpers (Reutilizados para estabilidade) ---

    private void registrarCliente(String cpf, String senha) {
        driver.get(BASE_URL + "/cadastro.jsp");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nome")));
        
        String nome = "PerfTest " + UUID.randomUUID().toString().substring(0, 5);
        String email = "perf" + UUID.randomUUID() + "@email.com";
        
        preencherCampo(By.id("nome"), nome);
        preencherCampo(By.id("cpf"), cpf);
        preencherCampo(By.id("dataNascimento"), "1990-01-01"); // Formato simplificado se o browser aceitar
        if(driver.getPageSource().contains("dd/mm/aaaa")) { // Fallback se precisar formatar
             preencherCampo(By.id("dataNascimento"), "01011990");
        }
        preencherCampo(By.id("email"), email);
        preencherCampo(By.id("telefone"), "21999999999");
        preencherCampo(By.id("cep"), "24000000");
        preencherCampo(By.id("endereco"), "Rua Perf");
        preencherCampo(By.id("bairro"), "Centro");
        selecionar(By.id("estado"), "RJ");
        preencherCampo(By.id("cidade"), "Rio");
        preencherCampo(By.id("renda"), "10000");
        selecionar(By.id("ocupacao"), "Outros");
        preencherCampo(By.id("senha"), senha);
        preencherCampo(By.id("confirmaSenha"), senha);

        clicar(By.cssSelector("button[type='submit']"));
        try { wait.until(ExpectedConditions.urlContains("login.jsp")); } catch (Exception e) {}
    }

    private void logarUsuario(String cpf, String senha) {
        driver.get(BASE_URL + "/login.jsp");
        WebElement campo = wait.until(ExpectedConditions.elementToBeClickable(By.id("login")));
        campo.clear();
        campo.sendKeys(cpf);
        preencherCampo(By.id("senha"), senha);
        clicar(By.cssSelector("button[type='submit']"));
        try { wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("login.jsp"))); } catch (Exception e) {}
    }

    private void realizarDeposito(String valor) {
        driver.get(BASE_URL + "/deposito.jsp");
        WebElement campo = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("valor")));
        campo.clear();
        campo.sendKeys(valor);
        try {
            clicar(By.xpath("//button[contains(text(), 'Confirmar Depósito')]"));
        } catch (Exception e) {
            clicar(By.cssSelector("button[type='submit']"));
        }
        try { Thread.sleep(500); } catch (Exception e) {} 
    }

    private void preencherCampo(By locator, String valor) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        el.clear();
        el.sendKeys(valor);
    }
    
    private void selecionar(By locator, String text) {
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
        el.sendKeys(text);
    }

    private void clicar(By locator) {
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
        try { el.click(); } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }
    }
    
    private String gerarCpfFake() {
        return UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 11);
    }
}