public class ItemVenda {

    private final Produto produto;
    private final int quantidade;
    private final double precoUnitario;
    private final double subtotal;

  
    ItemVenda(Produto produto, int quantidade) {
        validar(produto, quantidade);

      
        if (!produto.temEstoque(quantidade)) {
            throw new IllegalStateException("Estoque insuficiente para \"" + produto.getNome()
                    + "\": pedido " + quantidade
                    + ", disponível " + produto.getQuantidadeEstoque() + ".");
        }

        this.produto = produto;
        this.quantidade = quantidade;
        this.subtotal = produto.calcularPreco(quantidade);
        this.precoUnitario = subtotal / quantidade;
    }

  
    public ItemVenda(Produto produto, int quantidade, double precoUnitario) {
        validar(produto, quantidade);
        if (precoUnitario < 0) {
            throw new IllegalArgumentException("O preço unitário não pode ser negativo.");
        }
        this.produto = produto;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.subtotal = precoUnitario * quantidade;
    }

    private static void validar(Produto produto, int quantidade) {
        if (produto == null) {
            throw new IllegalArgumentException("Produto não informado para o item.");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade do item deve ser maior que zero.");
        }
    }

    public Produto getProduto() {
        return produto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public double getPrecoUnitario() {
        return precoUnitario;
    }

    public double getSubtotal() {
        return subtotal;
    }

    @Override
    public String toString() {
        return String.format("%s x%d = R$ %.2f", produto.getNome(), quantidade, subtotal);
    }
}
