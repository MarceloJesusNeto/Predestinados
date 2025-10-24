package Cinemach.cinemach.model;

import jakarta.persistence.*;

@Entity
public class FilmeSalvo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imdbId;
    private String titulo;
    private String imagem;
    private String genero;

    @ManyToOne
    private Usuario usuario;

    public FilmeSalvo() {}

    public FilmeSalvo(String imdbId, String titulo, String imagem, String genero, Usuario usuario) {
        this.imdbId = imdbId;
        this.titulo = titulo;
        this.imagem = imagem;
        this.genero = genero;
        this.usuario = usuario;
    }

    public Long getId() { return id; }
    public String getImdbId() { return imdbId; }
    public String getTitulo() { return titulo; }
    public String getImagem() { return imagem; }
    public String getGenero() { return genero; }
    public Usuario getUsuario() { return usuario; }

    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}