package Cinemach.cinemach.controller;

import Cinemach.cinemach.model.FilmeSalvo;
import Cinemach.cinemach.model.Usuario;
import Cinemach.cinemach.repository.FilmeSalvoRepository;
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

import java.util.Optional;

@Controller
@RequestMapping("/perfil")
public class PerfilController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private FilmeSalvoRepository filmeSalvoRepository;

    @PostMapping("/salvarFilme")
    @ResponseBody
    public String salvarFilme(@RequestParam String imdbId,
                              @RequestParam String titulo,
                              @RequestParam String imagem,
                              @RequestParam String genero,
                              HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return "ERRO_LOGIN";
        }

        long total = filmeSalvoRepository.countByUsuario(usuario);
        if (total >= 10) {
            return "LIMITE";
        }

        var existente = filmeSalvoRepository.findByUsuarioAndImdbId(usuario, imdbId);
        if (existente.isPresent()) {
            filmeSalvoRepository.delete(existente.get());
            return "REMOVIDO";
        }

        FilmeSalvo filme = new FilmeSalvo(imdbId, titulo, imagem, genero, usuario);
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

}