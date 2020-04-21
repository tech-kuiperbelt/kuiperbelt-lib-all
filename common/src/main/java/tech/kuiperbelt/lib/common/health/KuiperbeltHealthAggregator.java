package tech.kuiperbelt.lib.common.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthAggregator;
import org.springframework.boot.actuate.health.OrderedHealthAggregator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Aggregate strategy Third-party health indicator
 */
@Slf4j
@Component
public class KuiperbeltHealthAggregator implements HealthAggregator, BeanPostProcessor {
    @Autowired
    private HealthAggregateExcludeConfig config;

    private OrderedHealthAggregator orderedHealthAggregator = new OrderedHealthAggregator();

    private Map<String, Boolean> xwHealthMap = new HashMap<>();

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if(bean instanceof KuiperbeltHealthIndicator) {
            xwHealthMap.put(beanName, ((KuiperbeltHealthIndicator) bean).isAggregate());
        }
        return bean;
    }

    @Override
    public Health aggregate(Map<String, Health> healths) {
        Map<String, Health> xwHealths = new HashMap<>();
        Map<String, Health> excludeHealths = new HashMap<>();
        Map<String, Health> systemHealths = new HashMap<>();
        for(String name: healths.keySet()) {
            if(xwHealthMap.get(name) != null && xwHealthMap.get(name)) {
                xwHealths.put(name, healths.get(name));
            } else if(config.getExcludes().contains(name) ||
                    xwHealthMap.get(name) != null && !xwHealthMap.get(name)) {
                excludeHealths.put(name, healths.get(name));
            } else {
                systemHealths.put(name, healths.get(name));
            }
        }
        log.debug("总共的 health 有:{}, excludes 列表为：{}，xwHealth 有:{}, 系统 Health 有：{}, excludeHealths 有：{}",
                healths.keySet(),
                config.getExcludes(),
                xwHealths.keySet(),
                systemHealths.keySet(),
                excludeHealths.keySet());

        Map<String, Health> details = new HashMap<>();
        details.putAll(xwHealths);
        details.putAll(systemHealths);
        details.putAll(excludeHealths);

        Health system = orderedHealthAggregator.aggregate(systemHealths);
        log.debug("系统 Health 聚合状态:{}", system.getStatus());

        Health xw = aggregateXwHealth(xwHealths);
        log.debug("xwHealth 聚合状态:{}", xw.getStatus());

        // 决定最终状态
        Status finalStatus;
        if(Objects.equals(Status.DOWN, system.getStatus())) {
            finalStatus = Status.DOWN;
        } else if (Objects.equals(Status.OUT_OF_SERVICE, xw.getStatus())) {
            finalStatus = Status.OUT_OF_SERVICE;
        } else {
            finalStatus = system.getStatus();
        }

        log.debug("最终的 health 状态:{}", finalStatus);
        return (new Health.Builder(finalStatus, details)).build();
    }

    private Health aggregateXwHealth(Map<String, Health> xwHealths) {
        for (Health health: xwHealths.values()) {
            if (Objects.equals(Status.OUT_OF_SERVICE, health.getStatus()) || Objects.equals(Status.DOWN,
                    health.getStatus())) {
                return Health.status(Status.OUT_OF_SERVICE).build();
            }
        }
        return Health.up().build();
    }
}
