package org.hibernate.hql.internal.ast.tree;

public interface BinaryOperatorNode extends OperatorNode {
   Node getLeftHandOperand();

   Node getRightHandOperand();
}
