package br.com.confirmacao.session.api;
import br.com.confirmacao.session.application.ProfessionalFinanceService;
import br.com.confirmacao.session.application.ProfessionalFinanceService.DateBasis;
import br.com.confirmacao.session.domain.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.UUID;
@RestController @RequestMapping("/api/v1/sessions/finance")
public class ProfessionalFinanceController {
 private final ProfessionalFinanceService service;
 public ProfessionalFinanceController(ProfessionalFinanceService service){this.service=service;}
 @GetMapping public ProfessionalFinanceService.Result find(JwtAuthenticationToken jwt,@RequestParam(required=false) UUID patientId,@RequestParam(required=false) LocalDate from,@RequestParam(required=false) LocalDate to,@RequestParam(defaultValue="SESSION") DateBasis basis,@RequestParam(required=false) PaymentStatus status,@RequestParam(required=false) PaymentMethod method,@RequestParam(defaultValue="false") boolean unspecifiedMethod,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){
  return service.find(UUID.fromString(jwt.getToken().getClaimAsString("professional_id")),patientId,from,to,basis,status,method,unspecifiedMethod,page,size);
 }
}
