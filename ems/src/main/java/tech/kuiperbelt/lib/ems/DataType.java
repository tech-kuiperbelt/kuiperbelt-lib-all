package tech.kuiperbelt.lib.ems;

import lombok.Getter;
import lombok.experimental.FieldNameConstants;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * The data type supported by EMS
 */
@FieldNameConstants
@Getter
public enum DataType {
    INT(true, true),
    STRING(true, true),
    BOOLEAN(true, false),
    FLOAT(true, false),
    DATE(true, true),
    DATE_TIME(true, true),

    ENUM(true, true),
    LINK(false, false),

    ;

    DataType(boolean extensible, boolean indexable) {
        this.extensible = extensible;
        this.indexable = indexable;
    }

    /**
     * 是否可以作为扩展列的数据类型
     */
    private boolean extensible;


    /**
     * 作为扩展列是否可以声明索引
     */
    private boolean indexable;

    public static DataType valueOf(Class<?> clazz) {
        assert clazz != null: "clazz can not be null";
        if(String.class == clazz) {
            return STRING;
        } else if(Integer.class == clazz || int.class == clazz || Long.class == clazz || long.class == clazz) {
            return INT;
        } else if (Boolean.class == clazz || boolean.class == clazz) {
            return BOOLEAN;
        } else if(Float.class == clazz || float.class == clazz || Double.class == clazz || double.class == clazz || BigDecimal.class == clazz) {
            return FLOAT;
        } else if(LocalDate.class == clazz) {
            return DATE;
        } else if (LocalDateTime.class == clazz) {
            return DATE_TIME;
        } else if (clazz.isEnum()) {
            return ENUM;
        } else {
            throw new UnsupportedOperationException(clazz.getName() + " is not supported yet");
        }
    }
}
