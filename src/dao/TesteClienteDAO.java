package dao;

import modelo.Cliente;

import java.sql.SQLException;

public class TesteClienteDAO {
    public static void main(String[] args) {
        ClienteDAO dao = new ClienteDAO();

        try {
            String cpf = String.valueOf(System.currentTimeMillis() % 100000000000L);
            Cliente novo = dao.inserir(new Cliente(0, "Cliente Teste", cpf, "teste@email.com", "81999999999"));
            System.out.println("Inserido: id " + novo.getId() + " - " + novo);

            Cliente achado = dao.buscarPorCpf(cpf);
            System.out.println("Buscado por CPF: " + achado);

            dao.atualizar(new Cliente(novo.getId(), "Cliente Editado", cpf, "novo@email.com", "81888888888"));
            System.out.println("Depois de editar: " + dao.buscarPorId(novo.getId()));

            try {
                dao.inserir(new Cliente(0, "Outro", cpf, null, null));
            } catch (IllegalArgumentException e) {
                System.out.println("CPF repetido recusado: " + e.getMessage());
            }

            System.out.println("Todos os clientes:");
            dao.listar().forEach(c -> System.out.println("  " + c.getId() + " - " + c));
        } catch (SQLException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }
}
