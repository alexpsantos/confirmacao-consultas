package br.com.confirmacao.auth.application;
import br.com.confirmacao.user.domain.User;
import br.com.confirmacao.user.infrastructure.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
@Service public class PasswordChangeService{private final UserRepository users;private final PasswordEncoder passwords;public PasswordChangeService(UserRepository u,PasswordEncoder p){users=u;passwords=p;}@Transactional public User change(UUID id,String current,String next){var user=users.findById(id).filter(User::isActive).orElseThrow(InvalidCredentialsException::new);if(!passwords.matches(current,user.getPasswordHash()))throw new IllegalArgumentException("A senha atual está incorreta");if(passwords.matches(next,user.getPasswordHash()))throw new IllegalArgumentException("A nova senha deve ser diferente da senha atual");user.changePasswordHash(passwords.encode(next));return user;}}
