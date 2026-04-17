package org.hibernate.hql.internal.ast.tree;

import antlr.collections.AST;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.type.Type;

public abstract class AbstractNullnessCheckNode extends UnaryLogicOperatorNode {
   public void initialize() {
      Type operandType = extractDataType(this.getOperand());
      if (operandType != null) {
         SessionFactoryImplementor sessionFactory = this.getSessionFactoryHelper().getFactory();
         int operandColumnSpan = operandType.getColumnSpan(sessionFactory);
         if (operandColumnSpan > 1) {
            this.mutateRowValueConstructorSyntax(operandColumnSpan);
         }

      }
   }

   protected abstract int getExpansionConnectorType();

   protected abstract String getExpansionConnectorText();

   private void mutateRowValueConstructorSyntax(int operandColumnSpan) {
      int comparisonType = this.getType();
      String comparisonText = this.getText();
      int expansionConnectorType = this.getExpansionConnectorType();
      String expansionConnectorText = this.getExpansionConnectorText();
      this.setType(expansionConnectorType);
      this.setText(expansionConnectorText);
      String[] mutationTexts = extractMutationTexts(this.getOperand(), operandColumnSpan);
      AST container = this;

      for(int i = operandColumnSpan - 1; i > 0; --i) {
         if (i == 1) {
            AST op1 = this.getASTFactory().create(comparisonType, comparisonText);
            AST operand1 = this.getASTFactory().create(142, mutationTexts[0]);
            op1.setFirstChild(operand1);
            container.setFirstChild(op1);
            AST op2 = this.getASTFactory().create(comparisonType, comparisonText);
            AST operand2 = this.getASTFactory().create(142, mutationTexts[1]);
            op2.setFirstChild(operand2);
            op1.setNextSibling(op2);
         } else {
            AST op = this.getASTFactory().create(comparisonType, comparisonText);
            AST operand = this.getASTFactory().create(142, mutationTexts[i]);
            op.setFirstChild(operand);
            AST newContainer = this.getASTFactory().create(expansionConnectorType, expansionConnectorText);
            container.setFirstChild(newContainer);
            newContainer.setNextSibling(op);
            container = newContainer;
         }
      }

   }

   private static Type extractDataType(Node operand) {
      Type type = null;
      if (operand instanceof SqlNode) {
         type = ((SqlNode)operand).getDataType();
      }

      if (type == null && operand instanceof ExpectedTypeAwareNode) {
         type = ((ExpectedTypeAwareNode)operand).getExpectedType();
      }

      return type;
   }

   private static String[] extractMutationTexts(Node operand, int count) {
      if (operand instanceof ParameterNode) {
         String[] rtn = new String[count];

         for(int i = 0; i < count; ++i) {
            rtn[i] = "?";
         }

         return rtn;
      } else if (operand.getType() != 92) {
         if (operand instanceof SqlNode) {
            String nodeText = operand.getText();
            if (nodeText.startsWith("(")) {
               nodeText = nodeText.substring(1);
            }

            if (nodeText.endsWith(")")) {
               nodeText = nodeText.substring(0, nodeText.length() - 1);
            }

            String[] splits = StringHelper.split(", ", nodeText);
            if (count != splits.length) {
               throw new HibernateException("SqlNode's text did not reference expected number of columns");
            } else {
               return splits;
            }
         } else {
            throw new HibernateException("dont know how to extract row value elements from node : " + operand);
         }
      } else {
         String[] rtn = new String[operand.getNumberOfChildren()];
         int x = 0;

         for(AST node = operand.getFirstChild(); node != null; node = node.getNextSibling()) {
            rtn[x++] = node.getText();
         }

         return rtn;
      }
   }
}
