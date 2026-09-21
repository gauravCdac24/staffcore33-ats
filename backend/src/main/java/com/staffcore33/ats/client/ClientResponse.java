package com.staffcore33.ats.client;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ClientResponse {
    private Long id;
    private String companyName;
    private String website;
    private String industry;
    private String address;
    private String city;
    private String state;
    private String zip;
    private String primaryContactName;
    private String contactEmail;
    private String contactPhone;
    private String linkedin;
    private Long accountManagerId;
    private String msaStatus;
    private String paymentTerms;
    private String notes;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}
