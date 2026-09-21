package com.staffcore33.ats.pipeline;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PipelineStatusUpdateRequest {

    @NotBlank
    private String status;

    private String notes;
}
