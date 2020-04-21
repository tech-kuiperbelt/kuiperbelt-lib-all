package tech.kuiperbelt.lib.ems;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于 OOB Field  Override 默认的Meta 数据
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldMeta {
    String label();
    DataType dataType() ;
    boolean indexed();
}
