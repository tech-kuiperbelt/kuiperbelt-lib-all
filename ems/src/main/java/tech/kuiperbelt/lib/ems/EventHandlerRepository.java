package tech.kuiperbelt.lib.ems;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource(exported = false)
@Repository
public interface EventHandlerRepository extends JpaRepository<EventHandler, Long> {

    List<EventHandler> findByEntity(String entity);

    Optional<EventHandler> findByEntityAndName(String entity, String name);
}

