import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Venda {

    private final int id;
    private final Cliente cliente;
    private final LocalDate data;
    private final List<ItemVenda> itens;
    private boolean finalizada;

    public Venda(int id, Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("A venda precisa de um cliente.");
        }
        this.id = id;
        this.cliente = cliente;
        this.data = LocalDate.now();
        this.itens = new ArrayList<>();
        this.finalizada = false;
    }

    public void adicionarItem(Produto produto, int quantidade) throws EstoqueInsuficienteException {
        if (finalizada) {
            throw new IllegalStateException("A venda já foi finalizada.");
        }
        if (produto == null) {
            throw new IllegalArgumentException("Produto não informado.");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }

        int jaNaVenda = 0;
        for (ItemVenda item : itens) {
            if (item.getProduto() == produto) {
                jaNaVenda += item.getQuantidade();
            }
        }

        int totalPedido = jaNaVenda + quantidade;
        if (!produto.temEstoque(totalPedido)) {
            throw new EstoqueInsuficienteException("Estoque insuficiente para \"" + produto.getNome()
                    + "\": pedido total " + totalPedido
                    + ", disponível " + produto.getQuantidadeEstoque() + ".");
        }

        itens.add(new ItemVenda(produto, quantidade));
    }

    public double calcularTotal() {
        double total = 0;
        for (ItemVenda item : itens) {
            total += item.getSubtotal();
        }
        return total;
    }

    public void finalizarVenda() throws VendaInvalidaException {
        if (finalizada) {
            throw new VendaInvalidaException("Esta venda já foi finalizada.");
        }
        if (itens.isEmpty()) {
            throw new VendaInvalidaException("Não é possível finalizar uma venda sem itens.");
        }
        for (ItemVenda item : itens) {
            item.getProduto().baixarEstoque(item.getQuantidade());
        }
        finalizada = true;
    }

    public List<ItemVenda> getItens() {
        return List.copyOf(itens);
    }

    public int getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public LocalDate getData() {
        return data;
    }

    public boolean isFinalizada() {
        return finalizada;
    }

    @Override
    public String toString() {
        return String.format("Venda #%d - %s - %s - %d item(ns) - Total: R$ %.2f",
                id, data, cliente.getNome(), itens.size(), calcularTotal());
    }
}