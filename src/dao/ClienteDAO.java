package dao;

import modelo.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    public Cliente inserir(Cliente cliente) throws SQLException {
        String sql = "INSERT INTO cliente (nome, cpf, email, telefone) VALUES (?, ?, ?, ?)";

        try (Connection con = ConexaoBanco.getConexao();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, cliente.getNome());
            ps.setString(2, cliente.getCpf());
            ps.setString(3, cliente.getEmail());
            ps.setString(4, cliente.getTelefone());
            ps.executeUpdate();

            try (ResultSet chaves = ps.getGeneratedKeys()) {
                chaves.next();
                return new Cliente(chaves.getInt(1), cliente.getNome(), cliente.getCpf(),
                        cliente.getEmail(), cliente.getTelefone());
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalArgumentException("Já existe um cliente com o CPF " + cliente.getCpf() + ".");
        }
    }

    public boolean atualizar(Cliente cliente) throws SQLException {
        String sql = "UPDATE cliente SET nome = ?, cpf = ?, email = ?, telefone = ? WHERE id = ?";

        try (Connection con = ConexaoBanco.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, cliente.getNome());
            ps.setString(2, cliente.getCpf());
            ps.setString(3, cliente.getEmail());
            ps.setString(4, cliente.getTelefone());
            ps.setInt(5, cliente.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalArgumentException("Já existe outro cliente com o CPF " + cliente.getCpf() + ".");
        }
    }

    public boolean remover(int id) throws SQLException {
        String sql = "DELETE FROM cliente WHERE id = ?";

        try (Connection con = ConexaoBanco.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalArgumentException("Este cliente tem vendas registradas e não pode ser removido.");
        }
    }

    public Cliente buscarPorId(int id) throws SQLException {
        return buscarUm("SELECT id, nome, cpf, email, telefone FROM cliente WHERE id = ?", id);
    }

    public Cliente buscarPorCpf(String cpf) throws SQLException {
        return buscarUm("SELECT id, nome, cpf, email, telefone FROM cliente WHERE cpf = ?", cpf);
    }

    public List<Cliente> listar() throws SQLException {
        String sql = "SELECT id, nome, cpf, email, telefone FROM cliente ORDER BY nome";
        List<Cliente> clientes = new ArrayList<>();

        try (Connection con = ConexaoBanco.getConexao();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                clientes.add(montar(rs));
            }
        }
        return clientes;
    }

    private Cliente buscarUm(String sql, Object parametro) throws SQLException {
        try (Connection con = ConexaoBanco.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setObject(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? montar(rs) : null;
            }
        }
    }

    private Cliente montar(ResultSet rs) throws SQLException {
        return new Cliente(rs.getInt("id"), rs.getString("nome"), rs.getString("cpf"),
                rs.getString("email"), rs.getString("telefone"));
    }
}
