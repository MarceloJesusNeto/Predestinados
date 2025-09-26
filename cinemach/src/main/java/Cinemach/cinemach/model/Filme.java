package Cinemach.cinemach.model;

public class Filme {
    private String titulo;
    private String imagem;
    private String genero;
    private String descricao;
    private String nota;
    private String imdbId;

    public Filme(String titulo, String imagem, String genero, String descricao, String nota, String imdbId) {
        this.titulo = titulo;
        this.imagem = imagem;
        this.genero = genero != null ? genero : "Desconhecido";
        this.descricao = descricao;
        this.nota = nota != null ? nota : "N/A";
        this.imdbId = imdbId;
    }

    public String getTitulo() { return titulo; }
    public String getImagem() { return imagem; }
    public String getGenero() { return genero; }
    public String getDescricao() { return descricao; }
    public String getNota() { return nota; }
    public String getImdbId() { return imdbId; }
}
