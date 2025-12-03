package br.uff.ic.grupo6.banco.mutacao;

import br.uff.ic.grupo6.banco.dao.ContaDAO;
import br.uff.ic.grupo6.banco.dao.TransacaoDAO;
import br.uff.ic.grupo6.banco.dao.UsuarioDAO;
import br.uff.ic.grupo6.banco.model.Conta;
import br.uff.ic.grupo6.banco.model.Transacao;
import br.uff.ic.grupo6.banco.service.TransacaoService;
import br.uff.ic.grupo6.banco.service.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Testes de Mutação para o método realizarSaque.
 * Objetivo: Matar mutantes inseridos em operadores lógicos, relacionais e chamadas de método.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SaqueServiceMutacaoTest {

    @Mock
    private ContaDAO contaDAO;
    @Mock
    private TransacaoDAO transacaoDAO;
    @Mock
    private UsuarioDAO usuarioDAO;

    @InjectMocks
    private TransacaoService transacaoService;

    private Conta conta;

    @BeforeEach
    void setUp() {
        conta = mock(Conta.class);
        
        // Uso de lenient() permite configurar stubs que podem não ser usados em todos os testes
        lenient().when(conta.getId()).thenReturn(1);
        lenient().when(conta.getSaldo()).thenReturn(5000.0);
        lenient().when(conta.getNumero()).thenReturn("12345");
    }

    // --- LINHA 106: Validação Valor Positivo ---
    
    @Test
    void deveFalharQuandoValorIgualZero() {
        // Mata mutante: if (valor <= 0) -> if (valor < 0)
        assertThrows(ValidationException.class, 
            () -> transacaoService.realizarSaque(conta, 0.0));
    }

    @Test
    void deveFalharQuandoValorNegativo() {
        // Mata mutante: if (valor <= 0) -> if (valor == 0)
        // Se a condição mudar para apenas igual a zero, negativos passariam. Este teste impede isso.
        assertThrows(ValidationException.class, 
            () -> transacaoService.realizarSaque(conta, -10.0));
    }

    // --- LINHA 110: Limite Máximo 2000 ---

    @Test
    void devePermitirSaqueExatoDe2000() throws Exception {
        // Mata mutante: if (valor > 2000) -> if (valor >= 2000)
        LocalTime horarioComercial = LocalTime.of(14, 0);

        try (MockedStatic<LocalTime> mockedTime = Mockito.mockStatic(LocalTime.class, Mockito.CALLS_REAL_METHODS)) {
            mockedTime.when(LocalTime::now).thenReturn(horarioComercial);
            when(contaDAO.realizarSaque(anyInt(), anyDouble())).thenReturn(new Transacao());

            assertDoesNotThrow(() -> transacaoService.realizarSaque(conta, 2000.0));
        }
    }

    @Test
    void deveFalharSaqueAcimaDe2000() {
        // Mata mutante: if (valor > 2000) -> if (valor > 2010) (Enfraquecimento da condição)
        assertThrows(ValidationException.class, 
            () -> transacaoService.realizarSaque(conta, 2010.0));
    }

    // --- LINHA 114: Múltiplo de 10 ---

    @Test
    void deveFalharSeNaoForMultiploDeDez() {
        // Mata mutante: Remoção do bloco if (valor % 10 != 0)
        assertThrows(ValidationException.class, 
            () -> transacaoService.realizarSaque(conta, 15.0));
    }

    // --- LINHA 118: Valor Mínimo 10.00 ---

    @Test
    void devePermitirSaqueExatoDe10Reais() throws Exception {
        // Mata mutante: if (valor < 10.00) -> if (valor <= 10.00)
        // Se a condição mudar para <=, o valor 10.00 falharia. O teste garante que passa.
        LocalTime horarioComercial = LocalTime.of(14, 0);

        try (MockedStatic<LocalTime> mockedTime = Mockito.mockStatic(LocalTime.class, Mockito.CALLS_REAL_METHODS)) {
            mockedTime.when(LocalTime::now).thenReturn(horarioComercial);
            when(contaDAO.realizarSaque(anyInt(), anyDouble())).thenReturn(new Transacao());

            assertDoesNotThrow(() -> transacaoService.realizarSaque(conta, 10.0));
        }
    }

    @Test
    void devePermitirSaqueAcimaDoMinimo() throws Exception {
        // Mata mutante: if (valor < 10.00) -> if (valor < 20.00) ou inversão de lógica
        // Garante que valores válidos acima do mínimo continuam passando.
        LocalTime horarioComercial = LocalTime.of(14, 0);

        try (MockedStatic<LocalTime> mockedTime = Mockito.mockStatic(LocalTime.class, Mockito.CALLS_REAL_METHODS)) {
            mockedTime.when(LocalTime::now).thenReturn(horarioComercial);
            when(contaDAO.realizarSaque(anyInt(), anyDouble())).thenReturn(new Transacao());

            assertDoesNotThrow(() -> transacaoService.realizarSaque(conta, 20.0));
        }
    }

    // --- LINHA 122: Conta Salário ---

    @Test
    void deveFalharContaIniciadaCom9() {
        // Mata mutante: if (startsWith("9")) -> if (!startsWith("9"))
        when(conta.getNumero()).thenReturn("91234");
        
        assertThrows(ValidationException.class, 
            () -> transacaoService.realizarSaque(conta, 100.0));
    }

    // --- LINHA 128: Horário (Complexidade Alta) ---

    @Test
    void deveFalharSaqueAltoExatamenteAs22h() {
        // Fronteira Superior: Mata mutante (hora >= 22) -> (hora > 22)
        // Se mudar para > 22, as 22h passariam. O teste garante que 22h já bloqueia.
        LocalTime noite = LocalTime.of(22, 0);
        
        try (MockedStatic<LocalTime> mockedTime = Mockito.mockStatic(LocalTime.class, Mockito.CALLS_REAL_METHODS)) {
            mockedTime.when(LocalTime::now).thenReturn(noite);
            
            assertThrows(ValidationException.class, 
                () -> transacaoService.realizarSaque(conta, 1500.0));
        }
    }

    @Test
    void devePermitirSaqueAltoQuaseAs22h() throws Exception {
        // Fronteira Superior: Mata mutante (hora >= 22) -> (hora >= 21)
        // Testa 21:59 (último minuto permitido).
        LocalTime quaseNoite = LocalTime.of(21, 59);

        try (MockedStatic<LocalTime> mockedTime = Mockito.mockStatic(LocalTime.class, Mockito.CALLS_REAL_METHODS)) {
            mockedTime.when(LocalTime::now).thenReturn(quaseNoite);
            when(contaDAO.realizarSaque(anyInt(), anyDouble())).thenReturn(new Transacao());

            assertDoesNotThrow(() -> transacaoService.realizarSaque(conta, 1500.0));
        }
    }

    @Test
    void deveFalharSaqueAltoQuaseAs06h() {
        // Fronteira Inferior: Mata mutante (hora < 6) -> (hora < 5)
        // Testa 05:59 (ainda é proibido).
        LocalTime madrugada = LocalTime.of(5, 59);
        
        try (MockedStatic<LocalTime> mockedTime = Mockito.mockStatic(LocalTime.class, Mockito.CALLS_REAL_METHODS)) {
            mockedTime.when(LocalTime::now).thenReturn(madrugada);
            
            assertThrows(ValidationException.class, 
                () -> transacaoService.realizarSaque(conta, 1500.0));
        }
    }

    @Test
    void devePermitirSaqueAltoExatamenteAs06h() throws Exception {
        // Fronteira Inferior: Mata mutante (hora < 6) -> (hora <= 6)
        // Testa 06:00 (primeiro minuto permitido).
        LocalTime amanhecer = LocalTime.of(6, 0);

        try (MockedStatic<LocalTime> mockedTime = Mockito.mockStatic(LocalTime.class, Mockito.CALLS_REAL_METHODS)) {
            mockedTime.when(LocalTime::now).thenReturn(amanhecer);
            when(contaDAO.realizarSaque(anyInt(), anyDouble())).thenReturn(new Transacao());

            assertDoesNotThrow(() -> transacaoService.realizarSaque(conta, 1500.0));
        }
    }

    @Test
    void devePermitirSaqueExatoDe1000DeNoite() throws Exception {
        // Valor Limite Noturno: Mata mutante (valor > 1000) -> (valor >= 1000)
        // 1000 exatos é permitido à noite.
        LocalTime noite = LocalTime.of(23, 0);

        try (MockedStatic<LocalTime> mockedTime = Mockito.mockStatic(LocalTime.class, Mockito.CALLS_REAL_METHODS)) {
            mockedTime.when(LocalTime::now).thenReturn(noite);
            when(contaDAO.realizarSaque(anyInt(), anyDouble())).thenReturn(new Transacao());

            assertDoesNotThrow(() -> transacaoService.realizarSaque(conta, 1000.0));
        }
    }

    @Test
    void devePermitirSaqueBaixoDeNoite() throws Exception {
        // Lógica Composta: Mata mutante que remove a parte "valor > 1000" da condição.
        // Garante que a restrição noturna não se aplica a valores baixos.
        LocalTime horarioNoturno = LocalTime.of(23, 0);
        
        try (MockedStatic<LocalTime> mockedTime = Mockito.mockStatic(LocalTime.class, Mockito.CALLS_REAL_METHODS)) {
            mockedTime.when(LocalTime::now).thenReturn(horarioNoturno);
            when(contaDAO.realizarSaque(anyInt(), anyDouble())).thenReturn(new Transacao());

            assertDoesNotThrow(() -> transacaoService.realizarSaque(conta, 100.0));
        }
    }

    // --- LINHA 133: Saldo Insuficiente ---

    @Test
    void devePermitirSaqueDeTodoOSaldo() throws Exception {
        // Mata mutante: if (saldo < valor) -> if (saldo <= valor)
        // Permite zerar a conta.
        when(conta.getSaldo()).thenReturn(100.0);
        LocalTime horarioComercial = LocalTime.of(14, 0);
        
        try (MockedStatic<LocalTime> mockedTime = Mockito.mockStatic(LocalTime.class, Mockito.CALLS_REAL_METHODS)) {
            mockedTime.when(LocalTime::now).thenReturn(horarioComercial);
            when(contaDAO.realizarSaque(anyInt(), anyDouble())).thenReturn(new Transacao());

            assertDoesNotThrow(() -> transacaoService.realizarSaque(conta, 100.0));
        }
    }

    @Test
    void deveFalharQuandoSaldoInsuficiente() {
        // Mata mutante: Remoção do bloco if (saldo < valor)
        when(conta.getSaldo()).thenReturn(100.0);

        assertThrows(ValidationException.class, 
            () -> transacaoService.realizarSaque(conta, 200.0));
    }

    // --- LINHA 137: Chamada DAO ---

    @Test
    void deveChamarDAOSucesso() throws Exception {
        // Mata mutante: Remoção da chamada contaDAO.realizarSaque(...)
        LocalTime horarioComercial = LocalTime.of(14, 0);
        
        try (MockedStatic<LocalTime> mockedTime = Mockito.mockStatic(LocalTime.class, Mockito.CALLS_REAL_METHODS)) {
            mockedTime.when(LocalTime::now).thenReturn(horarioComercial);
            when(contaDAO.realizarSaque(anyInt(), anyDouble())).thenReturn(new Transacao());

            transacaoService.realizarSaque(conta, 100.0);

            verify(contaDAO, times(1)).realizarSaque(1, 100.0);
        }
    }
}