package org.hibernate.hql.internal.ast.tree;

import antlr.SemanticException;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;

public class BetweenOperatorNode extends SqlNode implements OperatorNode {
   public void initialize() throws SemanticException {
      Node fixture = this.getFixtureOperand();
      if (fixture == null) {
         throw new SemanticException("fixture operand of a between operator was null");
      } else {
         Node low = this.getLowOperand();
         if (low == null) {
            throw new SemanticException("low operand of a between operator was null");
         } else {
            Node high = this.getHighOperand();
            if (high == null) {
               throw new SemanticException("high operand of a between operator was null");
            } else {
               this.check(fixture, low, high);
               this.check(low, high, fixture);
               this.check(high, fixture, low);
            }
         }
      }
   }

   public Type getDataType() {
      return StandardBasicTypes.BOOLEAN;
   }

   public Node getFixtureOperand() {
      return (Node)this.getFirstChild();
   }

   public Node getLowOperand() {
      return (Node)this.getFirstChild().getNextSibling();
   }

   public Node getHighOperand() {
      return (Node)this.getFirstChild().getNextSibling().getNextSibling();
   }

   private void check(Node check, Node first, Node second) {
      if (ExpectedTypeAwareNode.class.isAssignableFrom(check.getClass())) {
         Type expectedType = null;
         if (SqlNode.class.isAssignableFrom(first.getClass())) {
            expectedType = ((SqlNode)first).getDataType();
         }

         if (expectedType == null && SqlNode.class.isAssignableFrom(second.getClass())) {
            expectedType = ((SqlNode)second).getDataType();
         }

         ((ExpectedTypeAwareNode)check).setExpectedType(expectedType);
      }

   }
}
