package tech.kuiperbelt.lib.common.health;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;


/**
 * Config exclude health indicator from aggregating to application health
 */
@Data
@Component("tech.kuiperbelt.volcano.common.web.HealthAggregateExcludeConfig")
@ConfigurationProperties(prefix = "tech.kuiperbelt.volcano.common.svc.health.aggregate")
public class HealthAggregateExcludeConfig {
    private Set<String> excludes = new HashSet<>();
}
