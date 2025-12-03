package br.uff.ic.grupo6.banco.sistema;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.TimeoutException; // Import necessário para tratar erros de tempo limite

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de Sistema (End-to-End) para a tela de Login.
 * O servidor deve estar rodando em http://localhost:8080/banco-atm/login.jsp.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LoginServiceSistemaTest {

    private WebDriver driver;
    private WebDriverWait wait;

    // Constantes de URL e Paths
    private static final String BASE_URL = "http://localhost:8080/banco-atm/login.jsp"; 
    private static final String SUCCESS_REDIRECT_PATH = "/dashboard.jsp"; 

    // Dados de Teste
    // CT-S-10: Este usuário deve existir no banco de dados.
    private static final String CPF_VALIDO_BD = "00000000000";
    private static final String SENHA_VALIDA_BD = "admin";
    // CT-S-12: Este usuário deve ser inexistente.
    private static final String CPF_INEXISTENTE = "12345678910"; 

    @BeforeAll
    static void setupClass() {
        // Configurações de classe (como configurar o caminho do ChromeDriver)
    }

    /**
     * Inicia o WebDriver (navegador Chrome) e a espera antes de cada teste.
     */
    @BeforeEach
    void setup() {
        driver = new ChromeDriver();
        // Tempo máximo de espera para elementos em 10 segundos
        wait = new WebDriverWait(driver, Duration.ofSeconds(10)); 
    }

    /**
     * Função para tentar fazer o login.
     */
    private void attemptLogin(String cpf, String senha) {
        driver.get(BASE_URL);

        // Espera a página carregar
        try {
            wait.until(ExpectedConditions.titleContains("Bank - Login"));
        } catch (Exception e) {
            tearDown(); 
            throw new RuntimeException("Falha ao carregar a página de login. Servidor fora do ar ou URL errada.", e);
        }
        
        // Preenche o campo CPF e Senha
        WebElement cpfField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login")));
        cpfField.sendKeys(cpf);

        WebElement senhaField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("senha")));
        senhaField.sendKeys(senha);

        // Clica no botão Entrar
        WebElement btnEntrar = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        btnEntrar.click();
    }

    /**
     * CT-S-10: Login com Sucesso (Caminho Feliz).
     */
    @Test
    @Order(10)
    @DisplayName("CT-S-10: Login com Sucesso (Caminho Feliz)")
    void cts10_loginSucesso() {
        // Tenta fazer login com credenciais válidas
        attemptLogin(CPF_VALIDO_BD, SENHA_VALIDA_BD);

        // Verifica o sucesso
        try {
            // 1. Tenta verificar se a URL mudou (esperado após um 'redirect')
            wait.until(ExpectedConditions.urlContains(SUCCESS_REDIRECT_PATH));
            
            // Confirma que o dashboard foi carregado
            assertTrue(driver.getCurrentUrl().contains(SUCCESS_REDIRECT_PATH), 
                       "O login não redirecionou para o dashboard. URL atual: " + driver.getCurrentUrl());
            
        } catch (TimeoutException e) {
            // 2. Se a URL não mudou (usando 'forward'), verifica se o título mudou.
            try {
                 wait.until(ExpectedConditions.titleContains("Dashboard"));
                 System.out.println("Sucesso, mas a URL não mudou (verifique o uso de forward no Servlet). Título: " + driver.getTitle());
                 
            } catch (Exception ex) {
                // Se falhar, é um erro real.
                throw new TimeoutException("Falha no login. Verifique a lógica de redirecionamento do LoginServlet.", e);
            }
        }
    }

    /**
     * CT-S-11: Falha de Login (Senha Incorreta).
     */
    @Test
    @Order(11)
    @DisplayName("CT-S-11: Falha de Login (Senha Incorreta)")
    void cts11_falhaSenhaIncorreta() {
        // Tenta fazer login com senha errada
        attemptLogin(CPF_VALIDO_BD, "senhaErrada");

        // CORREÇÃO: Remove a verificação de URL, pois o servidor faz 'forward' e a URL não muda de LoginServlet.
        // A presença do alerta de erro já confirma que permaneceu na tela de login após a falha.
        
        // Verifica a mensagem de erro no alerta
        WebElement alert = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(@class, 'alert-danger')]"))
        );
        
        String alertText = alert.getText();
        // A mensagem esperada contém "invalidos" (corrigido para o texto real)
        assertTrue(alertText.contains("invalidos"), 
                   "Erro: mensagem esperada 'invalidos', mas foi: " + alertText);
    }

    /**
     * CT-S-12: Falha de Login (Usuário Inexistente).
     */
    @Test
    @Order(12)
    @DisplayName("CT-S-12: Falha de Login (Usuário Inexistente)")
    void cts12_falhaUsuarioInexistente() {
        // Tenta fazer login com CPF inexistente
        attemptLogin(CPF_INEXISTENTE, "123");

        // CORREÇÃO: Remove a verificação de URL, pois o servidor faz 'forward' e a URL não muda de LoginServlet.
        // A presença do alerta de erro já confirma que permaneceu na tela de login após a falha.

        // Verifica a mensagem de erro (esperamos a mesma mensagem genérica)
        WebElement alert = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(@class, 'alert-danger')]"))
        );
        
        String alertText = alert.getText();
        // A mensagem esperada contém "invalidos" (corrigido para o texto real)
        assertTrue(alertText.contains("invalidos"), 
                   "Erro: mensagem esperada 'invalidos', mas foi: " + alertText);
    }

    /**
     * Fecha o navegador após cada teste.
     */
    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}