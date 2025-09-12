package Cinemach.cinemach.controller;

import Cinemach.cinemach.model.Filme;
import Cinemach.cinemach.service.ImdbService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class HomeController {

    private final ImdbService imdbService;

    public HomeController(ImdbService imdbService) {
        this.imdbService = imdbService;
    }

    @GetMapping("/pesquisa")
    public String pesquisa(@RequestParam(required = false) String q, Model model) {
        List<Filme> filmes;

        if (q != null && !q.isBlank()) {
            filmes = imdbService.buscarPorTitulo(q);
        } else {
            filmes = List.of(); // lista vazia quando não pesquisou nada
        }

        model.addAttribute("filmes", filmes);
        model.addAttribute("query", q);

        return "pesquisa"; // pesquisa.html
    }

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        // busca só uma vez
        List<Filme> todosFilmes = imdbService.buscarFilmesAleatorios();
        session.setAttribute("filmesCache", todosFilmes);

        int fim = Math.min(16, todosFilmes.size());
        List<Filme> filmesPagina = todosFilmes.subList(0, fim);

        model.addAttribute("filmes", filmesPagina);
        model.addAttribute("quantidade", fim);
        model.addAttribute("temMais", fim < todosFilmes.size());

        return "home";
    }

    @GetMapping("/mais")
    @ResponseBody
    public List<Filme> mais(@RequestParam int offset,
                            @RequestParam int limit,
                            HttpSession session) {

        @SuppressWarnings("unchecked")
        List<Filme> todosFilmes = (List<Filme>) session.getAttribute("filmesCache");

        if (todosFilmes == null) {
            todosFilmes = imdbService.buscarFilmesAleatorios();
            session.setAttribute("filmesCache", todosFilmes);
        }

        int fim = Math.min(offset + limit, todosFilmes.size());
        if (offset >= todosFilmes.size()) {
            return List.of(); // acabou
        }

        return todosFilmes.subList(offset, fim);
    }
}
