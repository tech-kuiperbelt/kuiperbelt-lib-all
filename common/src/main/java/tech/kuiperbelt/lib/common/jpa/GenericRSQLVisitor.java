package tech.kuiperbelt.lib.common.jpa;


import cz.jirutka.rsql.parser.ast.*;
import org.springframework.data.jpa.domain.Specification;

/**
 * RSQL 的辅助类
 * RSQL 用来实现通用查寻， 参考资料见：https://github.com/jirutka/rsql-parser
 * 本方案的实现参考： https://www.baeldung.com/rest-api-search-language-rsql-fiql
 * @param <T>
 */
public class GenericRSQLVisitor<T> implements RSQLVisitor<Specification<T>, Void> {
    @Override
    public Specification<T> visit(AndNode andNode, Void aVoid) {
        return visit((LogicalNode)andNode, aVoid);
    }

    @Override
    public Specification<T> visit(OrNode orNode, Void aVoid) {
        return visit((LogicalNode)orNode, aVoid);
    }

    @Override
    public Specification<T> visit(ComparisonNode comparisonNode, Void aVoid) {
        return new GenericRSQLSpecification<>(
                comparisonNode.getSelector(),
                comparisonNode.getOperator(),
                comparisonNode.getArguments()
        );
    }

    private Specification<T> visit(LogicalNode logicalNode, Void aVoid) {
        return logicalNode.getChildren().stream()
                .map(node -> node.accept(this))
                .reduce((specPrevious, specCurrent) -> logicalNode instanceof AndNode ?
                        specPrevious.and(specCurrent) :
                        specPrevious.or(specCurrent))
                .orElseThrow(() -> new RuntimeException("RSQL 逻辑运算符必须包含两个以上的操作元素"));
    }
}
