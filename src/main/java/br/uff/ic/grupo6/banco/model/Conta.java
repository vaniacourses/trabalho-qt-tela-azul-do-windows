package br.uff.ic.grupo6.banco.model;

public class Conta {

    private int id;
    private String agencia;
    private String numero;
    private double saldo;

    public Conta(String agencia, String numero, double saldo) {
        this.agencia = agencia;
        this.numero = numero;
        this.saldo = saldo;
    }

    public Conta(String agencia, String numero) {
        this.agencia = agencia;
        this.numero = numero;
        this.saldo = 0.0;
    }

    // Métodos para as operações bancárias
    /**
     * Adiciona um valor ao saldo da conta.
     *
     * @param valor O valor a ser depositado (deve ser positivo)
     */
    public void depositar(double valor) {
        if (valor > 0) {
            this.saldo += valor;
        }
    }

    /**
     * Retira um valor do saldo da conta, se houver fundos suficientes
     *
     * @param valor O valor a ser sacado (deve ser positivo)
     * @return true se o saque foi bem-sucedido, false caso contrário.
     */
    public boolean sacar(double valor) {
        if (valor > 0 && this.saldo >= valor) {
            this.saldo -= valor;
            return true;
        }
        return false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAgencia() {
        return agencia;
    }

    public void setAgencia(String agencia) {
        this.agencia = agencia;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public double getSaldo() {
        return saldo;
    }
    
    public void setSaldo(double valor) {
    	this.saldo = valor;
    }

    // O saldo não tem "set" público para evitar que seja alterado externamente
    // Ele só pode ser modificado através de saques e depósitos
}
