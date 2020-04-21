package tech.kuiperbelt.lib.common.web;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnProperty(name = "tech.kuiperbelt.volcano.common.web.autoconfig.enable", havingValue = "true",matchIfMissing = true)
@Import({ExceptionHandlerAdvice.class, RequestLoggingFilterConfig.class, PingController.class})
public class KuiperbeltWebAutoConfig {
}
