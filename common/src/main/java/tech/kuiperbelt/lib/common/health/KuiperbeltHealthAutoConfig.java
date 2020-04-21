package tech.kuiperbelt.lib.common.health;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import tech.kuiperbelt.lib.common.web.ExceptionHandlerAdvice;
import tech.kuiperbelt.lib.common.web.PingController;
import tech.kuiperbelt.lib.common.web.RequestLoggingFilterConfig;

@Configuration
@ConditionalOnProperty(name = "tech.kuiperbelt.volcano.common.web.autoconfig.enable", havingValue = "true",matchIfMissing = true)
@Import({KuiperbeltHealthAggregator.class, HealthAggregateExcludeConfig.class})
public class KuiperbeltHealthAutoConfig {
}
