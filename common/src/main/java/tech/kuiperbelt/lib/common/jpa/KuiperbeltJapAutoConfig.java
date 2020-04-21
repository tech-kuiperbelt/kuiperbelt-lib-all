package tech.kuiperbelt.lib.common.jpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.validation.Validator;

@EnableJpaAuditing
@Configuration
@Import({SnowflakeIdWorkerConfiguration.class})
@PropertySource("classpath:tech/kuiperbelt/lib/common/jpa/jpa.properties")
public class KuiperbeltJapAutoConfig {
    @Autowired(required = false)
    private Validator validator;

    // 将Spring Validator 配置到JPA中， 这样可以在自定义的Validator中用 Autowired 注入Spring Bean
    @Bean
    @Lazy
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer() {
        return hibernateProperties -> {
            if(validator != null) {
                hibernateProperties.put("javax.persistence.validation.factory", validator);
            }
        };
    }
}
