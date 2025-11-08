package br.uff.ic.grupo6.banco.dao;

import br.uff.ic.grupo6.banco.model.Cliente;
import br.uff.ic.grupo6.banco.model.Conta;
import br.uff.ic.grupo6.banco.model.Gerente;
import br.uff.ic.grupo6.banco.model.Usuario;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) para a entidade Usuario. Centraliza todas as
 * operações de banco de dados relacionadas a Clientes e Gerentes.
 */
public class UsuarioDAO {

    /**
     * Busca um usuário (Cliente ou Gerente) pelo CPF. Faz um LEFT JOIN com a
     * tabela CONTA para já trazer os dados bancários se existirem.
     *
     * @param cpf O CPF a ser buscado.
     * @return Um objeto Usuario (Cliente ou Gerente), ou null se não for
     * encontrado.
     * @throws SQLException
     */
    public Usuario buscarPorCpf(String cpf) throws SQLException {
        String sql = "SELECT u.*, c.id as conta_id, c.agencia, c.numero, c.saldo "
                + "FROM USUARIO u "
                + "LEFT JOIN CONTA c ON u.id = c.id_usuario "
                + "WHERE u.cpf = ?";
        Usuario usuario = null;
        try (Connection conexao = ConexaoDB.getConexao(); PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, cpf);
            ResultSet rs = ps.executeQuery();

            // Se encontrou um resultado, monta o objeto correspondente
            if (rs.next()) {
                String tipo = rs.getString("tipo");
                // Verifica se é Cliente ou Gerente para criar o objeto correto
                if ("CLIENTE".equals(tipo)) {
                    Cliente cliente = new Cliente(rs.getString("cpf"), rs.getString("senha"), rs.getString("nome"), rs.getString("cpf"));
                    // Preenche todos os atributos do cliente com os dados do banco
                    cliente.setId(rs.getInt("id"));
                    java.sql.Date dataNascimentoSql = rs.getDate("data_nascimento");
                    if (dataNascimentoSql != null) {
                        cliente.setDataNascimento(dataNascimentoSql.toLocalDate());
                    }
                    cliente.setEmail(rs.getString("email"));
                    cliente.setTelefone(rs.getString("telefone"));
                    cliente.setCep(rs.getString("cep"));
                    cliente.setEndereco(rs.getString("endereco"));
                    cliente.setBairro(rs.getString("bairro"));
                    cliente.setCidade(rs.getString("cidade"));
                    cliente.setEstado(rs.getString("estado"));
                    cliente.setOcupacao(rs.getString("ocupacao"));
                    double renda = rs.getDouble("renda");
                    if (!rs.wasNull()) {
                        cliente.setRenda(renda);
                    }
                    // Se o cliente tiver uma conta, cria e associa o objeto Conta
                    if (rs.getString("agencia") != null) {
                        Conta conta = new Conta(rs.getString("agencia"), rs.getString("numero"), rs.getDouble("saldo"));
                        conta.setId(rs.getInt("conta_id"));
                        cliente.setConta(conta);
                    }
                    usuario = cliente;
                } else if ("GERENTE".equals(tipo)) {
                    Gerente gerente = new Gerente(rs.getString("cpf"), rs.getString("senha"));
                    gerente.setId(rs.getInt("id"));
                    gerente.setNome(rs.getString("nome"));
                    usuario = gerente;
                }
            }
        }
        return usuario;
    }

    /**
     * Cadastra um novo cliente e sua respectiva conta bancária. Usa uma
     * transação para garantir que ou ambos são criados, ou nenhum é.
     *
     * @param cliente O objeto Cliente com todos os dados preenchidos.
     * @throws SQLException
     */
    public void cadastrarCliente(Cliente cliente) throws SQLException {
        String sqlUsuario = "INSERT INTO USUARIO (nome, cpf, login, senha, tipo, data_nascimento, email, telefone, cep, endereco, bairro, cidade, estado, renda, ocupacao) VALUES (?, ?, ?, ?, 'CLIENTE', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlConta = "INSERT INTO CONTA (agencia, numero, id_usuario) VALUES (?, ?, ?)";
        Connection conexao = null;
        PreparedStatement psUsuario = null;
        PreparedStatement psConta = null;
        ResultSet generatedKeys = null;
        try {
            conexao = ConexaoDB.getConexao();
            // Inicia uma transação, desativando o auto-commit
            conexao.setAutoCommit(false);

            // Insere o usuário e pega o ID gerado pelo banco
            psUsuario = conexao.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS);
            // ... (seta todos os parâmetros do cliente)
            psUsuario.setString(1, cliente.getNome());
            psUsuario.setString(2, cliente.getCpf());
            psUsuario.setString(3, cliente.getCpf());
            psUsuario.setString(4, cliente.getSenha());
            psUsuario.setDate(5, Date.valueOf(cliente.getDataNascimento()));
            psUsuario.setString(6, cliente.getEmail());
            psUsuario.setString(7, cliente.getTelefone());
            psUsuario.setString(8, cliente.getCep());
            psUsuario.setString(9, cliente.getEndereco());
            psUsuario.setString(10, cliente.getBairro());
            psUsuario.setString(11, cliente.getCidade());
            psUsuario.setString(12, cliente.getEstado());
            psUsuario.setDouble(13, cliente.getRenda());
            psUsuario.setString(14, cliente.getOcupacao());
            psUsuario.executeUpdate();

            generatedKeys = psUsuario.getGeneratedKeys();
            if (generatedKeys.next()) {
                int idUsuario = generatedKeys.getInt(1);
                // Usa o ID do usuário recém-criado para criar a conta associada a ele
                psConta = conexao.prepareStatement(sqlConta);
                psConta.setString(1, "0001"); // Agência padrão
                String numeroConta = String.format("%06d", (100000 + idUsuario)); // Lógica para gerar número da conta
                psConta.setString(2, numeroConta);
                psConta.setInt(3, idUsuario);
                psConta.executeUpdate();
            } else {
                throw new SQLException("Falha ao obter o ID do usuário, nenhuma linha inserida.");
            }
            // Se tudo correu bem, efetiva a transação
            conexao.commit();
        } catch (SQLException e) {
            // Se qualquer erro ocorreu, desfaz todas as operações da transação
            if (conexao != null) {
                conexao.rollback();
            }
            throw new SQLException("Erro ao cadastrar cliente: " + e.getMessage(), e);
        } finally {
            // fechar todos os recursos abertos (conexão, statements, etc)
            if (generatedKeys != null) try {
                generatedKeys.close();
            } catch (SQLException e) {
                /* Ignora */ }
            if (psUsuario != null) try {
                psUsuario.close();
            } catch (SQLException e) {
                /* Ignora */ }
            if (psConta != null) try {
                psConta.close();
            } catch (SQLException e) {
                /* Ignora */ }
            if (conexao != null) {
                try {
                    conexao.setAutoCommit(true);
                    conexao.close();
                } catch (SQLException e) {
                    /* Ignora */ }
            }
        }
    }

    /**
     * Atualiza os dados cadastrais de um cliente (exceto CPF e senha). O
     * 'synchronized' previne problemas de concorrência se duas threads tentarem
     * atualizar o mesmo cliente ao mesmo tempo.
     *
     * @param cliente Objeto cliente com os dados atualizados.
     * @throws SQLException
     */
    public synchronized void atualizarCliente(Cliente cliente) throws SQLException {
        String sql = "UPDATE USUARIO SET nome = ?, data_nascimento = ?, email = ?, telefone = ?, cep = ?, endereco = ?, bairro = ?, cidade = ?, estado = ?, renda = ?, ocupacao = ? WHERE id = ?";
        try (Connection conexao = ConexaoDB.getConexao(); PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, cliente.getNome());
            ps.setDate(2, Date.valueOf(cliente.getDataNascimento()));
            ps.setString(3, cliente.getEmail());
            ps.setString(4, cliente.getTelefone());
            ps.setString(5, cliente.getCep());
            ps.setString(6, cliente.getEndereco());
            ps.setString(7, cliente.getBairro());
            ps.setString(8, cliente.getCidade());
            ps.setString(9, cliente.getEstado());
            ps.setDouble(10, cliente.getRenda());
            ps.setString(11, cliente.getOcupacao());
            ps.setInt(12, cliente.getId());
            ps.executeUpdate();
        }
    }

    /**
     * Atualiza apenas a senha de um cliente.
     *
     * @param idCliente ID do cliente a ter a senha atualizada
     * @param novaSenha
     * @throws SQLException
     */
    public void atualizarSenha(int idCliente, String novaSenha) throws SQLException {
        String sql = "UPDATE USUARIO SET senha = ? WHERE id = ?";
        try (Connection conexao = ConexaoDB.getConexao(); PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, novaSenha);
            ps.setInt(2, idCliente);
            ps.executeUpdate();
        }
    }

    /**
     * Busca um cliente específico pelo seu ID de usuário.
     *
     * @param id O ID do usuário.
     * @return O objeto Cliente correspondente, ou null se não for encontrado.
     * @throws SQLException
     */
    public Cliente buscarClientePorId(int id) throws SQLException {
        // A lógica de busca e montagem do objeto é similar ao buscarPorCpf
        String sql = "SELECT u.*, c.id as conta_id, c.agencia, c.numero, c.saldo "
                + "FROM USUARIO u "
                + "LEFT JOIN CONTA c ON u.id = c.id_usuario "
                + "WHERE u.id = ? AND u.tipo = 'CLIENTE'";
        Cliente cliente = null;
        try (Connection conexao = ConexaoDB.getConexao(); PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                cliente = new Cliente(rs.getString("cpf"), rs.getString("senha"), rs.getString("nome"), rs.getString("cpf"));
                cliente.setId(rs.getInt("id"));
                java.sql.Date dataNascimentoSql = rs.getDate("data_nascimento");
                if (dataNascimentoSql != null) {
                    cliente.setDataNascimento(dataNascimentoSql.toLocalDate());
                }
                cliente.setEmail(rs.getString("email"));
                cliente.setTelefone(rs.getString("telefone"));
                cliente.setCep(rs.getString("cep"));
                cliente.setEndereco(rs.getString("endereco"));
                cliente.setBairro(rs.getString("bairro"));
                cliente.setCidade(rs.getString("cidade"));
                cliente.setEstado(rs.getString("estado"));
                cliente.setOcupacao(rs.getString("ocupacao"));
                double renda = rs.getDouble("renda");
                if (!rs.wasNull()) {
                    cliente.setRenda(renda);
                }
                if (rs.getString("agencia") != null) {
                    Conta conta = new Conta(rs.getString("agencia"), rs.getString("numero"), rs.getDouble("saldo"));
                    conta.setId(rs.getInt("conta_id"));
                    cliente.setConta(conta);
                }
            }
        }
        return cliente;
    }

    /**
     * Busca clientes por um termo de pesquisa (nome ou CPF). Usado na área do
     * gerente.
     *
     * @param termo O texto a ser buscado no nome ou CPF.
     * @return Uma lista de Clientes que correspondem à busca.
     * @throws SQLException
     */
    public List<Cliente> buscarClientesPorTermo(String termo) throws SQLException {
        List<Cliente> clientes = new ArrayList<>();
        // A busca usa LIKE com '%' para procurar por partes do nome ou do CPF
        String sql = "SELECT u.*, c.id as conta_id, c.agencia, c.numero, c.saldo "
                + "FROM USUARIO u "
                + "LEFT JOIN CONTA c ON u.id = c.id_usuario "
                + "WHERE u.tipo = 'CLIENTE' AND (LOWER(u.nome) LIKE ? OR u.cpf LIKE ?)";
        try (Connection conexao = ConexaoDB.getConexao(); PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, "%" + termo.toLowerCase() + "%");
            ps.setString(2, termo + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                // Monta uma lista de clientes encontrados
                Cliente cliente = new Cliente(rs.getString("cpf"), rs.getString("senha"), rs.getString("nome"), rs.getString("cpf"));
                cliente.setId(rs.getInt("id"));
                cliente.setEmail(rs.getString("email"));
                if (rs.getString("agencia") != null) {
                    Conta conta = new Conta(rs.getString("agencia"), rs.getString("numero"), rs.getDouble("saldo"));
                    conta.setId(rs.getInt("conta_id"));
                    cliente.setConta(conta);
                }
                clientes.add(cliente);
            }
        }
        return clientes;
    }

    /**
     * Exclui um cliente e todos os seus dados relacionados (conta, transações,
     * investimentos). Usa uma transação para garantir a integridade dos dados.
     *
     * @param idUsuario O ID do usuário a ser excluído.
     * @throws SQLException
     */
    public void excluirClientePorId(int idUsuario) throws SQLException {
        // A ordem dos deletes é importante para respeitar as chaves estrangeiras (de baixo para cima)
        String sqlDeleteInvestimentos = "DELETE FROM INVESTIMENTO WHERE id_conta IN (SELECT id FROM CONTA WHERE id_usuario = ?)";
        String sqlDeleteTransacoes = "DELETE FROM TRANSACAO WHERE id_conta IN (SELECT id FROM CONTA WHERE id_usuario = ?)";
        String sqlDeleteConta = "DELETE FROM CONTA WHERE id_usuario = ?";
        String sqlDeleteUsuario = "DELETE FROM USUARIO WHERE id = ? AND tipo = 'CLIENTE'";
        Connection conexao = null;
        try {
            conexao = ConexaoDB.getConexao();
            conexao.setAutoCommit(false); // Inicia transação
            // Executa cada delete em sequência
            try (PreparedStatement ps = conexao.prepareStatement(sqlDeleteInvestimentos)) {
                ps.setInt(1, idUsuario);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conexao.prepareStatement(sqlDeleteTransacoes)) {
                ps.setInt(1, idUsuario);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conexao.prepareStatement(sqlDeleteConta)) {
                ps.setInt(1, idUsuario);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conexao.prepareStatement(sqlDeleteUsuario)) {
                ps.setInt(1, idUsuario);
                ps.executeUpdate();
            }
            conexao.commit(); // Confirma todos os deletes
        } catch (SQLException e) {
            if (conexao != null) {
                conexao.rollback(); // Desfaz tudo se der erro
            }
            throw e;
        } finally {
            if (conexao != null) {
                conexao.setAutoCommit(true);
                conexao.close();
            }
        }
    }

    /**
     * Busca todos os clientes cadastrados, ordenados por nome.
     *
     * @return Uma lista de todos os Clientes.
     * @throws SQLException
     */
    public List<Cliente> buscarTodosClientesOrdenados() throws SQLException {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT u.*, c.id as conta_id, c.agencia, c.numero, c.saldo "
                + "FROM USUARIO u "
                + "LEFT JOIN CONTA c ON u.id = c.id_usuario "
                + "WHERE u.tipo = 'CLIENTE' ORDER BY nome ASC";

        try (Connection conexao = ConexaoDB.getConexao(); PreparedStatement ps = conexao.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            // O loop percorre cada linha retornada pela consulta no banco
            while (rs.next()) {
                // Para cada linha, cria um novo objeto Cliente
                Cliente cliente = new Cliente(rs.getString("cpf"), rs.getString("senha"), rs.getString("nome"), rs.getString("cpf"));
                cliente.setId(rs.getInt("id"));

                // Preenche todos os outros dados do cliente
                cliente.setEmail(rs.getString("email"));
                java.sql.Date dataNascimentoSql = rs.getDate("data_nascimento");
                if (dataNascimentoSql != null) {
                    cliente.setDataNascimento(dataNascimentoSql.toLocalDate());
                }
                cliente.setTelefone(rs.getString("telefone"));
                cliente.setCep(rs.getString("cep"));
                cliente.setEndereco(rs.getString("endereco"));
                cliente.setBairro(rs.getString("bairro"));
                cliente.setCidade(rs.getString("cidade"));
                cliente.setEstado(rs.getString("estado"));
                cliente.setOcupacao(rs.getString("ocupacao"));
                double renda = rs.getDouble("renda");
                if (!rs.wasNull()) {
                    cliente.setRenda(renda);
                }

                // Se o cliente tiver uma conta associada, cria o objeto Conta também
                if (rs.getString("agencia") != null) {
                    Conta conta = new Conta(rs.getString("agencia"), rs.getString("numero"), rs.getDouble("saldo"));
                    conta.setId(rs.getInt("conta_id"));
                    cliente.setConta(conta);
                }

                // Adiciona o cliente totalmente preenchido à lista
                clientes.add(cliente);
            }
        }
        return clientes;
    }

    public List<Cliente> buscarTodosClientes() throws SQLException {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT u.*, c.id as conta_id, c.agencia, c.numero, c.saldo "
                + "FROM USUARIO u "
                + "LEFT JOIN CONTA c ON u.id = c.id_usuario "
                + "WHERE u.tipo = 'CLIENTE'";

        try (Connection conexao = ConexaoDB.getConexao(); PreparedStatement ps = conexao.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // Para cada linha, cria um novo objeto Cliente
                Cliente cliente = new Cliente(rs.getString("cpf"), rs.getString("senha"), rs.getString("nome"), rs.getString("cpf"));
                cliente.setId(rs.getInt("id"));
                cliente.setEmail(rs.getString("email"));

                // Se houver uma conta bancária associada, cria e adiciona o objeto Conta
                if (rs.getString("agencia") != null) {
                    Conta conta = new Conta(rs.getString("agencia"), rs.getString("numero"), rs.getDouble("saldo"));
                    conta.setId(rs.getInt("conta_id"));
                    cliente.setConta(conta);
                }

                clientes.add(cliente);
            }
        }
        return clientes;
    }

    /**
     * Busca os dados de um cliente a partir do ID da sua conta bancária. Usado
     * na confirmação de transferência para pegar os dados do destinatário.
     *
     * @param idConta O ID da conta (da tabela CONTA).
     * @return Um objeto Cliente completo, ou null se não for encontrado.
     * @throws SQLException
     */
    public Cliente buscarClientePorIdConta(int idConta) throws SQLException {
        String sql = "SELECT u.*, c.id as conta_id, c.agencia, c.numero, c.saldo "
                + "FROM USUARIO u "
                + "JOIN CONTA c ON u.id = c.id_usuario " // JOIN normal para garantir que a conta exista
                + "WHERE c.id = ? AND u.tipo = 'CLIENTE'";

        Cliente cliente = null;
        try (Connection conexao = ConexaoDB.getConexao(); PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setInt(1, idConta);
            ResultSet rs = ps.executeQuery();

            // Se a consulta retornou um resultado, monta o objeto Cliente
            if (rs.next()) {
                // cria o objeto Cliente
                cliente = new Cliente(rs.getString("cpf"), rs.getString("senha"), rs.getString("nome"), rs.getString("cpf"));
                cliente.setId(rs.getInt("id"));

                java.sql.Date dataNascimentoSql = rs.getDate("data_nascimento");
                if (dataNascimentoSql != null) {
                    cliente.setDataNascimento(dataNascimentoSql.toLocalDate());
                }
                cliente.setEmail(rs.getString("email"));
                cliente.setTelefone(rs.getString("telefone"));
                cliente.setCep(rs.getString("cep"));
                cliente.setEndereco(rs.getString("endereco"));
                cliente.setBairro(rs.getString("bairro"));
                cliente.setCidade(rs.getString("cidade"));
                cliente.setEstado(rs.getString("estado"));
                cliente.setOcupacao(rs.getString("ocupacao"));
                double renda = rs.getDouble("renda");
                if (!rs.wasNull()) {
                    cliente.setRenda(renda);
                }
                // Como a busca é pela conta, a conta sempre existirá neste ponto
                Conta conta = new Conta(rs.getString("agencia"), rs.getString("numero"), rs.getDouble("saldo"));
                conta.setId(rs.getInt("conta_id"));
                cliente.setConta(conta);
            }
        }
        return cliente;
    }

    /**
     * Salva ou limpa um token de login persistente ("Lembrar de mim") para um
     * usuário.
     *
     * @param idUsuario O ID do usuário.
     * @param token O token a ser salvo, ou null para limpar/remover o token.
     * @throws SQLException
     */
    public void salvarTokenLembrarMe(int idUsuario, String token) throws SQLException {
        String sql = "UPDATE USUARIO SET token_lembrar_me = ? WHERE id = ?";
        try (Connection conexao = ConexaoDB.getConexao(); PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, token);
            ps.setInt(2, idUsuario);
            ps.executeUpdate();
        }
    }

    /**
     * Busca um usuário no banco a partir de um token de login persistente.
     * Usado pelo filtro para a funcionalidade "Lembrar de mim".
     *
     * @param token O token vindo do cookie do navegador.
     * @return O objeto Usuario correspondente, ou null se não for encontrado.
     * @throws SQLException
     */
    public Usuario buscarUsuarioPorToken(String token) throws SQLException {
        String sql = "SELECT * FROM USUARIO WHERE token_lembrar_me = ?";
        Usuario usuario = null;
        try (Connection conexao = ConexaoDB.getConexao(); PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, token);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                // Reutiliza o método buscarPorCpf para não duplicar a lógica de montar o objeto
                String cpf = rs.getString("cpf");
                usuario = this.buscarPorCpf(cpf);
            }
        }
        return usuario;
    }
}
