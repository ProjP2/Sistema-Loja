package modelo;

public abstract class Produto {

    private int id;
    private String nome;
    private double precoBase;
    private int quantidadeEstoque;
    private boolean ativo;

    public Produto(int id, String nome, double precoBase, int quantidadeEstoque, boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.precoBase = precoBase;
        this.quantidadeEstoque = quantidadeEstoque;
        this.ativo = ativo;
    }

    public abstract double calcularPreco(int quantidade);

    public void baixarEstoque(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero");
        }
        if (quantidade > quantidadeEstoque) {
            throw new EstoqueInsuficienteException("Estoque insuficiente para \"" + nome + "\": pedido " + quantidade + ", disponivel " + quantidadeEstoque + ".");
        }
        quantidadeEstoque -= quantidade;
    }

    public void repor(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
        quantidadeEstoque += quantidade;
    }

    public boolean temEstoque(int quantidade) {
        return quantidadeEstoque >= quantidade;
    }

    public void desativar() {
        ativo = false;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public double getPrecoBase() {
        return precoBase;
    }

    public int getQuantidadeEstoque() {
        return quantidadeEstoque;
    }
}
