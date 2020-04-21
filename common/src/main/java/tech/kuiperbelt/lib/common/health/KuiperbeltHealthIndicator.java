package tech.kuiperbelt.lib.common.health;

import org.springframework.boot.actuate.health.HealthIndicator;

/**
 * 表示 healthIndicator, 默认只有 out_of_service Or down 参与总状态 out_of_service 的汇聚
 */
public interface KuiperbeltHealthIndicator extends HealthIndicator {
    default boolean isAggregate() {
        return true;
    }
}
