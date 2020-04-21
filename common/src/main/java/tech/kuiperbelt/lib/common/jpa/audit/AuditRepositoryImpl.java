package tech.kuiperbelt.lib.common.jpa.audit;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import java.util.*;

/**
 * The implement of AuditRepository
 * @param <T>
 */
public class AuditRepositoryImpl<T> implements AuditRepository<T> {

    private static final String VERSION = "version";

    private final EntityManager entityManager;

    @Autowired
    public AuditRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }


    @Override
    public Page<T> findAllVersions(Class<T> tClass, Long entityId, Pageable pageable) {

        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        List versionList = auditReader.createQuery()
                .forRevisionsOfEntity(tClass, false, true)
                .add(AuditEntity.id().eq(entityId))
                .addProjection(AuditEntity.revisionNumber())
                .addOrder(AuditEntity.revisionNumber().desc())
                .getResultList();

        if(pageable.getOffset() > versionList.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        long upperBound = Math.min(pageable.getOffset() + pageable.getPageSize(), versionList.size());
        List subList = versionList.subList((int)pageable.getOffset(), (int)upperBound);

        @SuppressWarnings("unchecked") List<T> resultList = (List<T>) auditReader.createQuery()
                .forRevisionsOfEntity(tClass, true, false)
                .add(AuditEntity.id().eq(entityId))
                .add(AuditEntity.revisionNumber().in(subList))
                .addOrder(AuditEntity.revisionNumber().desc())
                .getResultList();

        return new PageImpl<>(new ArrayList<>(resultList), pageable, versionList.size());
    }

    @Override
    public Optional<T> findPriorVersion(Class<T> tClass, Long entityId) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        Number prior = (Number) auditReader.createQuery()
                .forRevisionsOfEntity(tClass, false, true)
                .add(AuditEntity.id().eq(entityId))
                .addProjection(AuditEntity.revisionNumber())
                .addOrder(AuditEntity.revisionNumber().desc())
                .setFirstResult(1)
                .setMaxResults(1)
                .getSingleResult();
        if(prior == null) {
            return Optional.empty();
        } else {
            @SuppressWarnings("unchecked") T singleResult = (T) auditReader.createQuery()
                    .forRevisionsOfEntity(tClass, true, false)
                    .add(AuditEntity.id().eq(entityId))
                    .add(AuditEntity.revisionNumber().eq(prior))
                    .getSingleResult();
            return Optional.of(singleResult);
        }
    }


    /**
     * 找到 entity 的 指定版本
     * @param tClass
     * @param entityId
     * @param version
     * @return
     */
    @Override
    public Optional<T> findVersion(Class<T> tClass, Long entityId, Long version) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        AuditQuery query = auditReader.createQuery().forRevisionsOfEntity(tClass, true, false);
        query.add(AuditEntity.id().eq(entityId));
        query.add(AuditEntity.property(VERSION).eq(version));
        //noinspection unchecked
        return (Optional<T>) query.getResultList().stream().findFirst();
    }
}
