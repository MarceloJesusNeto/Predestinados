package Cinemach.cinemach.controller;

import Cinemach.cinemach.model.FilmeFavorito;
import Cinemach.cinemach.model.Usuario;
import Cinemach.cinemach.repository.FilmeFavoritoRepository;
import Cinemach.cinemach.repository.UsuarioRepository;
import Cinemach.cinemach.service.CookieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class FavoritoController {

    private final FilmeFavoritoRepository favoritoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CookieService cookieService;

    @Autowired
    public FavoritoController(
            FilmeFavoritoRepository favoritoRepository,
            UsuarioRepository usuarioRepository,
            CookieService cookieService) {
        this.favoritoRepository = favoritoRepository;
        this.usuarioRepository = usuarioRepository;
        this.cookieService = cookieService;
    }

    @PostMapping("/favoritar")
    @Transactional
    @ResponseBody
    public String favoritar(@RequestParam String imdbId,
                            @RequestParam String titulo,
                            @RequestParam String imagem,
                            @RequestParam String genero,
                            HttpServletRequest request) {

        // Obtém ID do usuário logado via cookie
        String usuarioIdStr = cookieService.getCookie(request, "usuarioId");
        if (usuarioIdStr == null) {
            return "ERRO: Usuário não logado";
        }

        // Busca o usuário no banco
        Long usuarioId = Long.parseLong(usuarioIdStr);
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario == null) {
            return "ERRO: Usuário não encontrado";
        }

        // Verifica se o filme já está favoritado
        FilmeFavorito existente = favoritoRepository.findByUsuarioAndImdbId(usuario, imdbId);
        if (existente != null) {
            favoritoRepository.deleteByUsuarioAndImdbId(usuario, imdbId);
            return "REMOVIDO";
        }

        // Caso contrário, adiciona aos favoritos
        FilmeFavorito novo = new FilmeFavorito();
        novo.setUsuario(usuario);
        novo.setImdbId(imdbId);
        novo.setTitulo(titulo);
        novo.setImagem(imagem);
        novo.setGenero(genero);
        favoritoRepository.save(novo);

        return "ADICIONADO";
    }

    @GetMapping("/curtidos")
    public String listarCurtidos(HttpServletRequest request, Model model) {
        String usuarioIdStr = cookieService.getCookie(request, "usuarioId");
        if (usuarioIdStr == null) {
            model.addAttribute("erro", "Faça login para ver seus filmes curtidos.");
            return "login";
        }

        Long usuarioId = Long.parseLong(usuarioIdStr);
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario == null) {
            model.addAttribute("erro", "Usuário não encontrado.");
            return "login";
        }

        List<FilmeFavorito> favoritos = favoritoRepository.findByUsuario(usuario);
        model.addAttribute("filmesCurtidos", favoritos);

        return "curtidos";
    }
}