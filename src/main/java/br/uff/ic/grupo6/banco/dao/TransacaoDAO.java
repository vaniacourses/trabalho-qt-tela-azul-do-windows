package br.uff.ic.grupo6.banco.dao;

import br.uff.ic.grupo6.banco.model.Transacao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TransacaoDAO {

    public Transacao registrarTransacao(Connection conexao, int idConta, String tipo, double valor) throws SQLException {
        String sql = "INSERT INTO TRANSACAO (id_conta, tipo, valor, data_transacao) VALUES (?, ?, ?, ?)";
        Transacao transacao = null;

        try (PreparedStatement ps = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            Timestamp dataAtual = new Timestamp(System.currentTimeMillis());

            ps.setInt(1, idConta);
            ps.setString(2, tipo);
            ps.setBigDecimal(3, BigDecimal.valueOf(valor));
            ps.setTimestamp(4, dataAtual);

            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    transacao = new Transacao();
                    transacao.setId(generatedKeys.getInt(1));
                    transacao.setIdConta(idConta);
                    transacao.setTipo(tipo);
                    transacao.setValor(BigDecimal.valueOf(valor));
                    transacao.setDataTransacao(dataAtual.toLocalDateTime());
                } else {
                    throw new SQLException("Falha ao obter o ID da transação.");
                }
            }
        }
        return transacao;
    }

    /**
     * Busca todas as transações para uma conta específica, ordenadas pela data da transação (mais recente primeiro).
     * @param idConta O ID da conta.
     * @return Uma lista de objetos Transacao.
     * @throws SQLException
     */
    public List<Transacao> buscarTodasTransacoesPorConta(int idConta) throws SQLException {
        List<Transacao> transacoes = new ArrayList<>(); // Adicione 'import java.util.ArrayList;' se não estiver presente
        String sql = "SELECT id, tipo, valor, data_transacao, id_conta FROM TRANSACAO WHERE id_conta = ? ORDER BY data_transacao DESC";

        try (Connection conexao = ConexaoDB.getConexao();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setInt(1, idConta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Transacao transacao = new Transacao();
                    transacao.setId(rs.getInt("id"));
                    transacao.setTipo(rs.getString("tipo"));
                    transacao.setValor(rs.getBigDecimal("valor"));
                    // Converte Timestamp para LocalDateTime
                    transacao.setDataTransacao(rs.getTimestamp("data_transacao").toLocalDateTime());
                    transacao.setIdConta(rs.getInt("id_conta"));
                    transacoes.add(transacao);
                }
            }
        }
        return transacoes;
    }

    /**
     * Busca transações para uma conta específica dentro de um período de datas.
     * As datas de início e fim são opcionais.
     * @param idConta O ID da conta.
     * @param dataInicio Data de início do período (opcional).
     * @param dataFim Data de fim do período (opcional).
     * @return Uma lista de objetos Transacao.
     * @throws SQLException
     */
    public List<Transacao> buscarTransacoesPorPeriodo(int idConta, LocalDate dataInicio, LocalDate dataFim) throws SQLException {
        List<Transacao> transacoes = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT id, tipo, valor, data_transacao, id_conta FROM TRANSACAO WHERE id_conta = ?");

        if (dataInicio != null) {
            sqlBuilder.append(" AND data_transacao >= ?");
        }
        if (dataFim != null) {
            sqlBuilder.append(" AND data_transacao <= ?");
        }
        sqlBuilder.append(" ORDER BY data_transacao DESC");

        try (Connection conexao = ConexaoDB.getConexao();
             PreparedStatement ps = conexao.prepareStatement(sqlBuilder.toString())) {

            int paramIndex = 1;
            ps.setInt(paramIndex++, idConta);

            if (dataInicio != null) {
                // Para incluir o dia inteiro, vamos usar o início do dia
                ps.setTimestamp(paramIndex++, Timestamp.valueOf(dataInicio.atStartOfDay()));
            }
            if (dataFim != null) {
                // Para incluir o dia inteiro, vamos usar o final do dia (23:59:59.999...)
                ps.setTimestamp(paramIndex++, Timestamp.valueOf(dataFim.atTime(23, 59, 59, 999999999)));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Transacao transacao = new Transacao();
                    transacao.setId(rs.getInt("id"));
                    transacao.setTipo(rs.getString("tipo"));
                    transacao.setValor(rs.getBigDecimal("valor"));
                    transacao.setDataTransacao(rs.getTimestamp("data_transacao").toLocalDateTime());
                    transacao.setIdConta(rs.getInt("id_conta"));
                    transacoes.add(transacao);
                }
            }
        }
        return transacoes;
    }
}