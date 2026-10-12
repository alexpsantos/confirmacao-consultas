package br.com.confirmacao;
import br.com.confirmacao.professional.api.ProfessionalSessionPriceController;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import java.math.BigDecimal;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
class ProfessionalSessionPriceTest {
 @Test void preferenceUpdatesOnlyFuturePendingSessionsWithoutCustomAmount(){
  var jdbc=new JdbcTemplate(new DriverManagerDataSource("jdbc:h2:mem:"+UUID.randomUUID()+";DB_CLOSE_DELAY=-1","sa",""));
  jdbc.execute("create table professionals(id uuid primary key, default_session_price decimal(10,2), updated_at timestamp)");
  jdbc.execute("create table sessions(id int primary key, professional_id uuid, financial_amount decimal(10,2), starts_at timestamp, status varchar(20), payment_status varchar(20), financial_amount_overridden boolean, updated_at timestamp)");
  var owner=UUID.randomUUID();var other=UUID.randomUUID();
  jdbc.update("insert into professionals values (?,100,current_timestamp)",owner);
  for(int i=1;i<=7;i++)jdbc.update("insert into sessions values (?, ?, ?, DATEADD('DAY', ?, CURRENT_TIMESTAMP), ?, ?, ?, CURRENT_TIMESTAMP)",i,i==7?other:owner,i==3?75:100,i==2?-1:1,i==5?"CANCELED":"SCHEDULED",i==4?"PAID":i==5?"CANCELED":i==6?"EXEMPT":"PENDING",i==3);
  var jwt=Jwt.withTokenValue("test").header("alg","none").claim("professional_id",owner.toString()).build();
  new ProfessionalSessionPriceController(jdbc).update(new JwtAuthenticationToken(jwt),new ProfessionalSessionPriceController.PriceRequest(new BigDecimal("150.00")));
  assertEquals(new BigDecimal("150.00"),jdbc.queryForObject("select financial_amount from sessions where id=1",BigDecimal.class));
  for(int i=2;i<=7;i++)assertEquals(new BigDecimal(i==3?"75.00":"100.00"),jdbc.queryForObject("select financial_amount from sessions where id=?",BigDecimal.class,i));
 }
}
