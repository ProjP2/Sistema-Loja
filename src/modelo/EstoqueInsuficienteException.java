package modelo;

public class EstoqueInsuficienteException extends RuntimeException {

    public EstoqueInsuficienteException(String mensagem){
        super(mensagem);
    }
}
