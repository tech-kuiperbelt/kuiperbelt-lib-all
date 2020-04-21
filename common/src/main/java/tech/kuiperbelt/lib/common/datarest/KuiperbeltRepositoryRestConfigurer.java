package tech.kuiperbelt.lib.common.datarest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.core.mapping.RepositoryDetectionStrategy;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import tech.kuiperbelt.lib.common.jpa.BaseEntity;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;
import java.util.Collections;
import java.util.List;

/**
 * Customized Spring data rest config
 * @return
 */
@Configuration
public class KuiperbeltRepositoryRestConfigurer implements RepositoryRestConfigurer {
    @Autowired
    private EntityManager entityManager;

    @Bean
    public MvcValidatorHandler mvcValidatorHandler() {
        return new MvcValidatorHandler();
    }

    /**
     * Include "Id" in entity representation
     * @param config
     */
    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
        config.setRepositoryDetectionStrategy(RepositoryDetectionStrategy.RepositoryDetectionStrategies.ANNOTATED);

        /**
         *  expose Id for all BaseEntity
         */
        config.exposeIdsFor(
                entityManager.getMetamodel().getEntities().stream()
                        .map(EntityType::getJavaType)
                        .filter(BaseEntity.class::isAssignableFrom)
                        .toArray(Class[]::new));


    }

    protected List<Class> getCannotCreateClass() {
        return Collections.emptyList();
    }
    protected List<Class> getCannotUpdateClass() {
        return Collections.emptyList();
    }
    protected List<Class> getCannotDeleteClass() {
        return Collections.emptyList();
    }

    /**
     * Validate Entity can be operated by HTTP Method
     * @param v
     */
    @Override
    public void configureValidatingRepositoryEventListener(
            ValidatingRepositoryEventListener v) {
        v.addValidator("beforeCreate", CanNotValidator.builder().classes(getCannotCreateClass()).operation(CURDOperation.CREATE).build());
        v.addValidator("beforeSave", CanNotValidator.builder().classes(getCannotUpdateClass()).operation(CURDOperation.UPDATE).build());
        v.addValidator("beforeDelete", CanNotValidator.builder().classes(getCannotDeleteClass()).operation(CURDOperation.DELETE).build());
    }
}