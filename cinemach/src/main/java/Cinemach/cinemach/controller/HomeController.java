package Cinemach.cinemach.controller;

import Cinemach.cinemach.model.Filme;
import Cinemach.cinemach.model.Usuario;
import Cinemach.cinemach.repository.UsuarioRepository;
import Cinemach.cinemach.service.ImdbService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class HomeController {

    private final ImdbService imdbService;
    private final UsuarioRepository usuarioRepository;

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
    public String registrar(@ModelAttribute Usuario usuario,
                            @RequestParam(value = "generos", required = false) String generos,
                            Model model) {
        try {
            if (usuarioRepository.findByEmail(usuario.getEmail()) != null) {
                model.addAttribute("erro", "Email já cadastrado!");
                return "cadastro";
            }

            if (!usuario.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                model.addAttribute("erro", "Digite um e-mail válido!");
                return "cadastro";
            }

            // ⚠️ Validação: precisa ter exatamente 3 gêneros
            if (generos == null || generos.isBlank() || generos.split(",").length != 3) {
                model.addAttribute("erro", "Escolha exatamente 3 gêneros favoritos!");
                return "cadastro";
            }

            usuario.setGeneros(generos);
            usuarioRepository.save(usuario);

            model.addAttribute("msg", "Cadastro realizado com sucesso! Faça login.");
            return "login";

        } catch (Exception e) {
            model.addAttribute("erro", "Erro ao cadastrar: " + e.getMessage());
            return "cadastro";
        }
    }

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        List<Filme> todosFilmes = imdbService.buscarFilmesAleatorios();
        session.setAttribute("filmesCache", todosFilmes);

        int fim = Math.min(16, todosFilmes.size());
        List<Filme> filmesPagina = todosFilmes.subList(0, fim);

        model.addAttribute("filmes", filmesPagina);
        model.addAttribute("quantidade", fim);
        model.addAttribute("temMais", fim < todosFilmes.size());

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


    @GetMapping("/chat")
    public String chat(HttpSession session, Model model) {
        if (session.getAttribute("usuarioLogado") == null) {
            model.addAttribute("erro", "Você precisa estar logado para acessar o chat.");
            return "login";
        }
        return "chat";
    }

    @GetMapping("/perfil")
    public String perfil(HttpSession session, Model model) {
        if (session.getAttribute("usuarioLogado") == null) {
            model.addAttribute("erro", "Você precisa estar logado para ver seu perfil.");
            return "login";
        }
        return "perfil";
    }

    @GetMapping("/detalhes/{imdbId}")
    @ResponseBody
    public Filme getDetalhes(@PathVariable String imdbId) {
        return imdbService.buscarDetalhes(imdbId);
    }
}
