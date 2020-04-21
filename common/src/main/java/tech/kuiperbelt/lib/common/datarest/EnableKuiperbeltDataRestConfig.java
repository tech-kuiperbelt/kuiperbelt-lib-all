package tech.kuiperbelt.lib.common.datarest;


import org.springframework.context.annotation.Import;
import tech.kuiperbelt.lib.common.jpa.KuiperbeltJapAutoConfig;

import java.lang.annotation.*;

@Inherited
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({KuiperbeltDataRestAutoConfig.class})
public @interface EnableKuiperbeltDataRestConfig {
}
