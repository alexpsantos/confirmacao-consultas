package br.com.confirmacao.patient.application;

import br.com.confirmacao.patient.domain.Patient;
import br.com.confirmacao.patient.domain.PreferredContactChannel;
import br.com.confirmacao.patient.infrastructure.PatientRepository;
import br.com.confirmacao.tenant.application.TenantInactiveException;
import br.com.confirmacao.tenant.application.TenantNotFoundException;
import br.com.confirmacao.tenant.domain.Tenant;
import br.com.confirmacao.tenant.infrastructure.TenantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class PatientService {
    private final PatientRepository patientRepository;
    private final TenantRepository tenantRepository;

    public PatientService(PatientRepository patientRepository, TenantRepository tenantRepository) {
        this.patientRepository = patientRepository;
        this.tenantRepository = tenantRepository;
    }

    @Transactional
    public Patient create(UUID tenantId, String fullName, LocalDate birthDate, String phone,
                          String email, PreferredContactChannel preferredChannel) {
        Tenant tenant = findActiveTenantOrThrow(tenantId);
        if (patientRepository.existsByTenant_IdAndPhone(tenantId, normalizePhone(phone)))
            throw new PatientAlreadyExistsException();
        return patientRepository.save(new Patient(tenant, fullName, birthDate, phone, email,
                preferredChannel == null ? PreferredContactChannel.WHATSAPP : preferredChannel));
    }

    @Transactional(readOnly = true)
    public Page<Patient> findAll(UUID tenantId, Pageable pageable) {
        findActiveTenantOrThrow(tenantId);
        return patientRepository.findAllByTenant_Id(tenantId, pageable);
    }

    @Transactional(readOnly = true)
    public Patient findById(UUID tenantId, UUID patientId) { return findPatientOrThrow(tenantId, patientId); }

    @Transactional
    public Patient update(UUID tenantId, UUID patientId, String fullName, LocalDate birthDate,
                          String phone, String email, PreferredContactChannel preferredChannel) {
        Patient patient = findPatientOrThrow(tenantId, patientId);
        if (patientRepository.existsByTenant_IdAndPhoneAndIdNot(tenantId, normalizePhone(phone), patientId))
            throw new PatientAlreadyExistsException();
        patient.update(fullName, birthDate, phone, email, preferredChannel);
        return patient;
    }

    @Transactional public void deactivate(UUID tenantId, UUID patientId) { findPatientOrThrow(tenantId, patientId).deactivate(); }
    @Transactional public void activate(UUID tenantId, UUID patientId) { findPatientOrThrow(tenantId, patientId).activate(); }
    @Transactional public Patient grantConsent(UUID tenantId, UUID patientId) { Patient p = findPatientOrThrow(tenantId, patientId); p.grantConsent(); return p; }
    @Transactional public Patient revokeConsent(UUID tenantId, UUID patientId) { Patient p = findPatientOrThrow(tenantId, patientId); p.revokeConsent(); return p; }

    private Patient findPatientOrThrow(UUID tenantId, UUID patientId) {
        findActiveTenantOrThrow(tenantId);
        return patientRepository.findByIdAndTenant_Id(patientId, tenantId)
                .orElseThrow(() -> new PatientNotFoundException(patientId));
    }

    private Tenant findActiveTenantOrThrow(UUID tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId).orElseThrow(() -> new TenantNotFoundException(tenantId));
        if (!tenant.isActive()) throw new TenantInactiveException(tenantId);
        return tenant;
    }

    private String normalizePhone(String phone) { return phone == null ? null : phone.replaceAll("\\D", ""); }
}
