package br.com.confirmacao.professional.location.api;
import br.com.confirmacao.professional.location.domain.ProfessionalLocation;
public record ProfessionalLocationResponse(String city,String state,String timezone){public static ProfessionalLocationResponse from(ProfessionalLocation value){return new ProfessionalLocationResponse(value.getCity(),value.getState(),value.getTimezone());}}
