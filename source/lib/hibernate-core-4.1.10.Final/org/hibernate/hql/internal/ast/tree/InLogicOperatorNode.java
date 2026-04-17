package org.hibernate.hql.internal.ast.tree;

import antlr.SemanticException;
import antlr.collections.AST;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.param.ParameterSpecification;
import org.hibernate.type.Type;

public class InLogicOperatorNode extends BinaryLogicOperatorNode implements BinaryOperatorNode {
   public Node getInList() {
      return this.getRightHandOperand();
   }

   public void initialize() throws SemanticException {
      Node lhs = this.getLeftHandOperand();
      if (lhs == null) {
         throw new SemanticException("left-hand operand of in operator was null");
      } else {
         Node inList = this.getInList();
         if (inList == null) {
            throw new SemanticException("right-hand operand of in operator was null");
         } else {
            if (SqlNode.class.isAssignableFrom(lhs.getClass())) {
               Type lhsType = ((SqlNode)lhs).getDataType();

               for(AST inListChild = inList.getFirstChild(); inListChild != null; inListChild = inListChild.getNextSibling()) {
                  if (ExpectedTypeAwareNode.class.isAssignableFrom(inListChild.getClass())) {
                     ((ExpectedTypeAwareNode)inListChild).setExpectedType(lhsType);
                  }
               }
            }

            SessionFactoryImplementor sessionFactory = this.getSessionFactoryHelper().getFactory();
            if (!sessionFactory.getDialect().supportsRowValueConstructorSyntaxInInList()) {
               Type lhsType = this.extractDataType(lhs);
               if (lhsType != null) {
                  int lhsColumnSpan = lhsType.getColumnSpan(sessionFactory);
                  Node rhsNode = (Node)inList.getFirstChild();
                  if (this.isNodeAcceptable(rhsNode)) {
                     int rhsColumnSpan = 0;
                     if (rhsNode != null) {
                        if (rhsNode.getType() == 92) {
                           rhsColumnSpan = rhsNode.getNumberOfChildren();
                        } else {
                           Type rhsType = this.extractDataType(rhsNode);
                           if (rhsType == null) {
                              return;
                           }

                           rhsColumnSpan = rhsType.getColumnSpan(sessionFactory);
                        }

                        if (lhsColumnSpan > 1 && rhsColumnSpan > 1) {
                           this.mutateRowValueConstructorSyntaxInInListSyntax(lhsColumnSpan, rhsColumnSpan);
                        }

                     }
                  }
               }
            }
         }
      }
   }

   private boolean isNodeAcceptable(Node rhsNode) {
      return rhsNode == null || rhsNode instanceof LiteralNode || rhsNode instanceof ParameterNode || rhsNode.getType() == 92;
   }

   private void mutateRowValueConstructorSyntaxInInListSyntax(int lhsColumnSpan, int rhsColumnSpan) {
      String[] lhsElementTexts = extractMutationTexts(this.getLeftHandOperand(), lhsColumnSpan);
      Node rhsNode = (Node)this.getInList().getFirstChild();
      ParameterSpecification lhsEmbeddedCompositeParameterSpecification = this.getLeftHandOperand() != null && ParameterNode.class.isInstance(this.getLeftHandOperand()) ? ((ParameterNode)this.getLeftHandOperand()).getHqlParameterSpecification() : null;
      boolean negated = this.getType() == 83;
      if (rhsNode != null && rhsNode.getNextSibling() == null) {
         String[] rhsElementTexts = extractMutationTexts(rhsNode, rhsColumnSpan);
         this.setType(negated ? 40 : 6);
         this.setText(negated ? "or" : "and");
         ParameterSpecification rhsEmbeddedCompositeParameterSpecification = rhsNode != null && ParameterNode.class.isInstance(rhsNode) ? ((ParameterNode)rhsNode).getHqlParameterSpecification() : null;
         this.translate(lhsColumnSpan, negated ? 108 : 102, negated ? "<>" : "=", lhsElementTexts, rhsElementTexts, lhsEmbeddedCompositeParameterSpecification, rhsEmbeddedCompositeParameterSpecification, this);
      } else {
         List andElementsNodeList;
         for(andElementsNodeList = new ArrayList(); rhsNode != null; rhsNode = (Node)rhsNode.getNextSibling()) {
            String[] rhsElementTexts = extractMutationTexts(rhsNode, rhsColumnSpan);
            AST group = this.getASTFactory().create(negated ? 40 : 6, negated ? "or" : "and");
            ParameterSpecification rhsEmbeddedCompositeParameterSpecification = rhsNode != null && ParameterNode.class.isInstance(rhsNode) ? ((ParameterNode)rhsNode).getHqlParameterSpecification() : null;
            this.translate(lhsColumnSpan, negated ? 108 : 102, negated ? "<>" : "=", lhsElementTexts, rhsElementTexts, lhsEmbeddedCompositeParameterSpecification, rhsEmbeddedCompositeParameterSpecification, group);
            andElementsNodeList.add(group);
         }

         this.setType(negated ? 6 : 40);
         this.setText(negated ? "and" : "or");
         AST curNode = this;

         for(int i = andElementsNodeList.size() - 1; i > 1; --i) {
            AST group = this.getASTFactory().create(negated ? 6 : 40, negated ? "and" : "or");
            curNode.setFirstChild(group);
            curNode = group;
            AST and = (AST)andElementsNodeList.get(i);
            group.setNextSibling(and);
         }

         AST node0 = (AST)andElementsNodeList.get(0);
         AST node1 = (AST)andElementsNodeList.get(1);
         node0.setNextSibling(node1);
         curNode.setFirstChild(node0);
      }

   }
}
