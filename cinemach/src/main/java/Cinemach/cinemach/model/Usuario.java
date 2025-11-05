package Cinemach.cinemach.model;

import jakarta.persistence.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Entity
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @Column(unique = true)
    private String email;

    @ManyToOne
    @JoinColumn(name = "foto_perfil_id")
    private FotoPerfil fotoPerfil;

    private String senha;

    @Column(length = 255)
    private String generos;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSenha(String senha) { this.senha = senha;}

    public String getSenha() {
        return senha;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGeneros() {
        return generos;
    }

    public void setGeneros(String generos) {
        this.generos = generos;
    }

    public FotoPerfil getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(FotoPerfil fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }
}