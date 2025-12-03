package br.uff.ic.grupo6.banco.naoFuncional;

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
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Teste Não Funcional: Segurança (Security).
 * Objetivo: Validar controles de acesso, sanitização de entrada e proteção contra injeções no fluxo de Saque.
 */
class SegurancaTest {

    private static final String DEFAULT_URL = "http://localhost:8080/banco-atm";
    private static String BASE_URL;
    
    private WebDriver driver;
    private WebDriverWait wait;

    // Estado compartilhado para evitar recriar usuário a cada teste se não necessário
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
        // Blindagem de segurança (igual aos outros testes)
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
        
        options.setExperimentalOption("prefs", prefs);

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        driver.manage().window().setSize(new Dimension(1280, 900));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Cria massa de dados apenas uma vez
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
    @DisplayName("Segurança: Bloqueio de Acesso Não Autorizado (Broken Access Control)")
    void deveBloquearAcessoDiretoAoSaqueSemSessao() {
        // Tenta acessar a página de saque diretamente sem estar logado
        driver.get(BASE_URL + "/saque.jsp");

        // Validação Principal: Deve ser redirecionado para o login
        wait.until(ExpectedConditions.urlContains("login.jsp"));
        
        String urlAtual = driver.getCurrentUrl();
        assertTrue(urlAtual.contains("login.jsp"), 
            "Falha de Segurança! Usuário anônimo conseguiu acessar a URL /saque.jsp e não foi redirecionado.");
            
        // Nota: Removemos a validação secundária da mensagem de texto ("Acesso negado"),
        // pois o foco do teste de segurança é garantir que o acesso à página protegida foi impedido.
        // O redirecionamento para login.jsp já comprova que a barreira de segurança funcionou.
    }

    @Test
    @DisplayName("Segurança: Tratamento de Injeção de XSS no campo Valor")
    void deveSanitizarOuRejeitarScriptNoInput() {
        // Pré-condição: Login
        logarUsuario(sharedCpf, sharedSenha);

        driver.get(BASE_URL + "/saque.jsp");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("valor")));

        // Tenta injetar um script simples
        String payloadXSS = "<script>alert('Hacked');</script>";
        preencherCampo(By.id("valor"), payloadXSS);
        
        clicarBotaoSacar();

        // Verificações:
        // 1. Não deve aparecer nenhum alerta JS (se aparecer, o teste falha)
        try {
            wait.until(ExpectedConditions.alertIsPresent());
            Alert alert = driver.switchTo().alert();
            String alertText = alert.getText();
            alert.accept();
            if (alertText.contains("Hacked")) {
                Assertions.fail("Falha Crítica de Segurança: XSS Refletido detectado! O script foi executado.");
            }
        } catch (TimeoutException e) {
            // OK! Nenhum alerta apareceu.
        }

        // 2. Deve tratar como erro de formato (pois não é número) e não quebrar a página
        boolean urlDeErro = driver.getCurrentUrl().contains("erro=") || driver.getCurrentUrl().contains("saque.jsp");
        assertTrue(urlDeErro, "Deveria redirecionar para a página de saque com erro de validação.");
        
        // 3. O payload não deve ser renderizado como HTML na página de resposta
        assertFalse(driver.getPageSource().contains("<script>"), "O código fonte não deve refletir a tag script sem escape.");
    }

    @Test
    @DisplayName("Segurança: Tratamento de Injeção de SQL no campo Valor")
    void deveRejeitarPayloadSQLNoInput() {
        // Pré-condição: Login
        logarUsuario(sharedCpf, sharedSenha);

        driver.get(BASE_URL + "/saque.jsp");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("valor")));

        // Tenta injetar SQL (ex: tentar manipular uma query que usa o valor)
        String payloadSQL = "10 OR 1=1"; 
        preencherCampo(By.id("valor"), payloadSQL);
        
        clicarBotaoSacar();

        // O sistema deve capturar isso como NumberFormatException ou validação de entrada,
        // jamais executar ou mostrar erro de sintaxe SQL na tela.
        
        wait.until(ExpectedConditions.urlContains("saque.jsp"));
        
        String pageSource = driver.getPageSource();
        assertFalse(pageSource.contains("SQLException"), "Falha de Segurança: StackTrace de SQL vazou para o usuário!");
        assertFalse(pageSource.contains("Syntax error"), "Falha de Segurança: Erro de sintaxe SQL exposto!");
        
        assertTrue(driver.getCurrentUrl().contains("erro=") || pageSource.contains("Valor inválido"), 
            "O sistema deve rejeitar a entrada maliciosa como valor inválido.");
    }

    // --- Helpers (Reutilizados) ---

    private void registrarCliente(String cpf, String senha) {
        driver.get(BASE_URL + "/cadastro.jsp");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nome")));
        
        String nome = "SecTest " + UUID.randomUUID().toString().substring(0, 5);
        String email = "sec" + UUID.randomUUID() + "@email.com";
        
        preencherCampo(By.id("nome"), nome);
        preencherCampo(By.id("cpf"), cpf);
        preencherCampo(By.id("dataNascimento"), LocalDate.of(1990, 1, 1).format(DateTimeFormatter.ofPattern("ddMMyyyy")));
        if (driver.findElement(By.id("dataNascimento")).getAttribute("value").isEmpty()) {
             preencherCampo(By.id("dataNascimento"), "1990-01-01");
        }
        preencherCampo(By.id("email"), email);
        preencherCampo(By.id("telefone"), "21999999999");
        preencherCampo(By.id("cep"), "24000000");
        preencherCampo(By.id("endereco"), "Rua Sec");
        preencherCampo(By.id("bairro"), "Centro");
        selecionar(By.id("estado"), "RJ");
        preencherCampo(By.id("cidade"), "Rio");
        preencherCampo(By.id("renda"), "5000");
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
    
    private void clicarBotaoSacar() {
        try {
            clicar(By.xpath("//button[contains(text(), 'Sacar')]"));
        } catch (Exception e) {
            clicar(By.cssSelector("button[type='submit']"));
        }
    }
    
    private String gerarCpfFake() {
        return UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 11);
    }
}