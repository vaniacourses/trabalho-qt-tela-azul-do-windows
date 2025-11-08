package br.uff.ic.grupo6.banco.dao;

import br.uff.ic.grupo6.banco.model.Investimento;
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

public class InvestimentoDAO {

    public Investimento registrarInvestimento(Connection conexao, int idConta, String tipoInvestimento, double valor) throws SQLException {
        String sql = "INSERT INTO INVESTIMENTO (id_conta, tipo_investimento, valor_aplicado, data_aplicacao) VALUES (?, ?, ?, ?)";

        Investimento investimento = null;

        try (PreparedStatement ps = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            Timestamp dataAtual = new Timestamp(System.currentTimeMillis());

            ps.setInt(1, idConta);
            ps.setString(2, tipoInvestimento);
            ps.setBigDecimal(3, BigDecimal.valueOf(valor));
            ps.setTimestamp(4, dataAtual);

            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    investimento = new Investimento();
                    investimento.setId(generatedKeys.getInt(1));
                    investimento.setIdConta(idConta);
                    investimento.setTipoInvestimento(tipoInvestimento);
                    investimento.setValorAplicado(BigDecimal.valueOf(valor));
                    investimento.setDataAplicacao(dataAtual.toLocalDateTime());
                } else {
                    throw new SQLException("Falha ao obter o ID do investimento.");
                }
            }
        }
        return investimento;
    }
     /**
     * Busca investimentos para uma conta específica dentro de um período de datas.
     **/
    public List<Investimento> buscarInvestimentosPorPeriodo(int idConta, LocalDate dataInicio, LocalDate dataFim) throws SQLException {
        List<Investimento> investimentos = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT id, tipo_investimento, valor_aplicado, data_aplicacao, id_conta FROM INVESTIMENTO WHERE id_conta = ?");

        if (dataInicio != null) {
            sqlBuilder.append(" AND data_aplicacao >= ?");
        }
        if (dataFim != null) {
            sqlBuilder.append(" AND data_aplicacao <= ?");
        }
        sqlBuilder.append(" ORDER BY data_aplicacao DESC");

        try (Connection conexao = ConexaoDB.getConexao();
             PreparedStatement ps = conexao.prepareStatement(sqlBuilder.toString())) {

            int paramIndex = 1;
            ps.setInt(paramIndex++, idConta);

            if (dataInicio != null) {
                ps.setTimestamp(paramIndex++, Timestamp.valueOf(dataInicio.atStartOfDay()));
            }
            if (dataFim != null) {
                ps.setTimestamp(paramIndex++, Timestamp.valueOf(dataFim.atTime(23, 59, 59, 999999999)));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Investimento investimento = new Investimento();
                    investimento.setId(rs.getInt("id"));
                    investimento.setTipoInvestimento(rs.getString("tipo_investimento"));
                    investimento.setValorAplicado(rs.getBigDecimal("valor_aplicado"));
                    investimento.setDataAplicacao(rs.getTimestamp("data_aplicacao").toLocalDateTime());
                    investimento.setIdConta(rs.getInt("id_conta"));
                    investimentos.add(investimento);
                }
            }
        }
        return investimentos;
    }
}