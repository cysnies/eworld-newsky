package org.hibernate.hql.internal.ast.tree;

public class IsNotNullLogicOperatorNode extends AbstractNullnessCheckNode {
   protected int getExpansionConnectorType() {
      return 40;
   }

   protected String getExpansionConnectorText() {
      return "OR";
   }
}
