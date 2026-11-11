public class ProdutoVestuario extends Produto {

    private String tamanho;
    private boolean emPromocao;
    private double precoPromocional;

    public ProdutoVestuario(int id, String nome, double precoBase, int quantidadeEstoque,
                            boolean ativo, String tamanho, boolean emPromocao, double precoPromocional) {
        super(id, nome, precoBase, quantidadeEstoque, ativo);
        this.tamanho = tamanho;
        this.emPromocao = emPromocao;
        this.precoPromocional = precoPromocional;
    }

    @Override
    public double calcularPreco(int quantidade) {
        double precoUnitario = emPromocao ? precoPromocional : getPrecoBase();
        return quantidade * precoUnitario;
    }

    public void ligarPromocao(double precoPromocional) {
        this.precoPromocional = precoPromocional;
        this.emPromocao = true;
    }

    public void desligarPromocao() {
        emPromocao = false;
    }

    public String getTamanho() {
        return tamanho;
    }

    public boolean isEmPromocao() {
        return emPromocao;
    }

    public double getPrecoPromocional() {
        return precoPromocional;
    }
}