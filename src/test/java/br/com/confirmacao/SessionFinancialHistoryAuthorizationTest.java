package br.com.confirmacao;
import br.com.confirmacao.session.api.SessionFinancialHistoryController;
import br.com.confirmacao.session.application.SessionNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
class SessionFinancialHistoryAuthorizationTest {
 @Test void rejectsHistoryOfAnotherProfessionalBeforeReadingEntries(){
  var jdbc=new JdbcTemplate(new DriverManagerDataSource("jdbc:h2:mem:"+UUID.randomUUID()+";DB_CLOSE_DELAY=-1","sa",""));
  jdbc.execute("create table sessions(id uuid primary key, professional_id uuid)");
  var sessionId=UUID.randomUUID();jdbc.update("insert into sessions values (?,?)",sessionId,UUID.randomUUID());
  var jwt=Jwt.withTokenValue("test").header("alg","none").claim("professional_id",UUID.randomUUID().toString()).build();
  assertThrows(SessionNotFoundException.class,()->new SessionFinancialHistoryController(jdbc).history(new JwtAuthenticationToken(jwt),sessionId));
 }
}
