package com.staffcore33.ats.candidate;

import lombok.Data;

@Data
public class DuplicateCheckRequest {
    private String email;
    private String phone;
    private String linkedinUrl;
    private String firstName;
    private String lastName;
    private String city;
}
