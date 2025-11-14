package Cinemach.cinemach.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarTokenRedefinicao(String emailUsuario, String token) {

        String emailDestino = "frazaosilva90@gmail.com";

        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setTo(emailDestino);
        mensagem.setFrom("hello@demomailtrap.co");
        mensagem.setSubject("CineMatch - Token de Recuperação de Senha");
        mensagem.setText(
                "Solicitação de redefinição de senha\n\n" +
                        "Usuário solicitante: " + emailUsuario + "\n" +
                        "Token: " + token + "\n\n" +
                        "Link: http://localhost:8080/redefinir-senha?token=" + token
        );

        mailSender.send(mensagem);
    }
}
