package tech.kuiperbelt.lib.common.jpa;

import org.hibernate.dialect.MySQL5Dialect;

/**
 * MySQL5 方言类
 */
public class MySQL5Utf8mb4Dialect extends MySQL5Dialect {
    @Override
    public String getTableTypeString() {
        return "DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci";
    }
}
