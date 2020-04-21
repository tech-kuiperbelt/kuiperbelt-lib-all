package tech.kuiperbelt.lib.common.web;


import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Inherited
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({KuiperbeltWebAutoConfig.class})
public @interface EnableKuiperbeltWebConfig {
}
