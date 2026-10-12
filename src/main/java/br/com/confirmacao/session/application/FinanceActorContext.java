package br.com.confirmacao.session.application;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.util.UUID;
@Component public class FinanceActorContext {
 private final JdbcTemplate jdbc;
 public FinanceActorContext(JdbcTemplate jdbc){this.jdbc=jdbc;}
 public void set(UUID professionalId){
  jdbc.queryForObject("select set_config('app.finance_actor', (select full_name from professionals where id = ?), true)",String.class,professionalId);
 }
}
