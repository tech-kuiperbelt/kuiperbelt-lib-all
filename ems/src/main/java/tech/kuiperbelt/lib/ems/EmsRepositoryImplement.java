package tech.kuiperbelt.lib.ems;

import cz.jirutka.rsql.parser.RSQLParser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.repository.query.Param;
import tech.kuiperbelt.lib.common.jpa.BaseRepositoryImplement;

import javax.persistence.EntityManager;

/**
 * Enhance BaseRepositoryImplement.
 * As a global Base Repository implementation, it should be used as @EnableJpaRepositories(repositoryBaseClass = EmsRepositoryImplement.class)
 * @param <T>
 */
public class EmsRepositoryImplement<T> extends BaseRepositoryImplement<T> {
    public EmsRepositoryImplement(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
    }

    public EmsRepositoryImplement(Class<T> domainClass, EntityManager entityManager) {
        super(domainClass, entityManager);
    }

    @Override
    public Page<T> findByFilter(@Param("filter") String filter, Pageable pageable) {
        Specification accept = new RSQLParser()
                .parse(filter)
                .accept(new EmsGenericRSQLVisitor<>());

        return this.findAll(accept, pageable);
    }
}
