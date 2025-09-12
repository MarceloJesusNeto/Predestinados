
package Cinemach.cinemach.model;


public class Filme {
    private String titulo;
    private String imagem;
    private String genero;
    private String descricao;
    private String nota;

    public Filme(String titulo, String imagem, String genero, String descricao, String nota) {
        this.titulo = titulo;
        this.imagem = imagem;
        this.genero = genero;
        this.descricao = descricao;
        this.nota = nota;
    }

    public String getTitulo() { return titulo; }
    public String getImagem() { return imagem; }
    public String getGenero() { return genero; }
    public String getDescricao() { return descricao; }
    public String getNota() { return nota; }
}
