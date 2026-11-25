package app;

import dao.ClienteDAO;
import modelo.Cliente;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class Menu {

    private final Scanner scanner = new Scanner(System.in);
    private final ClienteDAO clienteDAO = new ClienteDAO();

    public void iniciar() {
        int opcao;
        do {
            System.out.println("\n=== MENU PRINCIPAL ===");
            System.out.println("1 - Produtos");
            System.out.println("2 - Clientes");
            System.out.println("3 - Nova venda");
            System.out.println("4 - Consultas");
            System.out.println("5 - Sair");
            opcao = lerInt("Opção: ");

            switch (opcao) {
                case 1 -> emDesenvolvimento("Produtos");
                case 2 -> menuClientes();
                case 3 -> emDesenvolvimento("Nova venda");
                case 4 -> emDesenvolvimento("Consultas");
                case 5 -> System.out.println("Programa encerrado.");
                default -> System.out.println("Opção inválida.");
            }
        } while (opcao != 5);
    }

    // ---------- Clientes ----------

    private void menuClientes() {
        int opcao;
        do {
            System.out.println("\n--- CLIENTES ---");
            System.out.println("1 - Cadastrar cliente");
            System.out.println("2 - Listar clientes");
            System.out.println("3 - Editar cliente");
            System.out.println("4 - Remover cliente");
            System.out.println("0 - Voltar");
            opcao = lerInt("Opção: ");

            try {
                switch (opcao) {
                    case 1 -> cadastrarCliente();
                    case 2 -> listarClientes();
                    case 3 -> editarCliente();
                    case 4 -> removerCliente();
                    case 0 -> { }
                    default -> System.out.println("Opção inválida.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Erro: " + e.getMessage());
            } catch (SQLException e) {
                System.out.println("Erro no banco de dados: " + e.getMessage());
            }
        } while (opcao != 0);
    }

    private void cadastrarCliente() throws SQLException {
        String nome = lerTexto("Nome: ");
        String cpf = lerCpf("CPF (11 dígitos): ");
        String email = lerTexto("E-mail: ");
        String telefone = lerTexto("Telefone: ");

        Cliente salvo = clienteDAO.inserir(new Cliente(0, nome, cpf, email, telefone));
        System.out.println("Cliente cadastrado com o id " + salvo.getId() + ".");
    }

    private void listarClientes() throws SQLException {
        List<Cliente> clientes = clienteDAO.listar();
        if (clientes.isEmpty()) {
            System.out.println("Nenhum cliente cadastrado.");
            return;
        }
        for (Cliente c : clientes) {
            System.out.println(c.getId() + " - " + c + " - " + c.getEmail() + " - " + c.getTelefone());
        }
    }

    private void editarCliente() throws SQLException {
        Cliente atual = clienteDAO.buscarPorId(lerInt("Id do cliente: "));
        if (atual == null) {
            System.out.println("Cliente não encontrado.");
            return;
        }

        System.out.println("Editando " + atual + " (deixe em branco para manter o valor atual)");
        String nome = lerTextoOuManter("Nome [" + atual.getNome() + "]: ", atual.getNome());
        String cpf = atual.getCpf();
        String novoCpf = lerTexto("CPF [" + atual.getCpf() + "]: ");
        if (!novoCpf.isBlank()) {
            cpf = validarCpf(novoCpf);
        }
        String email = lerTextoOuManter("E-mail [" + atual.getEmail() + "]: ", atual.getEmail());
        String telefone = lerTextoOuManter("Telefone [" + atual.getTelefone() + "]: ", atual.getTelefone());

        clienteDAO.atualizar(new Cliente(atual.getId(), nome, cpf, email, telefone));
        System.out.println("Cliente atualizado.");
    }

    private void removerCliente() throws SQLException {
        int id = lerInt("Id do cliente: ");
        Cliente cliente = clienteDAO.buscarPorId(id);
        if (cliente == null) {
            System.out.println("Cliente não encontrado.");
            return;
        }
        String resposta = lerTexto("Remover " + cliente + "? (s/n): ");
        if (resposta.equalsIgnoreCase("s")) {
            clienteDAO.remover(id);
            System.out.println("Cliente removido.");
        } else {
            System.out.println("Remoção cancelada.");
        }
    }

    // ---------- Leitura de dados ----------

    private void emDesenvolvimento(String nome) {
        System.out.println(nome + ": ainda não disponível.");
    }

    private String lerTexto(String mensagem) {
        System.out.print(mensagem);
        return scanner.nextLine().trim();
    }

    private String lerTextoOuManter(String mensagem, String valorAtual) {
        String texto = lerTexto(mensagem);
        return texto.isBlank() ? valorAtual : texto;
    }

    // Devolve -1 se o texto não for um número, o que cai em "opção inválida"
    // ou "cliente não encontrado" nos chamadores.
    private int lerInt(String mensagem) {
        try {
            return Integer.parseInt(lerTexto(mensagem));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String lerCpf(String mensagem) {
        return validarCpf(lerTexto(mensagem));
    }

    // Aceita com ou sem pontuação e guarda só os 11 dígitos (coluna VARCHAR(11)).
    private String validarCpf(String texto) {
        String digitos = texto.replaceAll("\\D", "");
        if (digitos.length() != 11) {
            throw new IllegalArgumentException("O CPF deve ter 11 dígitos.");
        }
        return digitos;
    }
}
