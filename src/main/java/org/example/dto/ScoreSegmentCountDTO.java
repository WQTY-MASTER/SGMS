package org.example.dto;

import lombok.Data;

/**
 * 成绩分段统计计数DTO
 */
@Data
public class ScoreSegmentCountDTO {
    private Long count0To60;
    private Long count60To80;
    private Long count80To100;
}