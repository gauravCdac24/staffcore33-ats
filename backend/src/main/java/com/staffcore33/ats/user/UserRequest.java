package com.staffcore33.ats.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserRequest {
    @NotBlank
    private String fullName;

    @NotBlank
    @Email
    private String email;

    private String phone;

    @NotNull
    private UserRole role;

    private String password;

    private Boolean active;
}
