import java.util.ArrayList;
import java.util.List;

public class Estoque {

    private List<Produto> produtos;

    public Estoque(List<Produto> produtos) {
        this.produtos = produtos;
    }

    public void  adicionarProduto(Produto produto){
        produtos.add(produto);
    }

    public List<Produto> buscarPorNome(String nome) {
        List<Produto> encontrados = new ArrayList<>();
        for (Produto produto : produtos) {
            if (produto.getNome() .toLowerCase().contains(nome.toLowerCase())) {
                encontrados.add(produto);
            }
        }
        return encontrados;
    }

    public List<Produto> listarEstoqueBaixo(int limite) {
        List<Produto> baixos = new ArrayList<>();
        for (Produto produto : produtos) {
            if (produto.isAtivo() && produto.getQuantidadeEstoque() <= limite){
                baixos.add(produto);
            }
        }
        return baixos;
    }
}
