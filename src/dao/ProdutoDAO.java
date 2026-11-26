package dao;

import modelo.Produto;
import modelo.ProdutoEletronico;
import modelo.ProdutoPerecivel;
import modelo.ProdutoVestuario;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

    public class ProdutoDAO {

        private static final String SELECT_PRODUTO =
                "SELECT p.id, p.nome, p.preco_base, p.quantidade_estoque, p.ativo, p.tipo, " +
                        "       pp.data_validade, pe.meses_garantia, " +
                        "       pv.tamanho, pv.em_promocao, pv.preco_promocional " +
                        "FROM produto p " +
                        "LEFT JOIN produto_perecivel  pp ON pp.produto_id = p.id " +
                        "LEFT JOIN produto_eletronico pe ON pe.produto_id = p.id " +
                        "LEFT JOIN produto_vestuario  pv ON pv.produto_id = p.id ";

        public int inserir(Produto produto) throws SQLException {
            String sql = "INSERT INTO produto (nome, preco_base, quantidade_estoque, ativo, tipo) " +
                    "VALUES (?, ?, ?, ?, ?)";

            try (Connection con = ConexaoBanco.getConexao()) {
                con.setAutoCommit(false); // as duas inserções valem juntas ou nenhuma vale

                try {
                    int id;
                    try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                        ps.setString(1, produto.getNome());
                        ps.setDouble(2, produto.getPrecoBase());
                        ps.setInt(3, produto.getQuantidadeEstoque());
                        ps.setBoolean(4, produto.isAtivo());
                        ps.setString(5, tipoDe(produto));
                        ps.executeUpdate();

                        try (ResultSet chaves = ps.getGeneratedKeys()) {
                            if (!chaves.next()) {
                                throw new SQLException("Falha ao obter o ID gerado para o produto.");
                            }
                            id = chaves.getInt(1);
                        }
                    }

                    inserirSubtipo(con, id, produto);
                    con.commit();
                    return id;

                } catch (SQLException e) {
                    con.rollback();
                    throw e;
                }
            }
        }

        private String tipoDe(Produto produto) {
            if (produto instanceof ProdutoPerecivel)  return "PERECIVEL";
            if (produto instanceof ProdutoEletronico) return "ELETRONICO";
            if (produto instanceof ProdutoVestuario)  return "VESTUARIO";
            throw new IllegalArgumentException("Tipo de produto desconhecido: " + produto.getClass().getName());
        }

        private void inserirSubtipo(Connection con, int id, Produto produto) throws SQLException {
            if (produto instanceof ProdutoPerecivel p) {
                String sql = "INSERT INTO produto_perecivel (produto_id, data_validade) VALUES (?, ?)";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, id);
                    ps.setDate(2, Date.valueOf(p.getDataValidade())); // LocalDate -> java.sql.Date
                    ps.executeUpdate();
                }
            } else if (produto instanceof ProdutoEletronico e) {
                String sql = "INSERT INTO produto_eletronico (produto_id, meses_garantia) VALUES (?, ?)";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, id);
                    ps.setInt(2, e.getMesesGarantia());
                    ps.executeUpdate();
                }
            } else if (produto instanceof ProdutoVestuario v) {
                String sql = "INSERT INTO produto_vestuario (produto_id, tamanho, em_promocao, preco_promocional) " +
                        "VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, id);
                    ps.setString(2, v.getTamanho());
                    ps.setBoolean(3, v.isEmPromocao());
                    ps.setDouble(4, v.getPrecoPromocional());
                    ps.executeUpdate();
                }
            }
        }

        // ---------- CONSULTAS ----------

        public Produto buscarPorId(int id) throws SQLException {
            String sql = SELECT_PRODUTO + "WHERE p.id = ?";

            try (Connection con = ConexaoBanco.getConexao();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? montar(rs) : null;
                }
            }
        }

        public List<Produto> buscarPorNome(String nome) throws SQLException {
            String sql = SELECT_PRODUTO + "WHERE p.nome LIKE ? ORDER BY p.nome";

            try (Connection con = ConexaoBanco.getConexao();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, "%" + nome + "%");
                return lerLista(ps);
            }
        }

        public List<Produto> listar() throws SQLException {
            String sql = SELECT_PRODUTO + "ORDER BY p.nome";

            try (Connection con = ConexaoBanco.getConexao();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                return lerLista(ps);
            }
        }

        public List<Produto> listarEstoqueBaixo(int limite) throws SQLException {
            String sql = SELECT_PRODUTO + "WHERE p.ativo = TRUE AND p.quantidade_estoque <= ? " +
                    "ORDER BY p.quantidade_estoque";

            try (Connection con = ConexaoBanco.getConexao();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, limite);
                return lerLista(ps);
            }
        }

        private List<Produto> lerLista(PreparedStatement ps) throws SQLException {
            List<Produto> produtos = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    produtos.add(montar(rs));
                }
            }
            return produtos;
        }

        public boolean reporEstoque(int produtoId, int quantidade) throws SQLException {
            String sql = "UPDATE produto SET quantidade_estoque = quantidade_estoque + ? WHERE id = ?";

            try (Connection con = ConexaoBanco.getConexao();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, quantidade);
                ps.setInt(2, produtoId);
                return ps.executeUpdate() > 0;
            }
        }

        public boolean desativar(int produtoId) throws SQLException {
            String sql = "UPDATE produto SET ativo = FALSE WHERE id = ?";

            try (Connection con = ConexaoBanco.getConexao();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, produtoId);
                return ps.executeUpdate() > 0;
            }
        }

        public boolean remover(int produtoId) throws SQLException {
            try (Connection con = ConexaoBanco.getConexao()) {
                con.setAutoCommit(false);

                try {
                    for (String tabela : new String[]{"produto_perecivel", "produto_eletronico", "produto_vestuario"}) {
                        try (PreparedStatement ps = con.prepareStatement(
                                "DELETE FROM " + tabela + " WHERE produto_id = ?")) {
                            ps.setInt(1, produtoId);
                            ps.executeUpdate();
                        }
                    }

                    int removidos;
                    try (PreparedStatement ps = con.prepareStatement("DELETE FROM produto WHERE id = ?")) {
                        ps.setInt(1, produtoId);
                        removidos = ps.executeUpdate();
                    }

                    con.commit();
                    return removidos > 0;

                } catch (SQLException e) {
                    con.rollback();
                    throw e;
                }
            } catch (SQLIntegrityConstraintViolationException e) {
                throw new IllegalArgumentException(
                        "Este produto tem vendas registradas e não pode ser removido. Use desativar().");
            }
        }


        private Produto montar(ResultSet rs) throws SQLException {
            int id = rs.getInt("id");
            String nome = rs.getString("nome");
            double precoBase = rs.getDouble("preco_base");
            int estoque = rs.getInt("quantidade_estoque");
            boolean ativo = rs.getBoolean("ativo");

            switch (rs.getString("tipo")) {
                case "PERECIVEL":
                    return new ProdutoPerecivel(id, nome, precoBase, estoque, ativo,
                            rs.getDate("data_validade").toLocalDate());
                case "ELETRONICO":
                    return new ProdutoEletronico(id, nome, precoBase, estoque, ativo,
                            rs.getInt("meses_garantia"));
                case "VESTUARIO":
                    return new ProdutoVestuario(id, nome, precoBase, estoque, ativo,
                            rs.getString("tamanho"), rs.getBoolean("em_promocao"),
                            rs.getDouble("preco_promocional"));
                default:
                    throw new SQLException("Tipo de produto desconhecido no banco: " + rs.getString("tipo"));
            }
        }
    }
