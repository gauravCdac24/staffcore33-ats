package com.staffcore33.ats.tag;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TagRequest {
    @NotBlank
    private String name;
}
