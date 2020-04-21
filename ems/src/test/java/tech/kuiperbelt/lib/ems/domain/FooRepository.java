package tech.kuiperbelt.lib.ems.domain;

import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import tech.kuiperbelt.lib.common.jpa.BaseRepository;

import java.util.List;

@RepositoryRestResource
public interface FooRepository extends BaseRepository<Foo> {
    List<Foo> findByName(@Param("name") String name);
}
