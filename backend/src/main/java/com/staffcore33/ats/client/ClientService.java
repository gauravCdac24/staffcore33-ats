package com.staffcore33.ats.client;

import com.staffcore33.ats.audit.AuditService;
import com.staffcore33.ats.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<ClientResponse> list(String status, String industry, Long accountManagerId, String search) {
        return clientRepository.findFiltered(emptyToNull(status), emptyToNull(industry), accountManagerId, emptyToNull(search))
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ClientResponse> listDeleted() {
        return clientRepository.findAllDeleted().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ClientResponse get(Long id) {
        return toResponse(findActive(id));
    }

    @Transactional
    public ClientResponse create(ClientRequest request) {
        Client client = Client.builder()
                .companyName(request.getCompanyName().trim())
                .website(request.getWebsite())
                .industry(request.getIndustry())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .zip(request.getZip())
                .primaryContactName(request.getPrimaryContactName())
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                .linkedin(request.getLinkedin())
                .accountManagerId(request.getAccountManagerId())
                .msaStatus(request.getMsaStatus())
                .paymentTerms(request.getPaymentTerms())
                .notes(request.getNotes())
                .status(request.getStatus() == null || request.getStatus().isBlank() ? "ACTIVE" : request.getStatus())
                .build();
        client = clientRepository.save(client);
        auditService.log("CLIENT_CREATED", "CLIENT", client.getId(), client.getCompanyName());
        return toResponse(client);
    }

    @Transactional
    public ClientResponse update(Long id, ClientRequest request) {
        Client client = findActive(id);
        client.setCompanyName(request.getCompanyName().trim());
        client.setWebsite(request.getWebsite());
        client.setIndustry(request.getIndustry());
        client.setAddress(request.getAddress());
        client.setCity(request.getCity());
        client.setState(request.getState());
        client.setZip(request.getZip());
        client.setPrimaryContactName(request.getPrimaryContactName());
        client.setContactEmail(request.getContactEmail());
        client.setContactPhone(request.getContactPhone());
        client.setLinkedin(request.getLinkedin());
        client.setAccountManagerId(request.getAccountManagerId());
        client.setMsaStatus(request.getMsaStatus());
        client.setPaymentTerms(request.getPaymentTerms());
        client.setNotes(request.getNotes());
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            client.setStatus(request.getStatus());
        }
        auditService.log("CLIENT_UPDATED", "CLIENT", client.getId(), client.getCompanyName());
        return toResponse(client);
    }

    @Transactional
    public void softDelete(Long id) {
        Client client = findActive(id);
        client.setDeletedAt(Instant.now());
        client.setStatus("INACTIVE");
        auditService.log("CLIENT_DELETED", "CLIENT", client.getId(), client.getCompanyName());
    }

    @Transactional
    public ClientResponse restore(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));
        client.setDeletedAt(null);
        if ("INACTIVE".equals(client.getStatus())) {
            client.setStatus("ACTIVE");
        }
        auditService.log("CLIENT_RESTORED", "CLIENT", client.getId(), client.getCompanyName());
        return toResponse(client);
    }

    public Client findActive(Long id) {
        return clientRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));
    }

    private ClientResponse toResponse(Client c) {
        return ClientResponse.builder()
                .id(c.getId())
                .companyName(c.getCompanyName())
                .website(c.getWebsite())
                .industry(c.getIndustry())
                .address(c.getAddress())
                .city(c.getCity())
                .state(c.getState())
                .zip(c.getZip())
                .primaryContactName(c.getPrimaryContactName())
                .contactEmail(c.getContactEmail())
                .contactPhone(c.getContactPhone())
                .linkedin(c.getLinkedin())
                .accountManagerId(c.getAccountManagerId())
                .msaStatus(c.getMsaStatus())
                .paymentTerms(c.getPaymentTerms())
                .notes(c.getNotes())
                .status(c.getStatus())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
