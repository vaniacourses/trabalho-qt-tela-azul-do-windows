package br.uff.ic.grupo6.banco.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class Transacao {

    private int id;
    private String tipo;
    private BigDecimal valor;
    private LocalDateTime dataTransacao;
    private int idConta;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public LocalDateTime getDataTransacao() {
        return dataTransacao;
    }

    public void setDataTransacao(LocalDateTime dataTransacao) {
        this.dataTransacao = dataTransacao;
    }

    public int getIdConta() {
        return idConta;
    }

    public void setIdConta(int idConta) {
        this.idConta = idConta;
    }
    
    public Date getDataTransacaoAsDate() {
        if (this.dataTransacao == null) {
            return null;
        }
        // Converte LocalDateTime para Instant e depois para java.util.Date... Necessário para visualizar o Extrato
        return Date.from(this.dataTransacao.atZone(ZoneId.systemDefault()).toInstant());
    }
}
