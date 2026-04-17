package org.hibernate.hql.internal.ast.tree;

import antlr.SemanticException;
import antlr.collections.AST;
import java.util.Arrays;
import org.hibernate.HibernateException;
import org.hibernate.TypeMismatchException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.param.ParameterSpecification;
import org.hibernate.type.OneToOneType;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;

public class BinaryLogicOperatorNode extends HqlSqlWalkerNode implements BinaryOperatorNode {
   public void initialize() throws SemanticException {
      Node lhs = this.getLeftHandOperand();
      if (lhs == null) {
         throw new SemanticException("left-hand operand of a binary operator was null");
      } else {
         Node rhs = this.getRightHandOperand();
         if (rhs == null) {
            throw new SemanticException("right-hand operand of a binary operator was null");
         } else {
            Type lhsType = this.extractDataType(lhs);
            Type rhsType = this.extractDataType(rhs);
            if (lhsType == null) {
               lhsType = rhsType;
            }

            if (rhsType == null) {
               rhsType = lhsType;
            }

            if (ExpectedTypeAwareNode.class.isAssignableFrom(lhs.getClass())) {
               ((ExpectedTypeAwareNode)lhs).setExpectedType(rhsType);
            }

            if (ExpectedTypeAwareNode.class.isAssignableFrom(rhs.getClass())) {
               ((ExpectedTypeAwareNode)rhs).setExpectedType(lhsType);
            }

            this.mutateRowValueConstructorSyntaxesIfNecessary(lhsType, rhsType);
         }
      }
   }

   protected final void mutateRowValueConstructorSyntaxesIfNecessary(Type lhsType, Type rhsType) {
      SessionFactoryImplementor sessionFactory = this.getSessionFactoryHelper().getFactory();
      if (lhsType != null && rhsType != null) {
         int lhsColumnSpan = this.getColumnSpan(lhsType, sessionFactory);
         if (lhsColumnSpan != this.getColumnSpan(rhsType, sessionFactory)) {
            throw new TypeMismatchException("left and right hand sides of a binary logic operator were incompatibile [" + lhsType.getName() + " : " + rhsType.getName() + "]");
         }

         if (lhsColumnSpan > 1 && !sessionFactory.getDialect().supportsRowValueConstructorSyntax()) {
            this.mutateRowValueConstructorSyntax(lhsColumnSpan);
         }
      }

   }

   private int getColumnSpan(Type type, SessionFactoryImplementor sfi) {
      int columnSpan = type.getColumnSpan(sfi);
      if (columnSpan == 0 && type instanceof OneToOneType) {
         columnSpan = ((OneToOneType)type).getIdentifierOrUniqueKeyType(sfi).getColumnSpan(sfi);
      }

      return columnSpan;
   }

   private void mutateRowValueConstructorSyntax(int valueElements) {
      int comparisonType = this.getType();
      String comparisonText = this.getText();
      this.setType(6);
      this.setText("AND");
      String[] lhsElementTexts = extractMutationTexts(this.getLeftHandOperand(), valueElements);
      String[] rhsElementTexts = extractMutationTexts(this.getRightHandOperand(), valueElements);
      ParameterSpecification lhsEmbeddedCompositeParameterSpecification = this.getLeftHandOperand() != null && ParameterNode.class.isInstance(this.getLeftHandOperand()) ? ((ParameterNode)this.getLeftHandOperand()).getHqlParameterSpecification() : null;
      ParameterSpecification rhsEmbeddedCompositeParameterSpecification = this.getRightHandOperand() != null && ParameterNode.class.isInstance(this.getRightHandOperand()) ? ((ParameterNode)this.getRightHandOperand()).getHqlParameterSpecification() : null;
      this.translate(valueElements, comparisonType, comparisonText, lhsElementTexts, rhsElementTexts, lhsEmbeddedCompositeParameterSpecification, rhsEmbeddedCompositeParameterSpecification, this);
   }

   protected void translate(int valueElements, int comparisonType, String comparisonText, String[] lhsElementTexts, String[] rhsElementTexts, ParameterSpecification lhsEmbeddedCompositeParameterSpecification, ParameterSpecification rhsEmbeddedCompositeParameterSpecification, AST container) {
      for(int i = valueElements - 1; i > 0; --i) {
         if (i == 1) {
            AST op1 = this.getASTFactory().create(comparisonType, comparisonText);
            AST lhs1 = this.getASTFactory().create(142, lhsElementTexts[0]);
            AST rhs1 = this.getASTFactory().create(142, rhsElementTexts[0]);
            op1.setFirstChild(lhs1);
            lhs1.setNextSibling(rhs1);
            container.setFirstChild(op1);
            AST op2 = this.getASTFactory().create(comparisonType, comparisonText);
            AST lhs2 = this.getASTFactory().create(142, lhsElementTexts[1]);
            AST rhs2 = this.getASTFactory().create(142, rhsElementTexts[1]);
            op2.setFirstChild(lhs2);
            lhs2.setNextSibling(rhs2);
            op1.setNextSibling(op2);
            SqlFragment fragment = (SqlFragment)lhs1;
            if (lhsEmbeddedCompositeParameterSpecification != null) {
               fragment.addEmbeddedParameter(lhsEmbeddedCompositeParameterSpecification);
            }

            if (rhsEmbeddedCompositeParameterSpecification != null) {
               fragment.addEmbeddedParameter(rhsEmbeddedCompositeParameterSpecification);
            }
         } else {
            AST op = this.getASTFactory().create(comparisonType, comparisonText);
            AST lhs = this.getASTFactory().create(142, lhsElementTexts[i]);
            AST rhs = this.getASTFactory().create(142, rhsElementTexts[i]);
            op.setFirstChild(lhs);
            lhs.setNextSibling(rhs);
            AST newContainer = this.getASTFactory().create(6, "AND");
            container.setFirstChild(newContainer);
            newContainer.setNextSibling(op);
            container = newContainer;
         }
      }

   }

   protected static String[] extractMutationTexts(Node operand, int count) {
      if (operand instanceof ParameterNode) {
         String[] rtn = new String[count];
         Arrays.fill(rtn, "?");
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

   protected Type extractDataType(Node operand) {
      Type type = null;
      if (operand instanceof SqlNode) {
         type = ((SqlNode)operand).getDataType();
      }

      if (type == null && operand instanceof ExpectedTypeAwareNode) {
         type = ((ExpectedTypeAwareNode)operand).getExpectedType();
      }

      return type;
   }

   public Type getDataType() {
      return StandardBasicTypes.BOOLEAN;
   }

   public Node getLeftHandOperand() {
      return (Node)this.getFirstChild();
   }

   public Node getRightHandOperand() {
      return (Node)this.getFirstChild().getNextSibling();
   }
}
