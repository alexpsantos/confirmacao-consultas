package br.com.confirmacao.patient.infrastructure;

import br.com.confirmacao.patient.domain.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PatientRepository extends JpaRepository<Patient, UUID> {
    Page<Patient> findAllByTenant_Id(UUID tenantId, Pageable pageable);
    Optional<Patient> findByIdAndTenant_Id(UUID patientId, UUID tenantId);
    boolean existsByTenant_IdAndPhone(UUID tenantId, String phone);
    boolean existsByTenant_IdAndPhoneAndIdNot(UUID tenantId, String phone, UUID patientId);
}
