package br.com.confirmacao.session.api;
import br.com.confirmacao.session.domain.*;import java.time.Instant;import java.util.UUID;
public record SessionResponse(UUID id,UUID patientId,String patientName,Instant startsAt,Instant endsAt,SessionModality modality,SessionStatus status,String meetingLink,String notes){public static SessionResponse from(SessionAppointment s){return new SessionResponse(s.getId(),s.getPatient().getId(),s.getPatient().getFullName(),s.getStartsAt(),s.getEndsAt(),s.getModality(),s.getStatus(),s.getMeetingLink(),s.getNotes());}}
