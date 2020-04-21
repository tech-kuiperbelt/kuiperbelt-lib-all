package tech.kuiperbelt.lib.ems;


import cz.jirutka.rsql.parser.ast.ComparisonNode;
import org.springframework.data.jpa.domain.Specification;
import tech.kuiperbelt.lib.common.jpa.GenericRSQLVisitor;

/**
 * Enhance  GenericRSQLVisitor, 在 Filter 的表达式中 支持扩展字段
 * @param <T>
 */
public class EmsGenericRSQLVisitor<T> extends GenericRSQLVisitor<T> {

    @Override
    public Specification<T> visit(ComparisonNode comparisonNode, Void aVoid) {
        return new EmsGenericRSQLSpecification<>(
                comparisonNode.getSelector(),
                comparisonNode.getOperator(),
                comparisonNode.getArguments()
        );
    }
}
