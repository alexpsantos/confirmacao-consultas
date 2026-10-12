package br.com.confirmacao.session.api;
import br.com.confirmacao.session.application.SessionNotFoundException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/v1/sessions")
public class SessionFinancialHistoryController {
 private final JdbcTemplate jdbc;
 public SessionFinancialHistoryController(JdbcTemplate jdbc){this.jdbc=jdbc;}
 @GetMapping("/{id}/finance/history") public List<HistoryResponse> history(JwtAuthenticationToken jwt,@PathVariable UUID id){
  var professionalId=UUID.fromString(jwt.getToken().getClaimAsString("professional_id"));
  var count=jdbc.queryForObject("select count(*) from sessions where id=? and professional_id=?",Long.class,id,professionalId);
  if(count==null||count==0)throw new SessionNotFoundException(id);
  return jdbc.query("select h.* from session_financial_history h join sessions s on s.id=h.session_id where s.id=? and s.professional_id=? order by h.changed_at desc,h.id desc",(rs,n)->new HistoryResponse(rs.getLong("id"),rs.getTimestamp("changed_at").toInstant().toString(),rs.getString("actor_name"),rs.getString("before_state"),rs.getString("after_state")),id,professionalId);
 }
 public record HistoryResponse(long id,String changedAt,String actorName,String beforeState,String afterState){}
}
