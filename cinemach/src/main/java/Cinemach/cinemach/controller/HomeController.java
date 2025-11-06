package Cinemach.cinemach.controller;

import Cinemach.cinemach.model.Filme;
import Cinemach.cinemach.model.FotoPerfil;
import Cinemach.cinemach.model.Usuario;
import Cinemach.cinemach.repository.FotoPerfilRepository;
import Cinemach.cinemach.repository.UsuarioRepository;
import Cinemach.cinemach.service.ImdbService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final ImdbService imdbService;
    private final UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private FotoPerfilRepository fotoPerfilRepository;

    public HomeController(ImdbService imdbService, UsuarioRepository usuarioRepository) {
        this.imdbService = imdbService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("erro", "Email ou senha inválidos!");
        }
        return "login";
    }

    @GetMapping("/registrar")
    public String registro() {
        return "cadastro";
    }

    @PostMapping("/registrar")
    public String registrar(@ModelAttribute Usuario usuario, Model model) {
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));

        FotoPerfil fotoPadrao = fotoPerfilRepository.findAll().stream()
                .filter(f -> f.getNome().equalsIgnoreCase("Foto 1"))
                .findFirst()
                .orElse(null);

        if (fotoPadrao != null) {
            usuario.setFotoPerfil(fotoPadrao);
        }

        usuarioRepository.save(usuario);
        return "redirect:/login";
    }

    @GetMapping("/")
    public String home(Model model, HttpSession session) {

        List<Filme> filmesCache = (List<Filme>) session.getAttribute("filmesCache");

        if (filmesCache == null || filmesCache.isEmpty()) {

            filmesCache = imdbService.buscarFilmesAleatorios();
            session.setAttribute("filmesCache", filmesCache);
        }

        // 27 filmes
        int fim = Math.min(27, filmesCache.size());
        List<Filme> filmesPagina = filmesCache.subList(0, fim);

        model.addAttribute("usuarioLogado", session.getAttribute("usuarioLogado"));
        model.addAttribute("filmes", filmesPagina);
        model.addAttribute("quantidade", fim);
        model.addAttribute("temMais", fim < filmesCache.size());

        return "home";
    }


    @GetMapping("/pesquisa")
    public String pesquisa(@RequestParam(required = false) String q,
                           HttpSession session,
                           Model model) {
        if (session.getAttribute("usuarioLogado") == null) {
            model.addAttribute("erro", "Faça login para pesquisar filmes.");
            return "login";
        }

        List<Filme> filmes;

        if (q != null && !q.isBlank()) {
            filmes = imdbService.buscarPorTitulo(q);
        } else {
            filmes = List.of();
        }

        model.addAttribute("filmes", filmes);
        model.addAttribute("query", q);

        return "pesquisa";
    }

    @GetMapping("/detalhes/{imdbId}")
    @ResponseBody
    public Filme getDetalhes(@PathVariable String imdbId) {
        return imdbService.buscarDetalhes(imdbId);
    }

    @GetMapping("/filmes/mais")
    @ResponseBody
    public List<Filme> carregarMaisFilmes(HttpSession session) {
        // Busca o cache atual, se houver
        List<Filme> filmesCache = (List<Filme>) session.getAttribute("filmesCache");

        // Se não existir, cria um novo cache
        if (filmesCache == null) {
            filmesCache = new ArrayList<>();
        }

        // Busca novos filmes do serviço
        List<Filme> novos = imdbService.buscarFilmesAleatorios();

        // Garante que não haja duplicados
        Set<String> idsExistentes = filmesCache.stream()
                .map(Filme::getImdbId)
                .collect(Collectors.toSet());

        List<Filme> novosUnicos = novos.stream()
                .filter(f -> !idsExistentes.contains(f.getImdbId()))
                .collect(Collectors.toList());

        // Adiciona ao cache e retorna apenas 27
        filmesCache.addAll(novosUnicos);
        session.setAttribute("filmesCache", filmesCache);

        return novosUnicos.stream().limit(27).collect(Collectors.toList());
    }
}
