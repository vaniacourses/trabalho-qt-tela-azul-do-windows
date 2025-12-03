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

    // 1 — Matar mutante que altera condição valor <= 0 para valor < 0
    @Test
    void deveFalharQuandoValorIgualZero() {
        assertThrows(ValidationException.class, 
            () -> transacaoService.realizarSaque(conta, 0.0));
    }

    // 2 — Matar mutante que remove verificação de Múltiplo de 10
    @Test
    void deveFalharSeNaoForMultiploDeDez() {
        assertThrows(ValidationException.class, 
            () -> transacaoService.realizarSaque(conta, 15.0));
    }

    // 3 — Matar mutante que altera limite 2000 (> para >=)
    @Test
    void devePermitirSaqueExatoDe2000() throws Exception {
        // CORREÇÃO: Criar o objeto de tempo FORA do mock estático
        LocalTime horarioComercial = LocalTime.of(14, 0);

        try (MockedStatic<LocalTime> mockedTime = Mockito.mockStatic(LocalTime.class, Mockito.CALLS_REAL_METHODS)) {
            mockedTime.when(LocalTime::now).thenReturn(horarioComercial);
            
            when(contaDAO.realizarSaque(anyInt(), anyDouble())).thenReturn(new Transacao());

            assertDoesNotThrow(() -> transacaoService.realizarSaque(conta, 2000.0));
        }
    }

    // 4 — Matar mutante que altera limite 2000 (> para > 2010 ou remove)
    @Test
    void deveFalharSaqueAcimaDe2000() {
        assertThrows(ValidationException.class, 
            () -> transacaoService.realizarSaque(conta, 2010.0));
    }

    // 5 — Matar mutante que altera validação de Saldo (ex: < para <=)
    @Test
    void devePermitirSaqueDeTodoOSaldo() throws Exception {
        when(conta.getSaldo()).thenReturn(100.0);
        
        // CORREÇÃO: Criar o objeto de tempo FORA do mock estático
        LocalTime horarioComercial = LocalTime.of(14, 0);
        
        try (MockedStatic<LocalTime> mockedTime = Mockito.mockStatic(LocalTime.class, Mockito.CALLS_REAL_METHODS)) {
            mockedTime.when(LocalTime::now).thenReturn(horarioComercial);
            
            when(contaDAO.realizarSaque(anyInt(), anyDouble())).thenReturn(new Transacao());

            assertDoesNotThrow(() -> transacaoService.realizarSaque(conta, 100.0));
        }
    }

    // 6 — Matar mutante que inverte lógica da Conta Salário (startsWith vs !startsWith)
    @Test
    void deveFalharContaIniciadaCom9() {
        when(conta.getNumero()).thenReturn("91234");
        
        assertThrows(ValidationException.class, 
            () -> transacaoService.realizarSaque(conta, 100.0));
    }

    // 7 — Matar mutante que altera operador lógico de Horário (&& por ||)
    @Test
    void deveFalharSaqueAltoDeNoite() {
        // CORREÇÃO: Criar o objeto de tempo FORA do mock estático
        LocalTime horarioNoturno = LocalTime.of(23, 0);
        
        try (MockedStatic<LocalTime> mockedTime = Mockito.mockStatic(LocalTime.class, Mockito.CALLS_REAL_METHODS)) {
            mockedTime.when(LocalTime::now).thenReturn(horarioNoturno);
            
            assertThrows(ValidationException.class, 
                () -> transacaoService.realizarSaque(conta, 1500.0));
        }
    }

    // 8 — Matar mutante que remove a restrição de valor no horário (apenas checa horário)
    @Test
    void devePermitirSaqueBaixoDeNoite() throws Exception {
        // CORREÇÃO: Criar o objeto de tempo FORA do mock estático
        LocalTime horarioNoturno = LocalTime.of(23, 0);
        
        try (MockedStatic<LocalTime> mockedTime = Mockito.mockStatic(LocalTime.class, Mockito.CALLS_REAL_METHODS)) {
            mockedTime.when(LocalTime::now).thenReturn(horarioNoturno);
            
            when(contaDAO.realizarSaque(anyInt(), anyDouble())).thenReturn(new Transacao());

            assertDoesNotThrow(() -> transacaoService.realizarSaque(conta, 100.0));
        }
    }

    // 9 — Matar mutante que remove a chamada ao DAO
    @Test
    void deveChamarDAOSucesso() throws Exception {
        // CORREÇÃO: Criar o objeto de tempo FORA do mock estático
        LocalTime horarioComercial = LocalTime.of(14, 0);
        
        try (MockedStatic<LocalTime> mockedTime = Mockito.mockStatic(LocalTime.class, Mockito.CALLS_REAL_METHODS)) {
            mockedTime.when(LocalTime::now).thenReturn(horarioComercial);
            
            when(contaDAO.realizarSaque(anyInt(), anyDouble())).thenReturn(new Transacao());

            transacaoService.realizarSaque(conta, 100.0);

            verify(contaDAO, times(1)).realizarSaque(1, 100.0);
        }
    }
}