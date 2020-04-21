package tech.kuiperbelt.lib.common.jpa;

import cz.jirutka.rsql.parser.RSQLParser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.query.Param;
import tech.kuiperbelt.lib.common.jpa.audit.AuditRepository;
import tech.kuiperbelt.lib.common.jpa.audit.AuditRepositoryImpl;

import javax.persistence.EntityManager;
import java.util.Optional;

/**
 * Base Repository implementation.
 * As global setting, it should be used as @EnableJpaRepositories(repositoryBaseClass = BaseRepositoryImplement.class)
 * @param <T>
 */
public class BaseRepositoryImplement<T> extends SimpleJpaRepository<T, Long>  implements BaseRepository<T> {

    private AuditRepository auditRepository;

    public BaseRepositoryImplement(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        auditRepository = new AuditRepositoryImpl(entityManager);
    }

    public BaseRepositoryImplement(Class<T> domainClass, EntityManager entityManager) {
        super(domainClass, entityManager);
        auditRepository = new AuditRepositoryImpl(entityManager);
    }

    @Override
    public Page<T> findByFilter(@Param("filter") String filter, Pageable pageable) {
        Specification accept = new RSQLParser()
                .parse(filter)
                .accept(new GenericRSQLVisitor<>());

        //noinspection unchecked
        return this.findAll(accept, pageable);
    }

    /**
     * 找到指定 entity 的所有 change log
     * @param entityId
     * @return
     */
    @Override
    public Page<T> findAllVersions(Long entityId, Pageable pageable) {
        //noinspection unchecked
        return auditRepository.findAllVersions(getDomainClass(), entityId, pageable);
    }

    /**
     * 找到 entity 的 指定版本
     * @param entityId
     * @param version
     * @return
     */
    @Override
    public Optional<T> findVersion(Long entityId, Long version) {
        //noinspection unchecked
        return auditRepository.findVersion(getDomainClass(), entityId, version);
    }

    /**
     * 找到 entity 的 前一个版本
     * @param entityId
     * @return
     */
    @Override
    public Optional<T> findPriorVersion(Long entityId) {
        //noinspection unchecked
        return auditRepository.findPriorVersion(getDomainClass(), entityId);
    }
}
