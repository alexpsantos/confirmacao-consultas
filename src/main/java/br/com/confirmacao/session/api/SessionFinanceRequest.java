package br.com.confirmacao.session.api;
import br.com.confirmacao.session.domain.PaymentStatus;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
public record SessionFinanceRequest(@NotNull @DecimalMin("0.00") @Digits(integer=8,fraction=2) BigDecimal financialAmount,@NotNull PaymentStatus paymentStatus,LocalDate paymentDate,@Size(max=500) String financialNotes,br.com.confirmacao.session.domain.PaymentMethod paymentMethod) {}
