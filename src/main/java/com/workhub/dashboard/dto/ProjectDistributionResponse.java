package com.workhub.dashboard.dto;

import com.workhub.projectNode.entity.NodeCategory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public record ProjectDistributionResponse(
        Long totalInProgressProjectCount,
        List<NodeCategoryDistribution> distributions
) {

    public static ProjectDistributionResponse of(Long totalInProgressProjectCount,
                                                 List<NodeCategoryDistribution> distributions) {
        return new ProjectDistributionResponse(totalInProgressProjectCount, distributions);
    }

    public record NodeCategoryDistribution(
            NodeCategory nodeCategory,
            Long totalNodes,
            Long completedNodes,
            Double completionRate
    ) {
        public static NodeCategoryDistribution of(NodeCategory nodeCategory,
                                                  Long totalNodes,
                                                  Long completedNodes) {
            return new NodeCategoryDistribution(
                    nodeCategory,
                    normalize(totalNodes),
                    normalize(completedNodes),
                    calculateRate(totalNodes, completedNodes)
            );
        }

        private static long normalize(Long value) {
            return value == null ? 0L : value;
        }

        private static double calculateRate(Long totalNodes, Long completedNodes) {
            long total = normalize(totalNodes);
            long completed = normalize(completedNodes);
            if (total == 0L) {
                return 0.0d;
            }

            BigDecimal rate = BigDecimal.valueOf(completed)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
            return rate.doubleValue();
        }
    }
}
