package tech.kuiperbelt.lib.ems;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@RepositoryRestResource(exported = false)
@Repository
public interface EnumDescriptorRepository extends JpaRepository<EnumDescriptor, Long> {

    List<EnumDescriptor> findAll();

    Optional<EnumDescriptor> findByName(String enumName);

}
