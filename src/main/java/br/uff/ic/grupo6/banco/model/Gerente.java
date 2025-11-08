package br.uff.ic.grupo6.banco.model;

public class Gerente extends Usuario {

    // Atributos específicos do Gerente
    private String nome;
    private String cargo;

    public Gerente(String login, String senha) {
        super(login, senha);
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }
}
