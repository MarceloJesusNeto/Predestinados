package Cinemach.cinemach.controller;

import Cinemach.cinemach.model.FilmeSalvo;
import Cinemach.cinemach.model.FotoPerfil;
import Cinemach.cinemach.model.Usuario;
import Cinemach.cinemach.repository.FilmeSalvoRepository;
import Cinemach.cinemach.repository.FotoPerfilRepository;
import Cinemach.cinemach.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/perfil")
public class PerfilController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private FilmeSalvoRepository filmeSalvoRepository;

    @Autowired
    private FotoPerfilRepository fotoPerfilRepository;

    @PostMapping("/salvarFilme")
    @ResponseBody
    public String salvarFilme(
            @RequestParam String imdbId,
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) String imagem,
            @RequestParam(required = false) String genero,
            HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            return "NAO_LOGADO";
        }

        // Valores padrão para evitar nulos
        if (titulo == null || titulo.isBlank()) titulo = "Título não disponível";
        if (imagem == null || imagem.isBlank()) imagem = "/img/placeholder.jpg";
        if (genero == null || genero.isBlank()) genero = "Desconhecido";

        // Verifica se o filme já foi salvo → toggle
        Optional<FilmeSalvo> existente = filmeSalvoRepository.findByUsuarioAndImdbId(usuario, imdbId);
        if (existente.isPresent()) {
            filmeSalvoRepository.delete(existente.get());
            return "REMOVIDO";
        }

        // 🔒 Verifica se já tem 10 filmes salvos
        long total = filmeSalvoRepository.countByUsuario(usuario);
        if (total >= 10) {
            return "LIMITE";
        }

        // Adiciona novo filme
        FilmeSalvo filme = new FilmeSalvo();
        filme.setImdbId(imdbId);
        filme.setTitulo(titulo);
        filme.setImagem(imagem);
        filme.setGenero(genero);
        filme.setUsuario(usuario);

        filmeSalvoRepository.save(filme);

        return "ADICIONADO";
    }

    @GetMapping
    public String visualizarPerfil(HttpSession session,
                                   Model model,
                                   @RequestParam(required = false) String atualizado) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            return "redirect:/login";
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("filmesSalvos", filmeSalvoRepository.findByUsuario(usuario));

        if (atualizado != null) {
            model.addAttribute("msg", "Perfil atualizado com sucesso!");
        }

        return "perfil";
    }

    @DeleteMapping("/removerFilme/{imdbId}")
    @ResponseBody
    public ResponseEntity<String> removerFilme(@PathVariable String imdbId, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("ERRO_LOGIN");
        }

        var existente = filmeSalvoRepository.findByUsuarioAndImdbId(usuario, imdbId);
        if (existente.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("NAO_ENCONTRADO");
        }

        try {
            filmeSalvoRepository.delete(existente.get());
            return ResponseEntity.ok("REMOVIDO");
        } catch (Exception e) {
            e.printStackTrace(); // debug
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("ERRO_SERVIDOR");
        }
    }

    @PostMapping("/atualizar")
    @ResponseBody
    public String atualizarPerfilAjax(@RequestParam String nome,
                                      @RequestParam String email,
                                      @RequestParam String senhaAtual,
                                      @RequestParam(required = false) String novaSenha,
                                      HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return "ERRO_LOGIN";
        }

        if (senhaAtual == null || senhaAtual.isBlank()) {
            return "Informe sua senha atual para atualizar o perfil.";
        }

        if (!passwordEncoder.matches(senhaAtual, usuario.getSenha())) {
            return "Senha atual incorreta.";
        }

        usuario.setNome(nome);
        usuario.setEmail(email);

        if (novaSenha != null && !novaSenha.isBlank()) {
            usuario.setSenha(passwordEncoder.encode(novaSenha));
        }

        usuarioRepository.save(usuario);
        session.setAttribute("usuarioLogado", usuario);

        return "Perfil atualizado com sucesso!";
    }

    @GetMapping("/salvos/ids")
    @ResponseBody
    public List<String> listarIdsSalvos(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            return List.of(); // usuário não logado
        }

        return filmeSalvoRepository.findByUsuario(usuario)
                .stream()
                .map(f -> f.getImdbId())
                .toList();
    }

    @GetMapping("/fotos")
    @ResponseBody
    public List<FotoPerfil> listarFotos() {
        return fotoPerfilRepository.findAll();
    }

    @PostMapping("/atualizarFoto")
    @ResponseBody
    public String atualizarFoto(@RequestParam("idFoto") Long idFoto, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return "ERRO_SESSAO";
        }

        FotoPerfil foto = fotoPerfilRepository.findById(idFoto).orElse(null);
        if (foto == null) {
            return "ERRO_FOTO";
        }

        usuario.setFotoPerfil(foto);
        usuarioRepository.save(usuario);

        // 🔧 Recarrega o usuário completo do banco (garante que nada fica nulo)
        Usuario usuarioAtualizado = usuarioRepository.findById(usuario.getId()).orElse(usuario);

        // Atualiza a sessão com o objeto sincronizado
        session.setAttribute("usuarioLogado", usuarioAtualizado);

        return "OK";
    }
}