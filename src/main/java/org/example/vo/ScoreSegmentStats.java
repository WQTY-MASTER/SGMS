package org.example.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoreSegmentStats {
    private BigDecimal avgScore;                // 课程平均分（高精度，保留1位小数）
    private BigDecimal maxScore;                // 课程最高分
    private BigDecimal minScore;                // 课程最低分
    private Long total;                         // 参与统计的总人数
    private List<ScoreSegmentDistribution> distribution; // 各分数段分布详情
}