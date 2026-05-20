package com.revtalent.ai_service.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScreeningResultDTO {

    private Long candidateId;
    private String name;
    private double score;
    private String aiSummary;
}
