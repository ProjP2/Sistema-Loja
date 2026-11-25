package dao;

import modelo.ItemVenda;
import modelo.Produto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ItemVendaDAO {

    private final ProdutoDAO produtoDAO;

    public ItemVendaDAO() {
        this.produtoDAO = new ProdutoDAO();
    }

    public ItemVendaDAO(ProdutoDAO produtoDAO) {
        this.produtoDAO = produtoDAO;
    }

    public void inserir(Connection con, int vendaId, ItemVenda item) throws SQLException {
        String sql = "INSERT INTO item_venda (venda_id, produto_id, quantidade, preco_unitario) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, vendaId);
            ps.setInt(2, item.getProduto().getId());
            ps.setInt(3, item.getQuantidade());
            ps.setDouble(4, item.getPrecoUnitario());
            ps.executeUpdate();
        }
    }

    public List<ItemVenda> listarPorVenda(int vendaId) throws SQLException {
        try (Connection con = ConexaoBanco.getConexao()) {
            return listarPorVenda(con, vendaId);
        }
    }

    public List<ItemVenda> listarPorVenda(Connection con, int vendaId) throws SQLException {
        String sql = "SELECT id, venda_id, produto_id, quantidade, preco_unitario FROM item_venda WHERE venda_id = ?";
        List<ItemVenda> itens = new ArrayList<>();

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, vendaId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int produtoId = rs.getInt("produto_id");
                    int quantidade = rs.getInt("quantidade");
                    double precoUnitario = rs.getDouble("preco_unitario");

                    Produto produto = produtoDAO.buscarPorId(produtoId);
                    if (produto == null) {
                        throw new SQLException("Produto ID " + produtoId + " não encontrado ao carregar item da venda.");
                    }

                    itens.add(new ItemVenda(produto, quantidade, precoUnitario));
                }
            }
        }
        return itens;
    }

    public boolean produtoPossuiVendas(int produtoId) throws SQLException {
        String sql = "SELECT 1 FROM item_venda WHERE produto_id = ? LIMIT 1";

        try (Connection con = ConexaoBanco.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, produtoId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
