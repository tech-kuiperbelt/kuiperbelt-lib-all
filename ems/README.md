# Kuiperbelt EMS Lib
Based on `Kuiperbelt Common Lib`, and provide capability of entity extension.

# Feature
- Provide META API
    - Get meta info of entity and enum  
    - Extend entity fields
    - Extend event handler when operating entity
    - Extend enum
    
- Enhance Search By Filter
    - Support filter condition on extension fields.

# REST API
- meta:
    - DataType: `/rest/meta/data-types` (`GET`)
    - Entity: `/rest/meta/entities` (`GET`)
    - Field: `/rest/meta/entities/{entity-name}/fields` (`GET`, `POST`, `DELETE`)
    - Event-handler: `/rest/meta/entities/{entity-name}/event-handlers` (`GET`, `POST`, `DELETE`)
    - Enum: `/rest/meta/enums` (`GET`,`POST`,`PUT`)
 
- ems:
    - Root: `/rest/ems`    (`GET`)
    - A Resource: `/rest/ems/{repository-path}` (`GET`,`POST`,`DELETE`,`PATCH`,`PUT`)
    - Search Of OOB: `/rest/ems/{repository-path}/search/{oob-search-name}?paramters` (`GET`)
    - Search By Filter: `/rest/ems/{repository-path}/search/findByFilter?filter=` (`GET`)

# DONE
- RF, metaService api, done
- RF, (auto) configure 
- support for all dataType.
- Enhance entityDescriptor with rest profile entry point, rest entry point, and repository path, 
- DateType add Link
- meta api, support DataType query
- RF metaService, do not persist OOB meta data any more.
- RF, merge emsRepository to BaseRepository, move EmsController to common.datarest
- US, support customize enum definition and validate
    - using cust enum as dataType
    - validate when post, put, patch
- indexed data type supported
- Hateos for meta-api
- UT support

# DOING
- Meta Catch which memory
    - instance life cycle level
        - EmsEntity (Enum)
        - EmsGenericRSQLSpecification   
        - EntityEventTriggerService
    - transaction level
    - @Cache level    
 
# TODO
- Mulititenancy Solution Hibernate filter (POC)
- exception handler



