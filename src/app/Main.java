package app;

import dao.CriadorBanco;

import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {
        try {
            CriadorBanco.criarTabelas();
        } catch (SQLException e) {
            System.out.println("Não foi possível preparar o banco de dados: " + e.getMessage());
            System.out.println("Verifique se o MySQL está rodando em localhost:3306.");
            return;
        }
        new Menu().iniciar();
    }
}
