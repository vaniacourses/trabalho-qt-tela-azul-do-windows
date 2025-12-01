package br.uff.ic.grupo6.banco.sistema;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class SaqueServletSistemaTest {

    private static final String DEFAULT_URL = "http://localhost:8080/banco-atm";
    private static String BASE_URL;
    
    // --- ESTADO COMPARTILHADO (SINGLE USER) ---
    // Criamos o usuário uma única vez e reutilizamos em todos os testes
    private static String sharedCpf;
    private static String sharedSenha;
    private static boolean usuarioRegistrado = false;

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeAll
    static void setupClass() {
        WebDriverManager.chromedriver().setup();
        BASE_URL = System.getProperty("BASE_URL", System.getenv().getOrDefault("BASE_URL", DEFAULT_URL));
    }

    @BeforeEach
    void setup() {
        ChromeOptions options = new ChromeOptions();
        
        // --- BLINDAGEM CONTRA POPUPS DE SEGURANÇA E SENHA ---
        options.addArguments("--disable-features=PasswordLeakDetection");
        options.addArguments("--disable-save-password-bubble");
        options.addArguments("--password-store=basic");
        options.addArguments("--disable-notifications");
        options.addArguments("--no-default-browser-check");
        
        options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("profile.password_manager_leak_detection", false);
        prefs.put("safebrowsing.enabled", false);
        prefs.put("safebrowsing.disable_download_protection", true);
        
        options.setExperimentalOption("prefs", prefs);

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        driver.manage().window().setSize(new Dimension(1280, 900));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // --- REGISTRO ÚNICO DE USUÁRIO ---
        // Se ainda não registramos o usuário compartilhado, fazemos agora.
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
    @DisplayName("Fluxo Realista: Login -> Depósito -> Saque com Sucesso")
    void deveRealizarFluxoCompletoDepositoESaque() {
        // 1. Login com o usuário compartilhado
        logarUsuario(sharedCpf, sharedSenha);

        // 2. Realizar Depósito para ter saldo (R$ 100,00)
        realizarDeposito("100.00");

        // 3. Acessar Saque
        driver.get(BASE_URL + "/saque.jsp");
        
        // 4. Realizar Saque (R$ 10,00)
        WebElement campoValor = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("valor")));
        campoValor.clear();
        preencherCampo(By.id("valor"), "10.00");
        
        try {
            clicar(By.xpath("//button[contains(text(), 'Sacar')]"));
        } catch (Exception e) {
            clicar(By.cssSelector("button[type='submit']"));
        }

        // 5. Validar Sucesso
        try {
            boolean sucesso = wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("SaqueServlet"),
                ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), "Sucesso"),
                ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), "Comprovante")
            ));
            assertTrue(sucesso, "Deveria ter carregado a página de comprovante após o saque.");
        } catch (TimeoutException e) {
            if (driver.getCurrentUrl().contains("erro=")) {
                fail("O saque falhou com erro na URL: " + driver.getCurrentUrl());
            } else {
                fail("Timeout: Não foi possível identificar o comprovante. URL atual: " + driver.getCurrentUrl());
            }
        }
    }

    @Test
    @DisplayName("Validação de Erro: Valor não múltiplo de 10")
    void deveExibirErroSaqueNaoMultiploDeDez() {
        // Login e Depósito prévio (opcional, mas bom para garantir consistência)
        logarUsuario(sharedCpf, sharedSenha);
        realizarDeposito("50.00"); // Garante saldo

        driver.get(BASE_URL + "/saque.jsp");
        wait.until(ExpectedConditions.urlContains("saque.jsp"));

        preencherCampo(By.id("valor"), "15.00");
        clicar(By.cssSelector("button[type='submit']"));
        
        aguardarCarregamento();
        
        boolean urlSaque = driver.getCurrentUrl().contains("saque.jsp");
        boolean temErro = driver.getPageSource().contains("múltiplos") || driver.getCurrentUrl().contains("erro=");
        
        assertTrue(urlSaque && temErro, "Deve permanecer na página e exibir erro de múltiplos de 10.");
    }

    @Test
    @DisplayName("Validação de Erro: Valor acima do limite")
    void deveExibirErroSaqueAcimaDoLimite() {
        logarUsuario(sharedCpf, sharedSenha);
        realizarDeposito("50.00"); 

        driver.get(BASE_URL + "/saque.jsp");
        wait.until(ExpectedConditions.urlContains("saque.jsp"));

        preencherCampo(By.id("valor"), "2500.00");
        clicar(By.cssSelector("button[type='submit']"));

        aguardarCarregamento();

        boolean urlSaque = driver.getCurrentUrl().contains("saque.jsp");
        boolean temErro = driver.getPageSource().contains("limite") || driver.getCurrentUrl().contains("erro=");

        assertTrue(urlSaque && temErro, "Deve permanecer na página e exibir erro de limite.");
    }

    @Test
    @DisplayName("Segurança: Tentar acessar saque sem login")
    void deveRedirecionarParaLoginSeNaoEstiverLogado() {
        driver.get(BASE_URL + "/saque.jsp");
        aguardarCarregamento();
        assertTrue(driver.getCurrentUrl().contains("login.jsp"), "Deveria ter redirecionado para login.");
    }

    // --- MÉTODOS AUXILIARES ---

    private void realizarDeposito(String valor) {
        driver.get(BASE_URL + "/deposito.jsp");
        
        // Aguarda formulário de depósito
        WebElement campoValor = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("valor")));
        campoValor.clear();
        preencherCampo(By.id("valor"), valor);

        // Clica em "Confirmar Depósito"
        try {
            clicar(By.xpath("//button[contains(text(), 'Confirmar Depósito')]"));
        } catch (Exception e) {
            clicar(By.cssSelector("button[type='submit']"));
        }

        // Aguarda processamento (assume-se que redireciona para dashboard ou mostra sucesso)
        // Como não tenho a URL de sucesso do depósito, verifico se NÃO estamos mais na página com erro
        aguardarCarregamento();
        boolean semErro = !driver.getCurrentUrl().contains("erro=");
        assertTrue(semErro, "O depósito falhou.");
    }

    private void registrarCliente(String cpf, String senha) {
        driver.get(BASE_URL + "/cadastro.jsp");
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nome")));
        
        String nome = "Cliente Teste " + UUID.randomUUID().toString().substring(0, 5);
        String email = "teste" + UUID.randomUUID() + "@email.com";
        
        preencherCampo(By.id("nome"), nome);
        preencherCampo(By.id("cpf"), cpf);
        preencherCampo(By.id("dataNascimento"), LocalDate.of(1990, 1, 1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        preencherCampo(By.id("email"), email);
        preencherCampo(By.id("telefone"), "21999999999");
        preencherCampo(By.id("cep"), "24000000");
        preencherCampo(By.id("endereco"), "Rua Teste, 123");
        preencherCampo(By.id("bairro"), "Centro");
        selecionar(By.id("estado"), "RJ");
        preencherCampo(By.id("cidade"), "Rio de Janeiro");
        preencherCampo(By.id("renda"), "5000");
        selecionar(By.id("ocupacao"), "Outros");
        preencherCampo(By.id("senha"), senha);
        preencherCampo(By.id("confirmaSenha"), senha);

        clicar(By.cssSelector("button[type='submit']"));
        
        try {
            wait.until(ExpectedConditions.urlContains("login.jsp"));
        } catch (TimeoutException e) {
            throw new RuntimeException("Falha ao registrar cliente. Não redirecionou para login.jsp.", e);
        }
    }

    private void logarUsuario(String cpf, String senha) {
        driver.get(BASE_URL + "/login.jsp");
        
        WebElement campoLogin = wait.until(ExpectedConditions.elementToBeClickable(By.id("login")));
        campoLogin.clear();
        campoLogin.sendKeys(cpf);
        
        preencherCampo(By.id("senha"), senha);
        clicar(By.cssSelector("button[type='submit']"));
        
        try {
            wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("login.jsp")));
        } catch (TimeoutException e) {
            throw new RuntimeException("Login falhou ou demorou muito. URL permaneceu em login.jsp.", e);
        }
        
        aguardarCarregamento();
    }

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
        WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center', inline:'center'});", el);
        } catch (Exception ignored) { }

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
        } catch (InterruptedException ignored) { }
    }
    
    private String gerarCpfFake() {
        return UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 11);
    }
}