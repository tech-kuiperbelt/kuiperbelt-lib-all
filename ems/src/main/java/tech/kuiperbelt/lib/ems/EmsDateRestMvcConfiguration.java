package tech.kuiperbelt.lib.ems;


import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.rest.core.support.EntityLookup;
import org.springframework.data.rest.webmvc.config.PersistentEntityResourceHandlerMethodArgumentResolver;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tech.kuiperbelt.lib.common.datarest.EnableKuiperbeltDataRestConfig;
import tech.kuiperbelt.lib.common.datarest.SearchController;
import tech.kuiperbelt.lib.common.jpa.EnableKuiperbeltJapConfig;
import tech.kuiperbelt.lib.common.jpa.audit.EnableKuiperbeltJapAuditConfig;
import tech.kuiperbelt.lib.common.web.EnableKuiperbeltWebConfig;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * EMS 的配置入口
 */
@EnableKuiperbeltWebConfig
@EnableKuiperbeltJapConfig
@EnableKuiperbeltJapAuditConfig
@EnableKuiperbeltDataRestConfig
@EnableJpaRepositories(repositoryBaseClass = EmsRepositoryImplement.class)
@Import({MetaService.class, MetaController.class, SearchController.class, EntityEventTriggerService.class, MetaDelegateRepository.class})
@Configuration
public class EmsDateRestMvcConfiguration extends RepositoryRestMvcConfiguration implements WebMvcConfigurer {
    public EmsDateRestMvcConfiguration(ApplicationContext context,
                                       @Qualifier("mvcConversionService") ObjectFactory<ConversionService> conversionService) {
        super(context, conversionService);
    }

    /**
     * 为 post event 的处理提供单独的ThreadPool
     * @return
     */
    @Bean(name = "entityEventThreadPoolTaskExecutor")
    public Executor eventThreadPoolTaskExecutor() {
        return new ThreadPoolTaskExecutor();
    }


    /**
     * 提供 参数解析
     * @param argumentResolvers
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        super.addArgumentResolvers(argumentResolvers);
        argumentResolvers.addAll(defaultMethodArgumentResolvers());
    }
    /**
     * Reads incoming JSON into an entity. 使用 Spring Date Rest 来提供 参数解析
     *
     * @return
     */
    @Primary
    @Bean
    public PersistentEntityResourceHandlerMethodArgumentResolver persistentEntityArgumentResolver() {

        PluginRegistry<EntityLookup<?>, Class<?>> lookups = PluginRegistry.of(getEntityLookups());

        return new PersistentEntityResourceHandlerMethodArgumentResolver(defaultMessageConverters(),
                repoRequestArgumentResolver(), backendIdHandlerMethodArgumentResolver(),
                new EmsDomainObjectReader(persistentEntities(), associationLinks()), lookups);
    }
}
