package br.com.confirmacao.session.api;
import br.com.confirmacao.session.domain.*;import jakarta.validation.constraints.*;import java.time.Instant;import java.util.UUID;
public record SessionRequest(@NotNull UUID patientId,@NotNull Instant startsAt,@NotNull Instant endsAt,@NotNull SessionModality modality,SessionStatus status,@Size(max=500)String meetingLink,@Size(max=500)String notes){}
