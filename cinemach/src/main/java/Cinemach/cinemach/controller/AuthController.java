package Cinemach.cinemach.controller;

import Cinemach.cinemach.model.Usuario;
import Cinemach.cinemach.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.Cookie;


@Controller
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String senha,
                        HttpSession session,
                        HttpServletResponse response,
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

        Cookie id = new Cookie("usuarioId", usuario.getId().toString());
        Cookie nome = new Cookie("nomeUsuario", usuario.getNome());
        id.setPath("/");
        nome.setPath("/");
        id.setMaxAge(60 * 60 * 24 * 365); // 1 ano
        nome.setMaxAge(60 * 60 * 24 * 365);
        response.addCookie(id);
        response.addCookie(nome);

        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletResponse response) {
        session.invalidate();

        // limpa cookies
        Cookie id = new Cookie("usuarioId", "");
        id.setMaxAge(0);
        id.setPath("/");

        Cookie nome = new Cookie("nomeUsuario", "");
        nome.setMaxAge(0);
        nome.setPath("/");

        response.addCookie(id);
        response.addCookie(nome);

        return "redirect:/";
    }
}
