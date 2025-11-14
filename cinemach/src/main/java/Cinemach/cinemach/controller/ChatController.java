package Cinemach.cinemach.controller;

import Cinemach.cinemach.model.Mensagem;
import Cinemach.cinemach.model.Solicitacao;
import Cinemach.cinemach.model.Usuario;
import Cinemach.cinemach.repository.MensagemRepository;
import Cinemach.cinemach.repository.SolicitacaoRepository;
import Cinemach.cinemach.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;
    @Controller
    public class ChatController {

        @Autowired
        private SolicitacaoRepository solicitacaoRepository;

        @Autowired
        private MensagemRepository mensagemRepository;

        @Autowired
        private UsuarioRepository usuarioRepository;

        @PostMapping("/enviarMensagem")
        public String enviarMensagem(@RequestParam Long destinatarioId,
                                     @RequestParam String conteudo,
                                     HttpSession session) {
            Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
            if (usuarioLogado == null) {
                return "redirect:/login";
            }

            Usuario destinatario = usuarioRepository.findById(destinatarioId).orElse(null);
            if (destinatario == null || conteudo == null || conteudo.trim().isEmpty()) {
                return "redirect:/chat";
            }

            // Buscar solicitação ACEITA entre os usuários
            Optional<Solicitacao> solicitacaoOpt = solicitacaoRepository.findChatAtivoEntreUsuarios(usuarioLogado, destinatario);

            if (solicitacaoOpt.isPresent()) {
                Mensagem mensagem = new Mensagem();
                mensagem.setRemetente(usuarioLogado);
                mensagem.setDestinatario(destinatario);
                mensagem.setConteudo(conteudo.trim());
                mensagem.setSolicitacao(solicitacaoOpt.get());

                mensagemRepository.save(mensagem);
            }

            return "redirect:/chat?chat=" + destinatarioId;
        }
    }

