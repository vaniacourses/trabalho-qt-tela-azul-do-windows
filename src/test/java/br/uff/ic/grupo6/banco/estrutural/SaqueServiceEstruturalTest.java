package br.uff.ic.grupo6.banco.estrutural;

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

import java.sql.SQLException;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SaqueServiceEstruturalTest {

    @Mock
    private ContaDAO contaDAO;

    @Mock
    private TransacaoDAO transacaoDAO;

    @Mock
    private UsuarioDAO usuarioDAO;

    @InjectMocks
    private TransacaoService transacaoService;

    // Objeto mockado (não real) para isolamento total
    private Conta conta;

    @BeforeEach
    void setup() {
        conta = mock(Conta.class);
        
        // Configuração padrão do Mock usando lenient() para evitar erros de stub não utilizado
        lenient().when(conta.getId()).thenReturn(1);
        lenient().when(conta.getSaldo()).thenReturn(5000.0);
        lenient().when(conta.getNumero()).thenReturn("12345");
    }

    @Test
    void valorNegativo() {
        assertThrows(ValidationException.class, 
            () -> transacaoService.realizarSaque(conta, -10.0));
    }

    @Test
    void valorAcimaDoLimite() {
        assertThrows(ValidationException.class, 
            () -> transacaoService.realizarSaque(conta, 2001.0));
    }

    @Test
    void valorNaoMultiploDeDez() {
        assertThrows(ValidationException.class, 
            () -> transacaoService.realizarSaque(conta, 15.0));
    }

    @Test
    void contaSalarioBloqueada() {
        when(conta.getNumero()).thenReturn("91234");
        
        assertThrows(ValidationException.class, 
            () -> transacaoService.realizarSaque(conta, 100.0));
    }

    @Test
    void bloqueioDeHorarioNoturno() {
        // CORREÇÃO: Adicionado CALLS_REAL_METHODS para evitar NullPointerException se o stub falhar
        try (MockedStatic<LocalTime> mockedTime = Mockito.mockStatic(LocalTime.class, Mockito.CALLS_REAL_METHODS)) {
            LocalTime noite = LocalTime.of(23, 0);
            
            // Força o retorno de 23:00 quando LocalTime.now() for chamado
            mockedTime.when(() -> LocalTime.now()).thenReturn(noite);
            
            ValidationException ex = assertThrows(ValidationException.class, 
                () -> transacaoService.realizarSaque(conta, 1500.0));
                
            assertTrue(ex.getMessage().contains("entre 06:00 e 22:00"));
        }
    }

    @Test
    void saldoInsuficiente() {
        when(conta.getSaldo()).thenReturn(50.0);
        
        assertThrows(ValidationException.class, 
            () -> transacaoService.realizarSaque(conta, 100.0));
    }

    @Test
    void saqueComSucesso() throws SQLException, ValidationException {
        // CORREÇÃO: Adicionado CALLS_REAL_METHODS para evitar NullPointerException
        try (MockedStatic<LocalTime> mockedTime = Mockito.mockStatic(LocalTime.class, Mockito.CALLS_REAL_METHODS)) {
            LocalTime dia = LocalTime.of(14, 0);
            
            // Força o retorno de 14:00 (horário comercial)
            mockedTime.when(() -> LocalTime.now()).thenReturn(dia);

            Transacao transacaoEsperada = new Transacao();
            when(contaDAO.realizarSaque(anyInt(), anyDouble())).thenReturn(transacaoEsperada);

            Transacao resultado = transacaoService.realizarSaque(conta, 100.0);

            assertNotNull(resultado);
            verify(contaDAO).realizarSaque(1, 100.0);
        }
    }
}