package br.uff.ic.grupo6.banco.dao;

import br.uff.ic.grupo6.banco.model.Conta;
import br.uff.ic.grupo6.banco.model.Transacao;
import br.uff.ic.grupo6.banco.model.Investimento;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ContaDAO {

    /**
     * Busca uma conta pela agência e número.
     */
    public Conta buscarContaPorAgenciaENumeroDaConta(String agencia, String numeroConta) throws SQLException {
        String sql = "SELECT c.id, c.agencia, c.numero, c.saldo "
                + "FROM CONTA c "
                + "WHERE c.agencia = ? AND c.numero = ?";

        Conta conta = null;
        if (agencia != null && numeroConta != null) {
            try (Connection conexao = ConexaoDB.getConexao(); PreparedStatement ps = conexao.prepareStatement(sql)) {
                ps.setString(1, agencia);
                ps.setString(2, numeroConta);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    conta = new Conta(
                            rs.getString("agencia"),
                            rs.getString("numero"),
                            rs.getDouble("saldo"));
                    conta.setId(rs.getInt("id"));
                }
            }
        }
        return conta;
    }

    /**
     * Realiza um depósito, atualizando o saldo e registrando a transação.
     * Retorna um objeto Transacao com os detalhes do comprovante.
     */
    public synchronized Transacao realizarDeposito(int idConta, double valor) throws SQLException {
        String sqlUpdate = "UPDATE CONTA SET saldo = saldo + ? WHERE id = ?";
        Transacao transacaoRegistrada = null;
        Connection conexao = null;

        try {
            conexao = ConexaoDB.getConexao();
            conexao.setAutoCommit(false);

            // 1. Atualiza o saldo na conta
            try (PreparedStatement psUpdate = conexao.prepareStatement(sqlUpdate)) {
                psUpdate.setDouble(1, valor);
                psUpdate.setInt(2, idConta);
                psUpdate.executeUpdate();
            }

            // 2. Registra a operação na tabela de transações
            TransacaoDAO transacaoDAO = new TransacaoDAO();
            transacaoRegistrada = transacaoDAO.registrarTransacao(conexao, idConta, "DEPOSITO", valor);

            conexao.commit();

        } catch (SQLException e) {
            if (conexao != null) {
                conexao.rollback();
            }
            throw e;
        } finally {
            if (conexao != null) {
                conexao.setAutoCommit(true);
                conexao.close();
            }
        }
        return transacaoRegistrada;
    }

    /**
     * Realiza uma transferência entre duas contas. 
     * Atualiza o saldo de ambas e registra as duas transações.
     */
    public synchronized List<Transacao> realizarTransferencia(int idRemetente, int idDestinatario, double valor) throws SQLException {
        String sqlRemetente = "UPDATE CONTA SET saldo = saldo - ? WHERE id = ?";
        String sqlDestinatario = "UPDATE CONTA SET saldo = saldo + ? WHERE id = ?";

        List<Transacao> transacoes = new ArrayList<>();
        Connection conexao = null;

        try {
            conexao = ConexaoDB.getConexao();
            conexao.setAutoCommit(false);

            // 1. Executa as atualizações de saldo
            try (PreparedStatement psRemetente = conexao.prepareStatement(sqlRemetente); PreparedStatement psDestinatario = conexao.prepareStatement(sqlDestinatario)) {

                psRemetente.setDouble(1, valor);
                psRemetente.setInt(2, idRemetente);

                psDestinatario.setDouble(1, valor);
                psDestinatario.setInt(2, idDestinatario);

                psRemetente.executeUpdate();
                psDestinatario.executeUpdate();
            }

            // 2. Registra as duas transações
            TransacaoDAO transacaoDAO = new TransacaoDAO();

            Transacao debito = transacaoDAO.registrarTransacao(conexao, idRemetente, "TRANSF_ENVIADA", valor);
            Transacao credito = transacaoDAO.registrarTransacao(conexao, idDestinatario, "TRANSF_RECEBIDA", valor);

            transacoes.add(debito);
            transacoes.add(credito);

            conexao.commit();

        } catch (SQLException e) {
            if (conexao != null) {
                conexao.rollback();
            }
            throw e;
        } finally {
            if (conexao != null) {
                conexao.setAutoCommit(true);
                conexao.close();
            }
        }
        return transacoes;
    }
    
    /**
     * Realiza um saque, atualizando o saldo e registrando a transação.
     * Retorna um objeto Transacao com os detalhes do comprovante.
     */
    public synchronized Transacao realizarSaque(int idConta, double valor) throws SQLException {
        String sqlUpdate = "UPDATE CONTA SET saldo = saldo - ? WHERE id = ?";
        Transacao transacaoRegistrada = null;
        Connection conexao = null;

        try {
            conexao = ConexaoDB.getConexao(); // Obtém a conexão com o banco
            conexao.setAutoCommit(false); // Inicia a transação

            // 1. Atualiza o saldo na conta (decrementa)
            try (PreparedStatement psUpdate = conexao.prepareStatement(sqlUpdate)) {
                psUpdate.setDouble(1, valor);
                psUpdate.setInt(2, idConta);
                psUpdate.executeUpdate();
            }

            // 2. Registra a operação na tabela de transações
            TransacaoDAO transacaoDAO = new TransacaoDAO();
            transacaoRegistrada = transacaoDAO.registrarTransacao(conexao, idConta, "SAQUE", valor); // Usa o tipo "SAQUE"

            conexao.commit(); // Confirma a transação

        } catch (SQLException e) {
            if (conexao != null) {
                conexao.rollback(); // Em caso de erro, reverte a transação
            }
            throw e; // Lança a exceção para a camada superior
        } finally {
            if (conexao != null) {
                conexao.setAutoCommit(true); // Restaura o auto-commit
                conexao.close(); // Fecha a conexão
            }
        }
        return transacaoRegistrada;
    }
    /**
    * Realiza um investimento, deduzindo o valor do saldo da conta e registrando a aplicação.
    * Retorna um objeto Investimento com os detalhes do comprovante.
    **/
public synchronized Investimento realizarInvestimento(int idConta, String tipoInvestimento, double valor) throws SQLException {
    String sqlUpdate = "UPDATE CONTA SET saldo = saldo - ? WHERE id = ?";
    Investimento investimentoRegistrado = null;
    Connection conexao = null;

    try {
        conexao = ConexaoDB.getConexao();
        conexao.setAutoCommit(false);

        // 1. Atualiza o saldo na conta (deduz o valor do investimento)
        try (PreparedStatement psUpdate = conexao.prepareStatement(sqlUpdate)) {
            psUpdate.setDouble(1, valor);
            psUpdate.setInt(2, idConta);
            psUpdate.executeUpdate();
        }

        // 2. Registra o investimento na tabela de investimentos
        InvestimentoDAO investimentoDAO = new InvestimentoDAO();
        investimentoRegistrado = investimentoDAO.registrarInvestimento(conexao, idConta, tipoInvestimento, valor);

        conexao.commit();

    } catch (SQLException e) {
        if (conexao != null) {
            conexao.rollback();
        }
        throw e;
    } finally {
        if (conexao != null) {
            conexao.setAutoCommit(true);
            conexao.close();
        }
    }
    return investimentoRegistrado;
}
}
