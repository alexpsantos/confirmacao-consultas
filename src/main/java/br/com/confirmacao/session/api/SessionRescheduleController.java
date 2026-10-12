package br.com.confirmacao.session.api;
import br.com.confirmacao.session.application.SessionRescheduleService;
import br.com.confirmacao.session.application.SessionRescheduleService.Scope;
import br.com.confirmacao.session.domain.SessionRescheduleHistory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;
@RestController @RequestMapping("/api/v1/sessions") public class SessionRescheduleController {
 private final SessionRescheduleService service;
 public SessionRescheduleController(SessionRescheduleService service){this.service=service;}
 public record Request(@NotNull Instant startsAt,@NotNull Instant endsAt,@Size(max=500) String reason,@NotNull Scope scope){}
 public record History(UUID id,Instant previousStart,Instant previousEnd,Instant newStart,Instant newEnd,String reason,String actorName,Instant changedAt){static History from(SessionRescheduleHistory h){return new History(h.getId(),h.getPreviousStart(),h.getPreviousEnd(),h.getNewStart(),h.getNewEnd(),h.getReason(),h.getActorName(),h.getChangedAt());}}
 @PatchMapping("/{id}/reschedule") public List<SessionResponse> reschedule(JwtAuthenticationToken jwt,@PathVariable UUID id,@Valid @RequestBody Request request){return service.reschedule(pid(jwt),id,request.startsAt(),request.endsAt(),request.reason(),request.scope()).stream().map(SessionResponse::from).toList();}
 @GetMapping("/{id}/reschedule/history") public List<History> history(JwtAuthenticationToken jwt,@PathVariable UUID id){return service.history(pid(jwt),id).stream().map(History::from).toList();}
 public record CancelRequest(@Size(max=500) String reason,@NotNull Scope scope){}
 @PatchMapping("/{id}/cancel") public List<SessionResponse> cancel(JwtAuthenticationToken jwt,@PathVariable UUID id,@Valid @RequestBody CancelRequest request){return service.cancel(pid(jwt),id,request.reason(),request.scope()).stream().map(SessionResponse::from).toList();}
 private UUID pid(JwtAuthenticationToken jwt){return UUID.fromString(jwt.getToken().getClaimAsString("professional_id"));}
}
