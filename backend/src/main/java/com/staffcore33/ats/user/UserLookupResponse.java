package com.staffcore33.ats.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserLookupResponse {
    private Long id;
    private String fullName;
    private UserRole role;
}
