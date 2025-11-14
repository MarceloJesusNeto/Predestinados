package Cinemach.cinemach.controller;

import Cinemach.cinemach.dto.ChatEntry;
import Cinemach.cinemach.model.Usuario;
import Cinemach.cinemach.model.FilmeSalvo;
import Cinemach.cinemach.model.SugestaoBloqueada;
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

@Controller
public class SugestaoController {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private FilmeSalvoRepository filmeSalvoRepository;
    @Autowired private SolicitacaoRepository solicitacaoRepository;
    @Autowired private SugestaoBloqueadaRepository bloqueadaRepository;
    @Autowired private MensagemRepository mensagemRepository;

    @GetMapping("/chat")
    public String chat(@RequestParam(required = false) Long chat, HttpSession session, Model model) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            model.addAttribute("erro", "Você precisa estar logado para acessar o chat.");
            return "login";
        }


        List<Usuario> todosUsuarios = usuarioRepository.findAll().stream()
                .filter(u -> !u.getId().equals(usuarioLogado.getId()))
                .collect(Collectors.toList());


        List<Usuario> sugeridos = todosUsuarios.stream()
                .filter(usuario -> !temMatchOuSolicitacaoAtiva(usuarioLogado, usuario))
                .filter(usuario -> !estaBloqueado(usuarioLogado, usuario))
                .limit(5)
                .collect(Collectors.toList());


        Map<Long, String> compatibilidades = new HashMap<>();
        for (Usuario u : sugeridos) {
            String compat = calcularCompatibilidadeAvancada(usuarioLogado, u);
            compatibilidades.put(u.getId(), compat);
        }


        List<Solicitacao> solicitacoesPendentes = solicitacaoRepository.findByDestinatarioAndStatus(usuarioLogado, "PENDENTE");
        List<Solicitacao> solicitacoesAceitas = solicitacaoRepository.findChatsAtivos(usuarioLogado);


        List<ChatEntry> chatsAtivos = new ArrayList<>();
        for (Solicitacao s : solicitacoesAceitas) {
            Usuario outro = s.getRemetente().getId().equals(usuarioLogado.getId())
                    ? s.getDestinatario()
                    : s.getRemetente();

            List<Mensagem> mensagensChat = mensagemRepository.findBySolicitacaoOrderByDataEnvioAsc(s);
            String ultimaMensagem = mensagensChat.isEmpty() ? "" :
                    mensagensChat.get(mensagensChat.size() - 1).getConteudo();

            if (ultimaMensagem.length() > 30) {
                ultimaMensagem = ultimaMensagem.substring(0, 30) + "...";
            }

            chatsAtivos.add(new ChatEntry(outro, ultimaMensagem, false));
        }


        Usuario destinatarioChat = null;
        List<Mensagem> mensagensChat = new ArrayList<>();
        if (chat != null) {
            destinatarioChat = usuarioRepository.findById(chat).orElse(null);
            if (destinatarioChat != null) {
                Optional<Solicitacao> solicitacaoOpt = solicitacaoRepository.findChatAtivoEntreUsuarios(usuarioLogado, destinatarioChat);
                if (solicitacaoOpt.isPresent()) {
                    mensagensChat = mensagemRepository.findMensagensPorSolicitacao(solicitacaoOpt.get());
                }
            }
        }


        model.addAttribute("chats", chatsAtivos);
        model.addAttribute("usuarioLogado", usuarioLogado);
        model.addAttribute("sugeridos", sugeridos);
        model.addAttribute("compatibilidades", compatibilidades);
        model.addAttribute("solicitacoes", solicitacoesPendentes);
        model.addAttribute("destinatario", destinatarioChat);
        model.addAttribute("mensagens", mensagensChat);

        if (destinatarioChat != null) {
            model.addAttribute("destinatarioId", destinatarioChat.getId());
        }

        return "chat";
    }


    private String calcularCompatibilidadeAvancada(Usuario a, Usuario b) {

        int generosComuns = calcularPorGeneros(a, b);


        List<FilmeSalvo> filmesA = filmeSalvoRepository.findByUsuarioId(a.getId());
        List<FilmeSalvo> filmesB = filmeSalvoRepository.findByUsuarioId(b.getId());


        int filmesComuns = contarFilmesComuns(a, b);


        double porcentagemSobreposicao = calcularPorcentagemSobreposicao(filmesA, filmesB, filmesComuns);


        int score = 0;


        score += generosComuns * 20;



        score += filmesComuns * 15;


        if (porcentagemSobreposicao > 0) {
            int pontosSobreposicao = (int) (porcentagemSobreposicao * 2);
            score += pontosSobreposicao;

        }

        if (!filmesA.isEmpty() && !filmesB.isEmpty()) {
            score += 25;

        }


        if (porcentagemSobreposicao >= 30) {
            score += 30;

        } else if (porcentagemSobreposicao >= 15) {
            score += 15;
        }


        if (filmesComuns >= 5) {
            score += 20;

        } else if (filmesComuns >= 3) {
            score += 10;

        }




        String compatibilidade;
        if (score >= 100) {
            compatibilidade = "Alta";
        } else if (score >= 60) {
            compatibilidade = "Média";
        } else if (score >= 30) {
            compatibilidade = "Baixa";
        } else {
            compatibilidade = "Muito Baixa";
        }

        return compatibilidade;
    }


    private double calcularPorcentagemSobreposicao(List<FilmeSalvo> filmesA, List<FilmeSalvo> filmesB, int filmesComuns) {
        if (filmesA.isEmpty() || filmesB.isEmpty()) return 0.0;


        int menorColecao = Math.min(filmesA.size(), filmesB.size());

        if (menorColecao == 0) return 0.0;

        double porcentagem = ((double) filmesComuns / menorColecao) * 100;
        return Math.min(porcentagem, 100.0);
    }


    private int contarFilmesComuns(Usuario a, Usuario b) {
        try {
            List<FilmeSalvo> filmesA = filmeSalvoRepository.findByUsuarioId(a.getId());
            List<FilmeSalvo> filmesB = filmeSalvoRepository.findByUsuarioId(b.getId());

            if (filmesA.isEmpty() || filmesB.isEmpty()) return 0;

            Set<String> idsA = filmesA.stream()
                    .map(FilmeSalvo::getImdbId)
                    .collect(Collectors.toSet());

            Set<String> idsB = filmesB.stream()
                    .map(FilmeSalvo::getImdbId)
                    .collect(Collectors.toSet());

            idsA.retainAll(idsB);


            if (!idsA.isEmpty()) {
                System.out.println("🎯 Filmes em comum encontrados:");
                for (String imdbId : idsA) {
                    filmesA.stream()
                            .filter(f -> f.getImdbId().equals(imdbId))
                            .findFirst()
                            .ifPresent(f -> System.out.println("   - " + f.getTitulo()));
                }
            }

            return idsA.size();
        } catch (Exception e) {
            System.out.println("❌ Erro ao contar filmes comuns: " + e.getMessage());
            return 0;
        }
    }


    private int calcularPorGeneros(Usuario a, Usuario b) {
        if (a.getGeneros() == null || b.getGeneros() == null) return 0;

        try {
            Set<String> generosA = Arrays.stream(a.getGeneros().split(","))
                    .map(String::trim)
                    .filter(g -> !g.isEmpty())
                    .collect(Collectors.toSet());

            Set<String> generosB = Arrays.stream(b.getGeneros().split(","))
                    .map(String::trim)
                    .filter(g -> !g.isEmpty())
                    .collect(Collectors.toSet());


            System.out.println("🎭 " + a.getNome() + " gêneros: " + generosA);
            System.out.println("🎭 " + b.getNome() + " gêneros: " + generosB);

            generosA.retainAll(generosB);


            if (!generosA.isEmpty()) {
                System.out.println("🎯 Gêneros em comum: " + generosA);
            }

            return generosA.size();
        } catch (Exception e) {
            System.out.println("❌ Erro ao calcular gêneros: " + e.getMessage());
            return 0;
        }
    }


    private boolean temMatchOuSolicitacaoAtiva(Usuario usuario1, Usuario usuario2) {
        List<Solicitacao> solicitacoes = solicitacaoRepository.findByRemetenteOrDestinatario(usuario1, usuario2);

        return solicitacoes.stream().anyMatch(s -> {
            boolean isParCorreto = (s.getRemetente().equals(usuario1) && s.getDestinatario().equals(usuario2)) ||
                    (s.getRemetente().equals(usuario2) && s.getDestinatario().equals(usuario1));


            boolean isAtiva = "PENDENTE".equals(s.getStatus()) || "ACEITA".equals(s.getStatus());

            return isParCorreto && isAtiva;
        });
    }


    private boolean estaBloqueado(Usuario usuario, Usuario possivelSugestao) {
        Optional<SugestaoBloqueada> bloqueio = bloqueadaRepository.findByUsuarioAndBloqueado(usuario, possivelSugestao);

        if (bloqueio.isPresent()) {
            SugestaoBloqueada b = bloqueio.get();

            return b.getExpiracao().isAfter(LocalDateTime.now());
        }

        return false;
    }

    private boolean temFilmes(Usuario u) {
        try {
            return !filmeSalvoRepository.findByUsuarioId(u.getId()).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    @PostMapping("/responderSolicitacao")
    public String responderSolicitacao(@RequestParam Long id, @RequestParam String acao) {
        Solicitacao s = solicitacaoRepository.findById(id).orElse(null);
        if (s != null) {
            if (acao.equals("aceitar")) {
                s.setStatus("ACEITA");
                solicitacaoRepository.save(s);


                Usuario remetente = s.getRemetente();
                Usuario destinatario = s.getDestinatario();


                LocalDateTime expiracao = LocalDateTime.now().plusDays(30);

                if (!bloqueadaRepository.existsByUsuarioAndBloqueado(remetente, destinatario)) {
                    bloqueadaRepository.save(new SugestaoBloqueada(remetente, destinatario, expiracao));
                }
                if (!bloqueadaRepository.existsByUsuarioAndBloqueado(destinatario, remetente)) {
                    bloqueadaRepository.save(new SugestaoBloqueada(destinatario, remetente, expiracao));
                }

            } else {
                s.setStatus("NEGADA");
                solicitacaoRepository.save(s);

                Usuario remetente = s.getRemetente();
                Usuario destinatario = s.getDestinatario();


                SugestaoBloqueada b1 = new SugestaoBloqueada(remetente, destinatario, LocalDateTime.now().plusDays(7));
                SugestaoBloqueada b2 = new SugestaoBloqueada(destinatario, remetente, LocalDateTime.now().plusDays(7));
                bloqueadaRepository.save(b1);
                bloqueadaRepository.save(b2);

                solicitacaoRepository.delete(s);
            }
        }
        return "redirect:/chat";
    }

    @PostMapping("/excluirSugestao")
    public String excluirSugestao(@RequestParam Long id, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        Usuario bloqueado = usuarioRepository.findById(id).orElse(null);
        if (usuario != null && bloqueado != null) {

            if (!bloqueadaRepository.existsByUsuarioAndBloqueado(usuario, bloqueado)) {
                SugestaoBloqueada b1 = new SugestaoBloqueada(usuario, bloqueado, LocalDateTime.now().plusDays(7));
                bloqueadaRepository.save(b1);
            }

            if (!bloqueadaRepository.existsByUsuarioAndBloqueado(bloqueado, usuario)) {
                SugestaoBloqueada b2 = new SugestaoBloqueada(bloqueado, usuario, LocalDateTime.now().plusDays(7));
                bloqueadaRepository.save(b2);
            }
        }
        return "redirect:/chat";
    }

    @PostMapping("/enviarSolicitacao")
    @ResponseBody
    public String enviarSolicitacao(@RequestParam Long id, HttpSession session) {
        Usuario remetente = (Usuario) session.getAttribute("usuarioLogado");
        Usuario destinatario = usuarioRepository.findById(id).orElse(null);

        if (remetente == null || destinatario == null) {
            return "erro";
        }


        if (temMatchOuSolicitacaoAtiva(remetente, destinatario)) {
            return "jaExiste";
        }


        if (estaBloqueado(remetente, destinatario)) {
            return "bloqueado";
        }


        Solicitacao nova = new Solicitacao();
        nova.setRemetente(remetente);
        nova.setDestinatario(destinatario);
        nova.setStatus("PENDENTE");
        nova.setDataEnvio(LocalDateTime.now());
        solicitacaoRepository.save(nova);

        if (!bloqueadaRepository.existsByUsuarioAndBloqueado(remetente, destinatario)) {
            bloqueadaRepository.save(new SugestaoBloqueada(remetente, destinatario, LocalDateTime.now().plusDays(7)));
        }


        if (!bloqueadaRepository.existsByUsuarioAndBloqueado(destinatario, remetente)) {
            bloqueadaRepository.save(new SugestaoBloqueada(destinatario, remetente, LocalDateTime.now().plusDays(7)));
        }

        return "ok";
    }

    @PostMapping("/excluirMatch")
    public String excluirMatch(@RequestParam Long usuarioId, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        Usuario outroUsuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (outroUsuario == null) {
            return "redirect:/chat";
        }

        Optional<Solicitacao> solicitacaoOpt = solicitacaoRepository.findChatAtivoEntreUsuarios(usuarioLogado, outroUsuario);

        if (solicitacaoOpt.isPresent()) {
            Solicitacao solicitacao = solicitacaoOpt.get();


            List<Mensagem> mensagens = mensagemRepository.findBySolicitacaoOrderByDataEnvioAsc(solicitacao);
            mensagemRepository.deleteAll(mensagens);


            solicitacaoRepository.delete(solicitacao);


            LocalDateTime expiracao = LocalDateTime.now().plusDays(30);


            Optional<SugestaoBloqueada> bloqueio1 = bloqueadaRepository.findByUsuarioAndBloqueado(usuarioLogado, outroUsuario);
            if (bloqueio1.isPresent()) {

                SugestaoBloqueada b1 = bloqueio1.get();
                b1.setExpiracao(expiracao);
                bloqueadaRepository.save(b1);
            } else {

                bloqueadaRepository.save(new SugestaoBloqueada(usuarioLogado, outroUsuario, expiracao));
            }


            Optional<SugestaoBloqueada> bloqueio2 = bloqueadaRepository.findByUsuarioAndBloqueado(outroUsuario, usuarioLogado);
            if (bloqueio2.isPresent()) {

                SugestaoBloqueada b2 = bloqueio2.get();
                b2.setExpiracao(expiracao);
                bloqueadaRepository.save(b2);
            } else {

                bloqueadaRepository.save(new SugestaoBloqueada(outroUsuario, usuarioLogado, expiracao));
            }
        }

        return "redirect:/chat";
    }
}