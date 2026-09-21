package com.staffcore33.ats.client;

import com.staffcore33.ats.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    private String website;
    private String industry;
    private String address;
    private String city;
    private String state;
    private String zip;

    @Column(name = "primary_contact_name")
    private String primaryContactName;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "contact_phone")
    private String contactPhone;

    private String linkedin;

    @Column(name = "account_manager_id")
    private Long accountManagerId;

    @Column(name = "msa_status")
    private String msaStatus;

    @Column(name = "payment_terms")
    private String paymentTerms;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private String status = "ACTIVE";

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
