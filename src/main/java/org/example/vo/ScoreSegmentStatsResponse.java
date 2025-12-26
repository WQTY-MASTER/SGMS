package org.example.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 成绩分段统计响应（兼容前端直接读取顶层字段）
 */
@Data
public class ScoreSegmentStatsResponse {
    private Integer code;
    private String msg;
    private ScoreSegmentStats data;
    private BigDecimal avgScore;
    private BigDecimal maxScore;
    private BigDecimal minScore;
    private Long total;
    private List<ScoreSegmentDistribution> distribution;

    public static ScoreSegmentStatsResponse success(ScoreSegmentStats stats) {
        ScoreSegmentStatsResponse response = new ScoreSegmentStatsResponse();
        response.code = 200;
        response.msg = "操作成功";
        response.data = stats;
        if (stats != null) {
            response.avgScore = stats.getAvgScore();
            response.maxScore = stats.getMaxScore();
            response.minScore = stats.getMinScore();
            response.total = stats.getTotal();
            response.distribution = stats.getDistribution();
        }
        return response;
    }

    public static ScoreSegmentStatsResponse error(String msg) {
        ScoreSegmentStatsResponse response = new ScoreSegmentStatsResponse();
        response.code = 400;
        response.msg = msg;
        return response;
    }

    public static ScoreSegmentStatsResponse forbidden() {
        ScoreSegmentStatsResponse response = new ScoreSegmentStatsResponse();
        response.code = 403;
        response.msg = "权限不足";
        return response;
    }
}
