package Cinemach.cinemach.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class SugestaoBloqueada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Usuario usuario;

    @ManyToOne
    private Usuario bloqueado;

    private LocalDateTime expiracao;

    public SugestaoBloqueada() {}

    public SugestaoBloqueada(Usuario usuario, Usuario bloqueado, LocalDateTime expiracao) {
        this.usuario = usuario;
        this.bloqueado = bloqueado;
        this.expiracao = expiracao;
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public Usuario getBloqueado() { return bloqueado; }
    public LocalDateTime getExpiracao() { return expiracao; }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public void setBloqueado(Usuario bloqueado) {
        this.bloqueado = bloqueado;
    }

    public void setExpiracao(LocalDateTime expiracao) {
        this.expiracao = expiracao;
    }
}
