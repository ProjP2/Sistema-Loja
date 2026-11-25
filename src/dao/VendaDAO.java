package dao;

import modelo.Cliente;
import modelo.EstoqueInsuficienteException;
import modelo.ItemVenda;
import modelo.Venda;
import modelo.VendaInvalidaException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class VendaDAO {

    private final ClienteDAO clienteDAO;
    private final ItemVendaDAO itemVendaDAO;

    public VendaDAO() {
        this.clienteDAO = new ClienteDAO();
        this.itemVendaDAO = new ItemVendaDAO();
    }

    public VendaDAO(ClienteDAO clienteDAO, ItemVendaDAO itemVendaDAO) {
        this.clienteDAO = clienteDAO;
        this.itemVendaDAO = itemVendaDAO;
    }

    public Venda inserir(Venda venda) throws SQLException, VendaInvalidaException {
        if (venda == null) {
            throw new IllegalArgumentException("Venda não informada.");
        }
        if (venda.getItens().isEmpty()) {
            throw new VendaInvalidaException("Não é possível registrar uma venda sem itens.");
        }

        String sqlVenda = "INSERT INTO venda (cliente_id, data, finalizada) VALUES (?, ?, ?)";
        Connection con = null;

        try {
            con = ConexaoBanco.getConexao();
            con.setAutoCommit(false);

            int vendaId;
            try (PreparedStatement ps = con.prepareStatement(sqlVenda, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, venda.getCliente().getId());
                ps.setTimestamp(2, Timestamp.valueOf(venda.getData()));
                ps.setBoolean(3, true);
                ps.executeUpdate();

                try (ResultSet chaves = ps.getGeneratedKeys()) {
                    if (!chaves.next()) {
                        throw new SQLException("Falha ao obter o ID gerado para a venda.");
                    }
                    vendaId = chaves.getInt(1);
                }
            }

            for (ItemVenda item : venda.getItens()) {
                itemVendaDAO.inserir(con, vendaId, item);
                baixarEstoqueNoBanco(con, item.getProduto().getId(), item.getQuantidade());
            }

            con.commit();

            if (!venda.isFinalizada()) {
                venda.finalizarVenda();
            }

            return new Venda(vendaId, venda.getCliente(), venda.getData(), true, venda.getItens());

        } catch (Exception e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException rollbackEx) {
                    e.addSuppressed(rollbackEx);
                }
            }
            throw e;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException closeEx) {
                }
            }
        }
    }

    private void baixarEstoqueNoBanco(Connection con, int produtoId, int quantidade) throws SQLException {
        String sql = "UPDATE produto SET quantidade_estoque = quantidade_estoque - ? " +
                     "WHERE id = ? AND quantidade_estoque >= ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, quantidade);
            ps.setInt(2, produtoId);
            ps.setInt(3, quantidade);

            int linhasAfetadas = ps.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new EstoqueInsuficienteException(
                        "Estoque insuficiente no banco para o produto ID " + produtoId + "."
                );
            }
        }
    }

    public Venda buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, cliente_id, data, finalizada FROM venda WHERE id = ?";

        try (Connection con = ConexaoBanco.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? montar(con, rs) : null;
            }
        }
    }

    public List<Venda> listar() throws SQLException {
        String sql = "SELECT id, cliente_id, data, finalizada FROM venda ORDER BY data DESC";
        List<Venda> vendas = new ArrayList<>();

        try (Connection con = ConexaoBanco.getConexao();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                vendas.add(montar(con, rs));
            }
        }
        return vendas;
    }

    public List<Venda> listarPorCliente(int clienteId) throws SQLException {
        String sql = "SELECT id, cliente_id, data, finalizada FROM venda WHERE cliente_id = ? ORDER BY data DESC";
        List<Venda> vendas = new ArrayList<>();

        try (Connection con = ConexaoBanco.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    vendas.add(montar(con, rs));
                }
            }
        }
        return vendas;
    }

    public List<Venda> listarPorPeriodo(LocalDate inicio, LocalDate fim) throws SQLException {
        String sql = "SELECT id, cliente_id, data, finalizada FROM venda WHERE data BETWEEN ? AND ? ORDER BY data DESC";
        List<Venda> vendas = new ArrayList<>();

        try (Connection con = ConexaoBanco.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(inicio.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(fim.atTime(LocalTime.MAX)));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    vendas.add(montar(con, rs));
                }
            }
        }
        return vendas;
    }

    private Venda montar(Connection con, ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int clienteId = rs.getInt("cliente_id");
        LocalDateTime data = rs.getTimestamp("data").toLocalDateTime();
        boolean finalizada = rs.getBoolean("finalizada");

        Cliente cliente = clienteDAO.buscarPorId(clienteId);
        if (cliente == null) {
            throw new SQLException("Cliente ID " + clienteId + " não encontrado para a venda #" + id + ".");
        }

        List<ItemVenda> itens = itemVendaDAO.listarPorVenda(con, id);
        return new Venda(id, cliente, data, finalizada, itens);
    }
}
