package org.example.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoreSegmentDistribution {
    private String range;   // 分数段范围（如"0-60"）
    private Long count;     // 该分数段人数
    private String rate;    // 该分数段占比（如"15.0%"）
}