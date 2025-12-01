package br.uff.ic.grupo6.banco.sistema;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

class SaqueServletSistemaTest {

    private static final String DEFAULT_URL = "http://localhost:8080/banco-atm";
    private static String BASE_URL;
    private static final String ADMIN_CPF = "00000000000";
    private static final String ADMIN_SENHA = "admin";

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeAll
    static void setupClass() {
        WebDriverManager.chromedriver().setup();
        BASE_URL = System.getProperty("BASE_URL", System.getenv().getOrDefault("BASE_URL", DEFAULT_URL));
    }

    @BeforeEach
    void setup() {
        // Configurações avançadas para bloquear popups de segurança do Chrome
        ChromeOptions options = new ChromeOptions();
        
        // Argumentos de inicialização para desativar recursos visuais
        options.addArguments("--disable-features=PasswordLeakDetection");
        options.addArguments("--disable-save-password-bubble");
        
        // Preferências do Perfil (User Preferences) - Mais efetivo para configurações internas
        Map<String, Object> prefs = new HashMap<>();
        
        // Desabilita o prompt de "Salvar Senha"
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        
        // CRUCIAL: Desabilita especificamente a verificação de vazamento de credenciais nas preferências
        prefs.put("profile.password_manager_leak_detection", false);
        
        // Desabilita Safe Browsing para evitar alertas de site enganoso/perigoso em testes locais
        prefs.put("safebrowsing.enabled", false);

        options.setExperimentalOption("prefs", prefs);

        driver = new ChromeDriver(options);
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
    @DisplayName("Fluxo Completo: Login como Admin -> Realizar Saque com Sucesso")
    void deveRealizarSaqueCompletoComAdmin() {
        logarUsuario(ADMIN_CPF, ADMIN_SENHA);

        driver.get(BASE_URL + "/saque.jsp");
        aguardarCarregamento();

        preencherCampo(By.id("valor"), "10.00");
        
        try {
            clicar(By.xpath("//button[contains(text(), 'Sacar')]"));
        } catch (Exception e) {
            clicar(By.cssSelector("button[type='submit']"));
        }

        aguardarCarregamento();
        
        boolean urlCorreta = driver.getCurrentUrl().contains("comprovanteSaque.jsp");
        boolean textoSucesso = driver.getPageSource().contains("Sucesso") || driver.getPageSource().contains("Comprovante");

        Assertions.assertTrue(urlCorreta || textoSucesso);
    }

    @Test
    @DisplayName("Validação de Erro: Valor não múltiplo de 10")
    void deveExibirErroSaqueNaoMultiploDeDez() {
        logarUsuario(ADMIN_CPF, ADMIN_SENHA);

        driver.get(BASE_URL + "/saque.jsp");
        
        preencherCampo(By.id("valor"), "15.00");
        clicar(By.cssSelector("button[type='submit']"));
        
        aguardarCarregamento();
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("saque.jsp"));
        Assertions.assertTrue(driver.getPageSource().contains("múltiplos") || driver.getCurrentUrl().contains("erro="));
    }

    @Test
    @DisplayName("Validação de Erro: Valor acima do limite")
    void deveExibirErroSaqueAcimaDoLimite() {
        logarUsuario(ADMIN_CPF, ADMIN_SENHA);

        driver.get(BASE_URL + "/saque.jsp");

        preencherCampo(By.id("valor"), "2500.00");
        clicar(By.cssSelector("button[type='submit']"));

        aguardarCarregamento();

        Assertions.assertTrue(driver.getCurrentUrl().contains("saque.jsp"));
        Assertions.assertTrue(driver.getPageSource().contains("limite") || driver.getCurrentUrl().contains("erro="));
    }

    @Test
    @DisplayName("Segurança: Tentar acessar saque sem login")
    void deveRedirecionarParaLoginSeNaoEstiverLogado() {
        driver.get(BASE_URL + "/saque.jsp");
        
        aguardarCarregamento();

        Assertions.assertTrue(driver.getCurrentUrl().contains("login.jsp"));
    }

    private void logarUsuario(String cpf, String senha) {
        driver.get(BASE_URL + "/login.jsp");
        preencherCampo(By.id("login"), cpf);
        preencherCampo(By.id("senha"), senha);
        clicar(By.cssSelector("button[type='submit']"));
        aguardarCarregamento();
    }

    private void preencherCampo(By locator, String valor) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        el.clear();
        el.sendKeys(valor);
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
}