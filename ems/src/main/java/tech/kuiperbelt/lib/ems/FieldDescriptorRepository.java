package tech.kuiperbelt.lib.ems;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource(exported = false)
@Repository
public interface FieldDescriptorRepository extends JpaRepository<FieldDescriptor, Long> {

    List<FieldDescriptor> findByEntity(@Param("entity") String entity);

    Optional<FieldDescriptor> findByEntityAndName(@Param("entity") String entity, @Param("name") String name);

}
