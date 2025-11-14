package Cinemach.cinemach.controller;

import Cinemach.cinemach.model.Usuario;
import Cinemach.cinemach.repository.UsuarioRepository;
import Cinemach.cinemach.service.EmailService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.Cookie;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    private Map<String, String> tokens = new HashMap<>();

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String senha,
                        HttpSession session,
                        HttpServletResponse response,
                        Model model) {
        Usuario usuario = usuarioRepository.findByEmail(email);

        if (usuario == null || !passwordEncoder.matches(senha, usuario.getSenha())) {
            model.addAttribute("erro", "E-mail ou senha inválidos!");
            return "login";
        }

        session.setAttribute("usuarioLogado", usuario);

        // Cookies opcionais (pra lembrar usuário)
        Cookie id = new Cookie("usuarioId", usuario.getId().toString());
        Cookie nome = new Cookie("nomeUsuario", usuario.getNome());
        id.setPath("/");
        nome.setPath("/");
        id.setMaxAge(60 * 60 * 24 * 30); // 30 dias
        nome.setMaxAge(60 * 60 * 24 * 30);
        response.addCookie(id);
        response.addCookie(nome);

        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletResponse response) {
        session.invalidate();

        // Limpa cookies
        Cookie id = new Cookie("usuarioId", "");
        id.setMaxAge(0);
        id.setPath("/");
        response.addCookie(id);

        return "redirect:/";
    }

    @GetMapping("/esqueci-senha")
    public String esqueciSenha() {
        return "esqueci-senha";
    }

    @PostMapping("/solicitar-redefinicao")
    public String solicitarRedefinicao(@RequestParam String email, Model model) {
        Usuario usuario = usuarioRepository.findByEmail(email);

        if (usuario == null) {
            model.addAttribute("erro", "Email não encontrado!");
            return "esqueci-senha";
        }

        // Gera token
        String token = UUID.randomUUID().toString().substring(0, 8);
        tokens.put(token, email);

        try {
            // ENVIA O TOKEN POR EMAIL
            emailService.enviarTokenRedefinicao(email, token);

            model.addAttribute("sucesso",
                    "Token enviado para seu email! Verifique sua caixa de entrada.");
        } catch (Exception e) {
            model.addAttribute("erro",
                    "Erro ao enviar email. Tente novamente.");
            e.printStackTrace();
        }

        return "esqueci-senha";
    }

    @GetMapping("/redefinir-senha")
    public String redefinirSenha(@RequestParam String token, Model model) {
        if (!tokens.containsKey(token)) {
            model.addAttribute("erro", "Token inválido!");
            return "redirect:/esqueci-senha";
        }

        model.addAttribute("token", token);
        return "redefinir-senha";
    }

    @PostMapping("/redefinir-senha")
    public String redefinirSenha(@RequestParam String token,
                                 @RequestParam String novaSenha,
                                 Model model) {
        if (!tokens.containsKey(token)) {
            model.addAttribute("erro", "Token inválido!");
            return "redefinir-senha";
        }

        String email = tokens.get(token);
        Usuario usuario = usuarioRepository.findByEmail(email);

        if (usuario != null) {
            usuario.setSenha(passwordEncoder.encode(novaSenha));
            usuarioRepository.save(usuario);
            tokens.remove(token); // Remove o token usado

            return "redirect:/login?sucesso=Senha alterada com sucesso!";
        }

        model.addAttribute("erro", "Erro ao redefinir senha.");
        return "redefinir-senha";
    }
}