package app;

import dao.ClienteDAO;
import dao.ItemVendaDAO;
import dao.ProdutoDAO;
import dao.VendaDAO;
import modelo.Cliente;
import modelo.EstoqueInsuficienteException;
import modelo.ItemVenda;
import modelo.Produto;
import modelo.ProdutoEletronico;
import modelo.ProdutoNaoEncontradoException;
import modelo.ProdutoPerecivel;
import modelo.ProdutoVencidoException;
import modelo.ProdutoVestuario;
import modelo.Venda;
import modelo.VendaInvalidaException;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Menu {

    private final Scanner scanner = new Scanner(System.in);
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final ProdutoDAO produtoDAO = new ProdutoDAO();
    private final ItemVendaDAO itemVendaDAO = new ItemVendaDAO();
    private final VendaDAO vendaDAO = new VendaDAO();

    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

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
                case 1 -> menuProdutos();
                case 2 -> menuClientes();
                case 3 -> novaVenda();
                case 4 -> emDesenvolvimento("Consultas");
                case 5 -> System.out.println("Programa encerrado.");
                default -> System.out.println("Opção inválida.");
            }
        } while (opcao != 5);
    }

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

    private void menuProdutos() {
        int opcao;
        do {
            System.out.println("\n--- PRODUTOS ---");
            System.out.println("1 - Cadastrar produto");
            System.out.println("2 - Listar produtos");
            System.out.println("3 - Editar produto");
            System.out.println("4 - Remover produto");
            System.out.println("0 - Voltar");
            opcao = lerInt("Opção: ");

            try {
                switch (opcao) {
                    case 1 -> cadastrarProduto();
                    case 2 -> listarProdutos();
                    case 3 -> editarProduto();
                    case 4 -> removerProduto();
                    case 0 -> { }
                    default -> System.out.println("Opção inválida.");
                }
            } catch (IllegalArgumentException | ProdutoNaoEncontradoException e) {
                System.out.println("Erro: " + e.getMessage());
            } catch (SQLException e) {
                System.out.println("Erro no banco de dados: " + e.getMessage());
            }
        } while (opcao != 0);
    }

    private void cadastrarProduto() throws SQLException {
        System.out.println("Tipo: 1 - Perecível  2 - Eletrônico  3 - Vestuário");
        int tipo = lerInt("Tipo: ");
        if (tipo < 1 || tipo > 3) {
            System.out.println("Tipo inválido.");
            return;
        }

        String nome = validarTexto(lerTexto("Nome: "), "nome", 100);
        double preco = validarPreco(lerTexto("Preço base: "));
        int estoque = validarNaoNegativo(lerTexto("Quantidade em estoque: "), "A quantidade");

        Produto produto = switch (tipo) {
            case 1 -> new ProdutoPerecivel(0, nome, preco, estoque, true,
                    validarData(lerTexto("Data de validade (dd/mm/aaaa): ")));
            case 2 -> new ProdutoEletronico(0, nome, preco, estoque, true,
                    validarNaoNegativo(lerTexto("Meses de garantia: "), "A garantia"));
            default -> {
                String tamanho = validarTexto(lerTexto("Tamanho: "), "tamanho", 10);
                boolean promocao = confirmar("Está em promoção? (s/n): ");
                double precoPromocional = promocao ? validarPreco(lerTexto("Preço promocional: ")) : 0;
                yield new ProdutoVestuario(0, nome, preco, estoque, true, tamanho, promocao, precoPromocional);
            }
        };

        int id = produtoDAO.inserir(produto);
        System.out.println("Produto cadastrado com o id " + id + ".");
    }

    private void listarProdutos() throws SQLException {
        List<Produto> produtos = produtoDAO.listar();
        if (produtos.isEmpty()) {
            System.out.println("Nenhum produto cadastrado.");
            return;
        }
        for (Produto p : produtos) {
            System.out.println(descrever(p));
        }
    }

    private void editarProduto() throws SQLException {
        Produto atual = buscarProduto(lerInt("Id do produto: "));

        System.out.println("Editando " + atual.getNome() + " (deixe em branco para manter o valor atual)");
        String nome = validarTexto(
                lerTextoOuManter("Nome [" + atual.getNome() + "]: ", atual.getNome()), "nome", 100);
        double preco = validarPreco(lerTextoOuManter(
                String.format("Preço base [%.2f]: ", atual.getPrecoBase()),
                String.valueOf(atual.getPrecoBase())));
        int estoque = validarNaoNegativo(lerTextoOuManter(
                "Quantidade em estoque [" + atual.getQuantidadeEstoque() + "]: ",
                String.valueOf(atual.getQuantidadeEstoque())), "A quantidade");
        boolean ativo = atual.isAtivo() || confirmar("Produto desativado. Reativar? (s/n): ");

        Produto editado;
        if (atual instanceof ProdutoPerecivel p) {
            String validadeAtual = p.getDataValidade().format(FORMATO_DATA);
            LocalDate validade = validarData(lerTextoOuManter(
                    "Data de validade [" + validadeAtual + "]: ", validadeAtual));
            editado = new ProdutoPerecivel(p.getId(), nome, preco, estoque, ativo, validade);
        } else if (atual instanceof ProdutoEletronico e) {
            int garantia = validarNaoNegativo(lerTextoOuManter(
                    "Meses de garantia [" + e.getMesesGarantia() + "]: ",
                    String.valueOf(e.getMesesGarantia())), "A garantia");
            editado = new ProdutoEletronico(e.getId(), nome, preco, estoque, ativo, garantia);
        } else {
            ProdutoVestuario v = (ProdutoVestuario) atual;
            String tamanho = validarTexto(
                    lerTextoOuManter("Tamanho [" + v.getTamanho() + "]: ", v.getTamanho()), "tamanho", 10);
            String resposta = lerTextoOuManter(
                    "Em promoção? (s/n) [" + (v.isEmPromocao() ? "s" : "n") + "]: ",
                    v.isEmPromocao() ? "s" : "n");
            boolean promocao = resposta.equalsIgnoreCase("s");
            double precoPromocional = v.getPrecoPromocional();
            if (promocao) {
                precoPromocional = validarPreco(lerTextoOuManter(
                        String.format("Preço promocional [%.2f]: ", precoPromocional),
                        String.valueOf(precoPromocional)));
            }
            editado = new ProdutoVestuario(v.getId(), nome, preco, estoque, ativo,
                    tamanho, promocao, precoPromocional);
        }

        if (produtoDAO.atualizar(editado)) {
            System.out.println("Produto atualizado.");
        } else {
            throw new ProdutoNaoEncontradoException("Produto não encontrado.");
        }
    }

    private void removerProduto() throws SQLException {
        Produto produto = buscarProduto(lerInt("Id do produto: "));
        boolean vendido = itemVendaDAO.produtoPossuiVendas(produto.getId());

        String pergunta = vendido
                ? produto.getNome() + " já foi vendido e só pode ser desativado. Desativar? (s/n): "
                : "Remover " + produto.getNome() + "? (s/n): ";
        if (!confirmar(pergunta)) {
            System.out.println("Operação cancelada.");
            return;
        }

        if (vendido) {
            produtoDAO.desativar(produto.getId());
            System.out.println("Produto desativado.");
        } else {
            produtoDAO.remover(produto.getId());
            System.out.println("Produto removido.");
        }
    }

    private Produto buscarProduto(int id) throws SQLException {
        Produto produto = produtoDAO.buscarPorId(id);
        if (produto == null) {
            throw new ProdutoNaoEncontradoException("Produto não encontrado.");
        }
        return produto;
    }

    private String descrever(Produto p) {
        String detalhe;
        if (p instanceof ProdutoPerecivel pp) {
            detalhe = "Perecível, validade " + pp.getDataValidade().format(FORMATO_DATA);
        } else if (p instanceof ProdutoEletronico e) {
            detalhe = "Eletrônico, garantia de " + e.getMesesGarantia() + " meses";
        } else {
            ProdutoVestuario v = (ProdutoVestuario) p;
            detalhe = "Vestuário, tamanho " + v.getTamanho();
            if (v.isEmPromocao()) {
                detalhe += String.format(", em promoção por R$ %.2f", v.getPrecoPromocional());
            }
        }
        return String.format("%d - %s - R$ %.2f - estoque %d - %s%s",
                p.getId(), p.getNome(), p.getPrecoBase(), p.getQuantidadeEstoque(), detalhe,
                p.isAtivo() ? "" : " [DESATIVADO]");
    }

    private void novaVenda() {
        System.out.println("\n--- NOVA VENDA ---");
        try {
            Cliente cliente = clienteDAO.buscarPorCpf(lerCpf("CPF do cliente: "));
            if (cliente == null) {
                System.out.println("Cliente não encontrado. Cadastre-o no menu Clientes.");
                return;
            }
            System.out.println("Cliente: " + cliente.getNome());

            Venda venda = new Venda(0, cliente);
            adicionarItens(venda);

            if (venda.getItens().isEmpty()) {
                System.out.println("Venda cancelada: nenhum item.");
                return;
            }

            System.out.println("\nResumo da venda:");
            for (ItemVenda item : venda.getItens()) {
                System.out.println(item);
            }
            System.out.printf("Total: R$ %.2f%n", venda.calcularTotal());

            if (!confirmar("Confirmar a venda? (s/n): ")) {
                System.out.println("Venda cancelada.");
                return;
            }

            Venda salva = vendaDAO.inserir(venda);
            System.out.println("Venda #" + salva.getId() + " registrada.");
        } catch (IllegalArgumentException | EstoqueInsuficienteException | VendaInvalidaException e) {
            System.out.println("Erro: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Erro no banco de dados: " + e.getMessage());
        }
    }

    private void adicionarItens(Venda venda) throws SQLException {
        Map<Integer, Produto> escolhidos = new HashMap<>();

        while (true) {
            int id = lerInt("\nId do produto (0 para encerrar): ");
            if (id == 0) {
                return;
            }
            try {
                Produto produto = escolhidos.get(id);
                if (produto == null) {
                    produto = buscarProduto(id);
                    escolhidos.put(id, produto);
                }
                if (!produto.isAtivo()) {
                    System.out.println("Este produto está desativado e não pode ser vendido.");
                    continue;
                }
                System.out.println(descrever(produto));
                venda.adicionarItem(produto, lerInt("Quantidade: "));
                System.out.printf("Item adicionado. Total parcial: R$ %.2f%n", venda.calcularTotal());
            } catch (ProdutoNaoEncontradoException | ProdutoVencidoException
                     | EstoqueInsuficienteException | VendaInvalidaException e) {
                System.out.println("Erro: " + e.getMessage());
            }
        }
    }

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

    private int lerInt(String mensagem) {
        try {
            return Integer.parseInt(lerTexto(mensagem));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private boolean confirmar(String mensagem) {
        return lerTexto(mensagem).equalsIgnoreCase("s");
    }

    private String validarTexto(String texto, String campo, int maximo) {
        if (texto.isBlank() || texto.length() > maximo) {
            throw new IllegalArgumentException(
                    "O campo " + campo + " é obrigatório e aceita até " + maximo + " caracteres.");
        }
        return texto;
    }

    private double validarPreco(String texto) {
        try {
            double preco = Math.round(Double.parseDouble(texto.replace(',', '.')) * 100) / 100.0;
            if (preco > 0 && preco < 100_000_000) {
                return preco;
            }
        } catch (NumberFormatException e) {
        }
        throw new IllegalArgumentException("Preço inválido. Use um valor maior que zero, como 19,90.");
    }

    private int validarNaoNegativo(String texto, String campo) {
        try {
            int valor = Integer.parseInt(texto);
            if (valor >= 0) {
                return valor;
            }
        } catch (NumberFormatException e) {
        }
        throw new IllegalArgumentException(campo + " deve ser um número inteiro, zero ou maior.");
    }

    private LocalDate validarData(String texto) {
        try {
            return LocalDate.parse(texto, FORMATO_DATA);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Data inválida. Use o formato dd/mm/aaaa.");
        }
    }

    private String lerCpf(String mensagem) {
        return validarCpf(lerTexto(mensagem));
    }

    private String validarCpf(String texto) {
        String digitos = texto.replaceAll("\\D", "");
        if (digitos.length() != 11) {
            throw new IllegalArgumentException("O CPF deve ter 11 dígitos.");
        }
        return digitos;
    }
}
