package Cinemach.cinemach.controller;

import Cinemach.cinemach.model.Usuario;
import Cinemach.cinemach.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String senha,
                        HttpSession session,
                        Model model) {
        Usuario usuario = usuarioRepository.findByEmail(email);

        if (usuario == null) {
            model.addAttribute("erro", "E-mail ou senha inválidos!");
            return "login";
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(senha, usuario.getSenha())) {
            model.addAttribute("erro", "E-mail ou senha inválidos!");
            return "login";
        }

        session.setAttribute("usuarioLogado", usuario);
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, Model model) {
        if (session.getAttribute("usuarioLogado") == null) {
            model.addAttribute("erro", "Você precisa estar logado para sair.");
            return "login";
        }

        session.invalidate();
        return "redirect:/";
    }
}
