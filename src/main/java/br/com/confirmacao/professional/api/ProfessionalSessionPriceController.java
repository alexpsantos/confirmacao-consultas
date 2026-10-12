package br.com.confirmacao.professional.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/professionals/me/session-price")
public class ProfessionalSessionPriceController {
  private final JdbcTemplate jdbc;
  public ProfessionalSessionPriceController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

  @GetMapping public PriceResponse get(JwtAuthenticationToken jwt) {
    var price = jdbc.queryForObject("select default_session_price from professionals where id = ?", BigDecimal.class, id(jwt));
    return new PriceResponse(price);
  }

  @org.springframework.transaction.annotation.Transactional
  @PutMapping public PriceResponse update(JwtAuthenticationToken jwt, @Valid @RequestBody PriceRequest request) {
    jdbc.execute((org.springframework.jdbc.core.ConnectionCallback<Void>) connection -> {
      if ("PostgreSQL".equals(connection.getMetaData().getDatabaseProductName())) {
        try (var statement = connection.prepareStatement("select set_config('app.finance_actor', (select full_name from professionals where id = ?), true)")) {
          statement.setObject(1, id(jwt)); statement.execute();
        }
      }
      return null;
    });
    jdbc.update("update professionals set default_session_price = ?, updated_at = current_timestamp where id = ?", request.value(), id(jwt));
    jdbc.update("update sessions set financial_amount = ?, updated_at = current_timestamp where professional_id = ? and starts_at > current_timestamp and status in ('SCHEDULED', 'CONFIRMED') and payment_status = 'PENDING' and financial_amount_overridden = false", request.value(), id(jwt));
    return new PriceResponse(request.value());
  }

  private UUID id(JwtAuthenticationToken jwt) { return UUID.fromString(jwt.getToken().getClaimAsString("professional_id")); }
  public record PriceRequest(@NotNull @DecimalMin(value="0.00") @Digits(integer=8, fraction=2) BigDecimal value) {}
  public record PriceResponse(BigDecimal value) {}
}
