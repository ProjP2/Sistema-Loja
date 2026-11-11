import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ProdutoPerecivel extends Produto{
    private LocalDate dataValidade;

    public ProdutoPerecivel(int id, String nome, double precoBase, int quantidadeEstoque, boolean ativo, LocalDate dataValidade) {
        super(id, nome, precoBase, quantidadeEstoque, ativo);
        this.dataValidade = dataValidade;
    }

    @Override
    public double calcularPreco(int quantidade) {
        if (estaVencido()) {
            throw new IllegalStateException("Produto vencido não pode ser vendido.");
        }

        if (diasParaVencer() <= 7){
            return quantidade * getPrecoBase() * 0.80;
        }else{
            return quantidade * getPrecoBase();
        }
    }

    public boolean estaVencido(){
        return dataValidade.isBefore(LocalDate.now());
    }

    public long diasParaVencer(){
        return ChronoUnit.DAYS.between(LocalDate.now(), dataValidade);
    }

    public LocalDate getDataValidade(){
        return dataValidade;
    }
}