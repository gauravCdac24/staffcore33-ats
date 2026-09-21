package com.staffcore33.ats.tag;

import lombok.Data;

import java.util.List;

@Data
public class AssignTagsRequest {
    private List<Long> tagIds;
    private List<String> tagNames;
}
