package Cinemach.cinemach.model;

import jakarta.persistence.*;

@Entity
public class FilmeFavorito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imdbId;
    private String titulo;
    private String imagem;
    private String genero;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    public FilmeFavorito() {}

    public FilmeFavorito(String imdbId, String titulo, String imagem, String genero, Usuario usuario) {
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

    public void setId(Long id) { this.id = id; }
    public void setImdbId(String imdbId) { this.imdbId = imdbId; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setImagem(String imagem) { this.imagem = imagem; }
    public void setGenero(String genero) { this.genero = genero; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}