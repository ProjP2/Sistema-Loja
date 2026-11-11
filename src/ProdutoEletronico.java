import java.time.LocalDate;

public class ProdutoEletronico extends Produto{
    private int mesesGarantia;

    public ProdutoEletronico(int id, String nome, double precoBase, int quantidadeEstoque, boolean ativo, int mesesGarantia) {
        super(id, nome, precoBase, quantidadeEstoque, ativo);
        this.mesesGarantia = mesesGarantia;
    }

    @Override
    public double calcularPreco(int quantidade) {
        return (quantidade * getPrecoBase());
    }

    public int getMesesGarantia(){
        return mesesGarantia;
    }
}