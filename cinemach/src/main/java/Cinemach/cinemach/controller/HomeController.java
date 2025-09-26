package Cinemach.cinemach.controller;

import Cinemach.cinemach.model.Filme;
import Cinemach.cinemach.service.ImdbService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class HomeController {

    private final ImdbService imdbService;

    public HomeController(ImdbService imdbService) {
        this.imdbService = imdbService;
    }

    @GetMapping("/chat")
    public String chat(){
        return "chat";
    }

    @GetMapping("/curtidos")
    public String fav(){
        return "curtidos";
    }

    @GetMapping("/perfil")
    public String perfil(){
        return "perfil";
    }

    @GetMapping("/pesquisa")
    public String pesquisa(@RequestParam(required = false) String q, Model model) {
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

    @GetMapping("/detalhes/{imdbId}")
    @ResponseBody
    public Filme getDetalhes(@PathVariable String imdbId) {
        return imdbService.buscarDetalhes(imdbId);
    }

}
