package com.n1netails.n1netails.zenko.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlertTailRequest {
    private String title;
    private String description;
    private String details;
    private Instant timestamp;
    private String level;
    private String type;
    private Map<String, String> metadata;
}
