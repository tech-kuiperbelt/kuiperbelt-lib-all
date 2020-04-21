# Kuiperbelt Common Lib
Provide common util Classes and Base Classes for projects which based on Spring Data Rest, JPA and Spring Web Mvc Framework

# FEATURE

- JPA Related
    - Id Generator based on "Twitter_Snowflake"    
    - Customized MySQL Dialect to solve unicode issue

    - BaseEntity
        - Audit fields
        - Equals and HashCode implementation
        
    - BaseRepository: A BaseRepository to provide enhanced functionality of JpaRepository
        - Search By Filter Expression String
        - Search Audited version 
    

- Spring Data Rest Related
    - Wrap event handlers operation in on DB Transaction, and provide Non-Rollback Exception Parent Class
    - Expose `Search By Filter` to REST endpoint for each `Entity` under the path: `{entity-repository}/search/findByFilter`
    - Expose `{entity-repository}/{entity-id}/audits` to REST endpoint for audit records of entity
    - Log operations of `CURD` for each `Entity`
    - Provide configuration to declare forbidden operation for `Entity` 
    
- Web Related    
    - A Ping Controller
    - Base Health Indicator and Aggregator
    - Exception Handler and Error Response
    - Request logging config
    
- Health Related
    - Base Health Indicator
    - Aggregator

# DEVELOPMENT
    
## DONE
- UT

## DOING

## TODO 
- bug: DataType of Float can not work well in `==` operation when using findByFilter   