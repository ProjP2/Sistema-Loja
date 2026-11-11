import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        Estoque estoque = new Estoque (new ArrayList<>());
        List <Venda> vendas = new ArrayList<>();

        ProdutoEletronico fone = new ProdutoEletronico(1, "Fone Bluetooth", 100.00, 10, true, 12);
        ProdutoPerecivel leite = new ProdutoPerecivel(2, "Leite Integral", 10.00, 20, true, LocalDate.now().plusDays(5));
        ProdutoPerecivel iogurte = new ProdutoPerecivel(3, "Iogurte Natural", 8.00, 5, true, LocalDate.now().minusDays(1));
        ProdutoVestuario camisa = new ProdutoVestuario(4, "Camisa Polo", 80.00, 3, true, "M", false, 0);

        estoque.adicionarProduto(fone);
        estoque.adicionarProduto(leite);
        estoque.adicionarProduto(iogurte);
        estoque.adicionarProduto(camisa);

        Cliente ana = new Cliente(1, "Ana Souza", "111.222.333-44", "ana@email.com", "(81) 91234-5678");

        System.out.println("=== 1. Venda com vários itens ===");
        Venda venda1 = new Venda(1, ana);
        venda1.adicionarItem(fone, 3);
        venda1.adicionarItem(leite, 2);
        venda1.adicionarItem(camisa);
        for (ItemVenda item : venda1.getItens()) {
            System.out.println(item);
        }
        System.out.printf("Total: R$ %.2f%n", venda1.calcularTotal());

        System.out.println("Estoque do fone antes :" + fone.getQuantidadeEstoque());
        venda1.finalizarVenda();
        vendas.add(venda1);
        System.out.println("Estoque do fone depois: " + fone.getQuantidadeEstoque());


    }
}