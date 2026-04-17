package org.hibernate.hql.internal.ast.tree;

import java.util.List;
import org.hibernate.transform.ResultTransformer;

public interface AggregatedSelectExpression extends SelectExpression {
   List getAggregatedSelectionTypeList();

   String[] getAggregatedAliases();

   ResultTransformer getResultTransformer();

   Class getAggregationResultType();
}
