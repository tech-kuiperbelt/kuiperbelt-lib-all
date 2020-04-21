package tech.kuiperbelt.lib.common.jpa;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;

/**
 * JPA entity ID 生成器
 */
public class LongIdentifierGenerator implements IdentifierGenerator {

    @Override
    public Serializable generate(SharedSessionContractImplementor sharedSessionContractImplementor, Object o) throws HibernateException {
        return SnowflakeIdWorker.getInstance().nextId();
    }
}
