package Cinemach.cinemach.controller;

import Cinemach.cinemach.model.Usuario;
import Cinemach.cinemach.model.FilmeSalvo;
import Cinemach.cinemach.repository.SolicitacaoRepository;
import Cinemach.cinemach.repository.SugestaoBloqueadaRepository;
import Cinemach.cinemach.repository.UsuarioRepository;
import Cinemach.cinemach.repository.FilmeSalvoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import Cinemach.cinemach.model.*;
import Cinemach.cinemach.repository.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class SugestaoController {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private FilmeSalvoRepository filmeSalvoRepository;
    @Autowired private SolicitacaoRepository solicitacaoRepository;
    @Autowired private SugestaoBloqueadaRepository bloqueadaRepository;

    @GetMapping("/chat")
    public String chat(HttpSession session, Model model) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            model.addAttribute("erro", "Você precisa estar logado para acessar o chat.");
            return "login";
        }

        // remove bloqueios expirados
        bloqueadaRepository.findByUsuario(usuarioLogado).forEach(b -> {
            if (b.getExpiracao().isBefore(LocalDateTime.now())) {
                bloqueadaRepository.delete(b);
            }
        });

        List<Usuario> todosUsuarios = usuarioRepository.findAll().stream()
                .filter(u -> !u.getId().equals(usuarioLogado.getId()))
                .collect(Collectors.toList());

        // aplica filtro de bloqueio
        List<Long> bloqueados = bloqueadaRepository.findByUsuario(usuarioLogado)
                .stream().map(b -> b.getBloqueado().getId()).toList();

        boolean usuarioTemFilmes = temFilmes(usuarioLogado);

        List<Usuario> sugeridos = todosUsuarios.stream()
                .filter(u -> !bloqueados.contains(u.getId()))
                .filter(u -> {
                    int generosEmComum = calcularPorGeneros(usuarioLogado, u);
                    if (generosEmComum == 0) return false;
                    if (!usuarioTemFilmes) return true;

                    boolean outroTemFilmes = temFilmes(u);
                    if (outroTemFilmes) {
                        int filmesComuns = contarFilmesComuns(usuarioLogado, u);
                        return filmesComuns > 0;
                    }
                    return false;
                })
                .limit(20)
                .collect(Collectors.toList());

        Map<Long, String> compatibilidades = new HashMap<>();
        for (Usuario u : sugeridos) {
            compatibilidades.put(u.getId(), calcularCompatibilidade(usuarioLogado, u));
        }

        // solicitações recebidas pendentes
        List<Solicitacao> solicitacoesPendentes =
                solicitacaoRepository.findByDestinatarioAndStatus(usuarioLogado, "PENDENTE");

        model.addAttribute("usuarioLogado", usuarioLogado);
        model.addAttribute("sugeridos", sugeridos);
        model.addAttribute("compatibilidades", compatibilidades);
        model.addAttribute("solicitacoes", solicitacoesPendentes);

        return "chat";
    }

    @PostMapping("/enviarSolicitacao")
    public String enviarSolicitacao(@RequestParam Long id, HttpSession session) {
        Usuario remetente = (Usuario) session.getAttribute("usuarioLogado");
        Usuario destinatario = usuarioRepository.findById(id).orElse(null);
        if (remetente != null && destinatario != null) {
            Solicitacao s = new Solicitacao();
            s.setRemetente(remetente);
            s.setDestinatario(destinatario);
            solicitacaoRepository.save(s);
        }
        return "redirect:/chat";
    }

    @PostMapping("/responderSolicitacao")
    public String responderSolicitacao(@RequestParam Long id, @RequestParam String acao) {
        Solicitacao s = solicitacaoRepository.findById(id).orElse(null);
        if (s != null) {
            if (acao.equals("aceitar")) s.setStatus("ACEITA");
            else s.setStatus("NEGADA");
            solicitacaoRepository.save(s);
        }
        return "redirect:/chat";
    }

    @PostMapping("/excluirSugestao")
    public String excluirSugestao(@RequestParam Long id, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        Usuario bloqueado = usuarioRepository.findById(id).orElse(null);
        if (usuario != null && bloqueado != null) {
            if (!bloqueadaRepository.existsByUsuarioAndBloqueado(usuario, bloqueado)) {
                SugestaoBloqueada b = new SugestaoBloqueada(usuario, bloqueado,
                        LocalDateTime.now().plusDays(7));
                bloqueadaRepository.save(b);
            }
        }
        return "redirect:/chat";
    }

    // =================== LÓGICAS DE COMPATIBILIDADE =================== //

    private String calcularCompatibilidade(Usuario a, Usuario b) {
        int score = 0;

        boolean temFilmesA = temFilmes(a);
        boolean temFilmesB = temFilmes(b);

        // 1️⃣ Gêneros
        int generosIguais = calcularPorGeneros(a, b);
        score += generosIguais * 10;

        // 2️⃣ Top 10 (só se ambos tiverem filmes)
        if (temFilmesA && temFilmesB) {
            String afinidadeTop10 = calcularPorTop10(a, b);
            switch (afinidadeTop10) {
                case "Baixa" -> score += 10;
                case "Média" -> score += 25;
                case "Alta" -> score += 40;
            }
        }

        // 3️⃣ Se nenhum tiver filmes
        if (!temFilmesA && !temFilmesB) {
            if (generosIguais == 0) return "Baixa";
            if (generosIguais == 1) return "Baixa";
            if (generosIguais == 2) return "Média";
            return "Alta";
        }

        // 4️⃣ Avalia score final
        if (score >= 50) return "Alta";
        if (score >= 30) return "Média";
        return "Baixa";
    }

    // =================== FUNÇÕES DE APOIO =================== //

    private int calcularPorGeneros(Usuario a, Usuario b) {
        if (a.getGeneros() == null || b.getGeneros() == null) return 0;

        Set<String> generosA = Arrays.stream(a.getGeneros().split(","))
                .map(String::trim)
                .filter(g -> !g.isEmpty())
                .collect(Collectors.toSet());

        Set<String> generosB = Arrays.stream(b.getGeneros().split(","))
                .map(String::trim)
                .filter(g -> !g.isEmpty())
                .collect(Collectors.toSet());

        generosA.retainAll(generosB);
        return generosA.size();
    }

    private String calcularPorTop10(Usuario a, Usuario b) {
        int filmesComuns = contarFilmesComuns(a, b);

        if (filmesComuns >= 5) return "Alta";
        if (filmesComuns >= 2) return "Média";
        if (filmesComuns == 1) return "Baixa";
        return "Neutro";
    }

    private int contarFilmesComuns(Usuario a, Usuario b) {
        List<FilmeSalvo> filmesA = filmeSalvoRepository.findByUsuarioId(a.getId());
        List<FilmeSalvo> filmesB = filmeSalvoRepository.findByUsuarioId(b.getId());

        if (filmesA.isEmpty() || filmesB.isEmpty()) return 0;

        Set<String> idsA = filmesA.stream().map(FilmeSalvo::getImdbId).collect(Collectors.toSet());
        Set<String> idsB = filmesB.stream().map(FilmeSalvo::getImdbId).collect(Collectors.toSet());

        idsA.retainAll(idsB);
        return idsA.size();
    }

    private boolean temFilmes(Usuario u) {
        return !filmeSalvoRepository.findByUsuarioId(u.getId()).isEmpty();
    }
}