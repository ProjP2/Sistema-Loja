package dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CriadorBanco {

    private static final String[] COMANDOS = {
            "CREATE TABLE IF NOT EXISTS cliente ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "nome VARCHAR(100) NOT NULL, "
                    + "cpf VARCHAR(11) UNIQUE, "
                    + "email VARCHAR(100), "
                    + "telefone VARCHAR(20))",

            "CREATE TABLE IF NOT EXISTS produto ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "nome VARCHAR(100) NOT NULL, "
                    + "preco_base DECIMAL(10,2) NOT NULL, "
                    + "quantidade_estoque INT NOT NULL DEFAULT 0, "
                    + "ativo BOOLEAN NOT NULL DEFAULT TRUE, "
                    + "tipo VARCHAR(20) NOT NULL)",

            "CREATE TABLE IF NOT EXISTS produto_perecivel ("
                    + "produto_id INT PRIMARY KEY, "
                    + "data_validade DATE NOT NULL, "
                    + "FOREIGN KEY (produto_id) REFERENCES produto(id) ON DELETE CASCADE)",

            "CREATE TABLE IF NOT EXISTS produto_eletronico ("
                    + "produto_id INT PRIMARY KEY, "
                    + "meses_garantia INT NOT NULL, "
                    + "FOREIGN KEY (produto_id) REFERENCES produto(id) ON DELETE CASCADE)",

            "CREATE TABLE IF NOT EXISTS produto_vestuario ("
                    + "produto_id INT PRIMARY KEY, "
                    + "tamanho VARCHAR(10), "
                    + "em_promocao BOOLEAN NOT NULL DEFAULT FALSE, "
                    + "preco_promocional DECIMAL(10,2), "
                    + "FOREIGN KEY (produto_id) REFERENCES produto(id) ON DELETE CASCADE)",

            "CREATE TABLE IF NOT EXISTS venda ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "cliente_id INT NOT NULL, "
                    + "data DATETIME NOT NULL, "
                    + "finalizada BOOLEAN NOT NULL DEFAULT FALSE, "
                    + "FOREIGN KEY (cliente_id) REFERENCES cliente(id))",

            "CREATE TABLE IF NOT EXISTS item_venda ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "venda_id INT NOT NULL, "
                    + "produto_id INT NOT NULL, "
                    + "quantidade INT NOT NULL, "
                    + "preco_unitario DECIMAL(10,4) NOT NULL, "
                    + "subtotal DECIMAL(10,2) GENERATED ALWAYS AS (quantidade * preco_unitario) STORED, "
                    + "FOREIGN KEY (venda_id) REFERENCES venda(id), "
                    + "FOREIGN KEY (produto_id) REFERENCES produto(id))"
    };

    public static void criarTabelas() throws SQLException {
        try (Connection con = ConexaoBanco.getConexao();
             Statement st = con.createStatement()) {
            for (String sql : COMANDOS) {
                st.executeUpdate(sql);
            }
        }
    }
}
