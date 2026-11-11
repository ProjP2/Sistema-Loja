public class Cliente {

    private int id;
    private String nome;
    private String cpf;
    private String email;
    private String telefone;

    public Cliente(int id, String nome, String cpf, String email, String telefone) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.telefone = telefone;
    }

    public int getId(){
        return id;
    }
    public String getNome(){
        return nome;
    }
    public String getCpf(){
        return cpf;
    }
    public String geEmail(){
        return email;
    }
    public String getTelefone(){
        return telefone;
    }
}
