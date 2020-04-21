package tech.kuiperbelt.lib.common.jpa;


import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Inherited
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({KuiperbeltJapAutoConfig.class})
public @interface EnableKuiperbeltJapConfig {
}
