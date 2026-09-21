package com.staffcore33.ats.auth;

import com.staffcore33.ats.user.UserRole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String token;
    private Long userId;
    private String email;
    private String fullName;
    private UserRole role;
}
