package br.uff.ic.grupo6.banco.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.sql.Timestamp;

public class Investimento {
    private int id;
    private String tipoInvestimento;
    private BigDecimal valorAplicado;
    private LocalDateTime dataAplicacao;
    private int idConta;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTipoInvestimento() {
        return tipoInvestimento;
    }

    public void setTipoInvestimento(String tipoInvestimento) {
        this.tipoInvestimento = tipoInvestimento;
    }

    public BigDecimal getValorAplicado() {
        return valorAplicado;
    }

    public void setValorAplicado(BigDecimal valorAplicado) {
        this.valorAplicado = valorAplicado;
    }

    public LocalDateTime getDataAplicacao() {
        return dataAplicacao;
    }

    public void setDataAplicacao(LocalDateTime dataAplicacao) {
        this.dataAplicacao = dataAplicacao;
    }

    public int getIdConta() {
        return idConta;
    }

    public void setIdConta(int idConta) {
        this.idConta = idConta;
    }
    
    public Date getDataTransacaoAsDate() {
        if (this.dataAplicacao == null) {
            return null;
        }
        // Converte LocalDateTime para Instant e depois para java.util.Date... Necessário para visualizar o Histórico
        return Date.from(this.dataAplicacao.atZone(ZoneId.systemDefault()).toInstant());
    }
}