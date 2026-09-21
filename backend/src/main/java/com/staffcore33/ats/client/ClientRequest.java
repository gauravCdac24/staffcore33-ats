package com.staffcore33.ats.client;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClientRequest {

    @NotBlank
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
}
