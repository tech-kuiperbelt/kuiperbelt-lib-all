package tech.kuiperbelt.lib.common.health;


import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Inherited
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({KuiperbeltHealthAutoConfig.class})
public @interface EnableKuiperbeltHealthConfig {
}
