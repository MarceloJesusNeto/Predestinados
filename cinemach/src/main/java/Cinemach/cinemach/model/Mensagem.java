package Cinemach.cinemach.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Mensagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Usuario remetente;

    @ManyToOne
    private Usuario destinatario;

    @Column(length = 1000)
    private String conteudo;

    private LocalDateTime dataEnvio;

    @ManyToOne
    private Solicitacao solicitacao;

    private boolean lida = false;

    public Mensagem() {
        this.dataEnvio = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getRemetente() { return remetente; }
    public void setRemetente(Usuario remetente) { this.remetente = remetente; }

    public Usuario getDestinatario() { return destinatario; }
    public void setDestinatario(Usuario destinatario) { this.destinatario = destinatario; }

    public String getConteudo() { return conteudo; }
    public void setConteudo(String conteudo) { this.conteudo = conteudo; }

    public LocalDateTime getDataEnvio() { return dataEnvio; }
    public void setDataEnvio(LocalDateTime dataEnvio) { this.dataEnvio = dataEnvio; }

    public Solicitacao getSolicitacao() { return solicitacao; }
    public void setSolicitacao(Solicitacao solicitacao) { this.solicitacao = solicitacao; }

    public boolean isLida() { return lida; }
    public void setLida(boolean lida) { this.lida = lida; }
}
