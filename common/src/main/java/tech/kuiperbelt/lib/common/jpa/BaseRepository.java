package tech.kuiperbelt.lib.common.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;


/**
 * Base Repository interface
 * @param <T>
 */
@NoRepositoryBean
public interface BaseRepository<T> extends JpaRepository<T, Long> {


    @RestResource(exported = false)
    default T getById(Long id) {
        return this.findById(id).orElseThrow(() -> new EntityNotFoundException());
    }

    @RestResource(exported = false)
    default Optional<T> findById(@Param("id") String id) {
        return this.findById(Long.parseLong(id));
    }


    Page<T> findByFilter(@Param("filter") String filter, Pageable pageable);

    Page<T> findAllVersions(Long entityId, Pageable pageable);

    Optional<T> findVersion(Long entityId, Long version);

    Optional<T> findPriorVersion(Long entityId);

}
