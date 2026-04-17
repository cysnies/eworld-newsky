package org.hibernate.hql.internal.ast.tree;

import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;

public class UnaryLogicOperatorNode extends HqlSqlWalkerNode implements UnaryOperatorNode {
   public Node getOperand() {
      return (Node)this.getFirstChild();
   }

   public void initialize() {
   }

   public Type getDataType() {
      return StandardBasicTypes.BOOLEAN;
   }
}
