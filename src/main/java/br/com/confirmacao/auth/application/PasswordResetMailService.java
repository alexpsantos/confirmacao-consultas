package br.com.confirmacao.auth.application;

import br.com.confirmacao.user.domain.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class PasswordResetMailService {
    private final JavaMailSender mail;
    private final String frontendUrl;
    private final String from;

    public PasswordResetMailService(JavaMailSender mail,
            @Value("${app.password-reset.frontend-url:http://localhost:5173}") String frontendUrl,
            @Value("${app.password-reset.from}") String from) {
        this.mail = mail;
        this.frontendUrl = frontendUrl;
        this.from = from;
    }

    public void send(User user, String token) {
        String link = frontendUrl + "/?reset-token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(user.getEmail());
        message.setSubject("Recuperação de senha — Confirma");
        message.setText("Olá, " + user.getName() + ".\n\nUse o link abaixo para criar uma nova senha:\n" + link
                + "\n\nO link expira em 30 minutos e pode ser utilizado uma única vez.\nSe você não solicitou a recuperação, ignore esta mensagem.");
        mail.send(message);
    }
}
