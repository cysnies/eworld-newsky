package org.hibernate.hql.internal.ast.tree;

public class IsNullLogicOperatorNode extends AbstractNullnessCheckNode {
   protected int getExpansionConnectorType() {
      return 6;
   }

   protected String getExpansionConnectorText() {
      return "AND";
   }
}
