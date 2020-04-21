package tech.kuiperbelt.lib.common.datarest;

import org.hibernate.envers.Audited;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.rest.core.mapping.ResourceMappings;
import org.springframework.data.rest.core.mapping.ResourceMetadata;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * 为Entity 增强 Rest API
 * 1 /{repository}/search/findByFilter
 * 2 /{repository}/{entityId}/audits/*
 */
@RepositoryRestController
public class SearchController {

    private static final String SEARCH_BY_FILTER = "/{repository}/search/findByFilter";
    private static final String FIND_ALL_VERSION = "/{repository}/{entityId}/audits";
    private static final String FIND_A_VERSION = "/{repository}/{entityId}/audits/{version}";
    private static final String FIND_PRIOR_VERSION = "/{repository}/{entityId}/audits/prior";

    @Autowired
    Repositories repositories;

    @Autowired
    private ResourceMappings mappings;

    @Transactional
    @ResponseBody
    @RequestMapping(value = SEARCH_BY_FILTER, method = RequestMethod.GET)
    public ResponseEntity<Object> executeSearch(@PathVariable("repository")String repositoryKey,
                                           @RequestParam("filter") String filter, @PageableDefault Pageable pageable,
                                                PersistentEntityResourceAssembler assembler) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Class<?> domainType = findDomainType(repositoryKey);
        Object repository = repositories.getRepositoryFor(domainType)
                .orElseThrow(() -> new ResourceNotFoundException("repository not found with domainType: " + domainType));
        RepositoryInformation repositoryInformation = repositories.getRepositoryInformationFor(domainType)
                .orElseThrow(() -> new ResourceNotFoundException("repositoryInformation not found with domainType: " + domainType));

        String findByFilterMethod = "findByFilter";
        Method method = repositoryInformation.getRepositoryInterface().getMethod(findByFilterMethod, String.class, Pageable.class);
        Page result = (Page) method.invoke(repository, filter, pageable);
        PagedResourcesAssembler pagedResourcesAssembler = new PagedResourcesAssembler(null,null);
        @SuppressWarnings("unchecked") PagedModel pagedModel = pagedResourcesAssembler.toModel(result, assembler);

        return ResponseEntity.ok(pagedModel);
    }

    @Transactional
    @ResponseBody
    @RequestMapping(value = FIND_ALL_VERSION, method = RequestMethod.GET)
    public ResponseEntity<Object> findAllVersions(@PathVariable("repository")String repositoryKey,
                                                  @PathVariable("entityId") Long entityId,
                                                  @PageableDefault Pageable pageable,
                                                  PersistentEntityResourceAssembler assembler) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Class<?> domainType = findDomainType(repositoryKey);
        if(AnnotationUtils.getAnnotation(domainType, Audited.class) == null ) {
            throw new ResourceNotFoundException(domainType + " is not audited yet.");
        }
        Object repository = repositories.getRepositoryFor(domainType)
                .orElseThrow(() -> new ResourceNotFoundException("repository not found with domainType: " + domainType));
        RepositoryInformation repositoryInformation = repositories.getRepositoryInformationFor(domainType)
                .orElseThrow(() -> new ResourceNotFoundException("repositoryInformation not found with domainType: " + domainType));

        String findAllVersionsMethod = "findAllVersions";
        Method method = repositoryInformation.getRepositoryInterface().getMethod(findAllVersionsMethod, Long.class, Pageable.class);
        Page result = (Page) method.invoke(repository, entityId, pageable);
        PagedResourcesAssembler pagedResourcesAssembler = new PagedResourcesAssembler(null,null);
        @SuppressWarnings("unchecked") PagedModel pagedModel = pagedResourcesAssembler.toModel(result, assembler);

        return ResponseEntity.ok(pagedModel);
    }

    @Transactional
    @ResponseBody
    @RequestMapping(value = FIND_A_VERSION, method = RequestMethod.GET)
    public ResponseEntity<?> findVersion(@PathVariable("repository")String repositoryKey,
                                                  @PathVariable("entityId") Long entityId,
                                                  @PathVariable("version") Long version,
                                                  PersistentEntityResourceAssembler assembler) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Class<?> domainType = findDomainType(repositoryKey);
        if(AnnotationUtils.getAnnotation(domainType, Audited.class) == null ) {
            throw new ResourceNotFoundException(domainType + " is not audited yet.");
        }
        Object repository = repositories.getRepositoryFor(domainType)
                .orElseThrow(() -> new ResourceNotFoundException("repository not found with domainType: " + domainType));
        RepositoryInformation repositoryInformation = repositories.getRepositoryInformationFor(domainType)
                .orElseThrow(() -> new ResourceNotFoundException("repositoryInformation not found with domainType: " + domainType));

        String findVersionsMethod = "findVersion";
        Method method = repositoryInformation.getRepositoryInterface().getMethod(findVersionsMethod, Long.class, Long.class);
        Optional<?> result = (Optional<?>) method.invoke(repository, entityId, version);
        if(result.isPresent()) {
            PersistentEntityResource persistentEntityResource = assembler.toModel(result.get());
            return ResponseEntity.ok(persistentEntityResource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Transactional
    @ResponseBody
    @RequestMapping(value = FIND_PRIOR_VERSION, method = RequestMethod.GET)
    public ResponseEntity<?> findPriorVersion(@PathVariable("repository")String repositoryKey,
                                         @PathVariable("entityId") Long entityId,
                                         PersistentEntityResourceAssembler assembler) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Class<?> domainType = findDomainType(repositoryKey);
        if(AnnotationUtils.getAnnotation(domainType, Audited.class) == null ) {
            throw new ResourceNotFoundException(domainType + " is not audited yet.");
        }
        Object repository = repositories.getRepositoryFor(domainType)
                .orElseThrow(() -> new ResourceNotFoundException("repository not found with domainType: " + domainType));
        RepositoryInformation repositoryInformation = repositories.getRepositoryInformationFor(domainType)
                .orElseThrow(() -> new ResourceNotFoundException("repositoryInformation not found with domainType: " + domainType));

        String findPriorVersionsMethod = "findPriorVersion";
        Method method = repositoryInformation.getRepositoryInterface().getMethod(findPriorVersionsMethod, Long.class);
        Optional<?> result = (Optional<?>) method.invoke(repository, entityId);
        if(result.isPresent()) {
            PersistentEntityResource persistentEntityResource = assembler.toModel(result.get());
            return ResponseEntity.ok(persistentEntityResource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private Class<?> findDomainType(String repositoryKey) {
        for (Class<?> domainType : repositories) {
            ResourceMetadata m = mappings.getMetadataFor(domainType);
            if (m.getPath().matches(repositoryKey) && m.isExported()) {
                return domainType;
            }
        }
        throw new ResourceNotFoundException("domainType not found with: " + repositoryKey);
    }

}
