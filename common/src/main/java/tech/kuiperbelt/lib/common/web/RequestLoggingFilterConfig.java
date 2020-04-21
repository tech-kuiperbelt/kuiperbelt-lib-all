package tech.kuiperbelt.lib.common.web;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

/**
 * Config the behaviors of logging Http Request.
 */
@ConditionalOnMissingBean(CommonsRequestLoggingFilter.class)
@Configuration("tech.kuiperbelt.volcano.common.svc.web.RequestLoggingFilterConfig")
public class RequestLoggingFilterConfig {

    @ConfigurationProperties("tech.kuiperbelt.volcano.common.web.commons-request")
    @Bean
    public CommonsRequestLoggingFilter logFilter() {
        CommonsRequestLoggingFilter filter
          = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(10000);
        filter.setIncludeHeaders(false);
        filter.setAfterMessagePrefix("请求数据 : ");
        return filter;
    }
}