package tech.kuiperbelt.lib.ems;

import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import tech.kuiperbelt.lib.common.jpa.GenericRSQLSpecification;

import javax.persistence.criteria.Path;
import java.util.List;

/**
 * Enhance  GenericRSQLSpecification, 在 Filter 的表达式中 支持扩展字段
 * @param <T>
 */
public class EmsGenericRSQLSpecification<T> extends GenericRSQLSpecification<T> {
    private MetaCache metaCache;

    public EmsGenericRSQLSpecification(String property, ComparisonOperator operator, List arguments) {
        super(property, operator, arguments);
    }

    @Override
    protected Path<T> findProperty(Path<T> root, String property) {
        if(EmsEntity.class.isAssignableFrom(root.getJavaType())) {
            if(metaCache == null) {
                metaCache = MetaService.getInstance()
                        .orElseThrow(() -> new IllegalStateException("metaService is not ready"))
                        .getOrCreateMetaCache((Class<? extends EmsEntity>) root.getJavaType());
            }
            if(metaCache.getFieldDescriptorMap().containsKey(this.getProperty())) {
                FieldDescriptor fieldDescriptor = metaCache.getFieldDescriptorMap().get(this.getProperty());
                if(fieldDescriptor.isExtension()) {
                    return root.get(EmsEntity.Fields.ext)
                            .get(fieldDescriptor.getDomainField());
                }
            }
        }

        return super.findProperty(root, property);
    }
}
