package tech.kuiperbelt.lib.common.jpa.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.Optional;

/**
 * Audit API
 * @param <T>
 */
public interface AuditRepository<T> {
    /**
     * 找到指定 entity 的所有 change log
     * @param tClass
     * @param entityId
     * @return
     */
    @RestResource(exported = false)
    Page<T> findAllVersions(Class<T> tClass, Long entityId, Pageable pageable);


    /**
     *
     * @param tClass
     * @param entityId
     * @return
     */
    Optional<T> findPriorVersion(Class<T> tClass, Long entityId);

    /**
     * 找到 entity 的 指定版本
     * @param tClass
     * @param entityId
     * @param version
     * @return
     */
    @RestResource(exported = false)
    Optional<T> findVersion(Class<T> tClass, Long entityId, Long version);

}
