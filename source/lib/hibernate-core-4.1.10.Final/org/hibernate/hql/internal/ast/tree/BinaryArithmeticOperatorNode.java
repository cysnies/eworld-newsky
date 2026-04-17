package org.hibernate.hql.internal.ast.tree;

import antlr.SemanticException;
import java.util.Calendar;
import java.util.Date;
import org.hibernate.hql.internal.ast.util.ColumnHelper;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;

public class BinaryArithmeticOperatorNode extends AbstractSelectExpression implements BinaryOperatorNode, DisplayableNode {
   public void initialize() throws SemanticException {
      Node lhs = this.getLeftHandOperand();
      Node rhs = this.getRightHandOperand();
      if (lhs == null) {
         throw new SemanticException("left-hand operand of a binary operator was null");
      } else if (rhs == null) {
         throw new SemanticException("right-hand operand of a binary operator was null");
      } else {
         Type lhType = lhs instanceof SqlNode ? ((SqlNode)lhs).getDataType() : null;
         Type rhType = rhs instanceof SqlNode ? ((SqlNode)rhs).getDataType() : null;
         if (ExpectedTypeAwareNode.class.isAssignableFrom(lhs.getClass()) && rhType != null) {
            Type expectedType = null;
            if (this.isDateTimeType(rhType)) {
               expectedType = (Type)(this.getType() == 115 ? StandardBasicTypes.DOUBLE : rhType);
            } else {
               expectedType = rhType;
            }

            ((ExpectedTypeAwareNode)lhs).setExpectedType(expectedType);
         } else if (ParameterNode.class.isAssignableFrom(rhs.getClass()) && lhType != null) {
            Type expectedType = null;
            if (this.isDateTimeType(lhType)) {
               if (this.getType() == 115) {
                  expectedType = StandardBasicTypes.DOUBLE;
               }
            } else {
               expectedType = lhType;
            }

            ((ExpectedTypeAwareNode)rhs).setExpectedType(expectedType);
         }

      }
   }

   public Type getDataType() {
      if (super.getDataType() == null) {
         super.setDataType(this.resolveDataType());
      }

      return super.getDataType();
   }

   private Type resolveDataType() {
      Node lhs = this.getLeftHandOperand();
      Node rhs = this.getRightHandOperand();
      Type lhType = lhs instanceof SqlNode ? ((SqlNode)lhs).getDataType() : null;
      Type rhType = rhs instanceof SqlNode ? ((SqlNode)rhs).getDataType() : null;
      if (!this.isDateTimeType(lhType) && !this.isDateTimeType(rhType)) {
         if (lhType == null) {
            return (Type)(rhType == null ? StandardBasicTypes.DOUBLE : rhType);
         } else if (rhType == null) {
            return lhType;
         } else if (lhType != StandardBasicTypes.DOUBLE && rhType != StandardBasicTypes.DOUBLE) {
            if (lhType != StandardBasicTypes.FLOAT && rhType != StandardBasicTypes.FLOAT) {
               if (lhType != StandardBasicTypes.BIG_DECIMAL && rhType != StandardBasicTypes.BIG_DECIMAL) {
                  if (lhType != StandardBasicTypes.BIG_INTEGER && rhType != StandardBasicTypes.BIG_INTEGER) {
                     if (lhType != StandardBasicTypes.LONG && rhType != StandardBasicTypes.LONG) {
                        return (Type)(lhType != StandardBasicTypes.INTEGER && rhType != StandardBasicTypes.INTEGER ? lhType : StandardBasicTypes.INTEGER);
                     } else {
                        return StandardBasicTypes.LONG;
                     }
                  } else {
                     return StandardBasicTypes.BIG_INTEGER;
                  }
               } else {
                  return StandardBasicTypes.BIG_DECIMAL;
               }
            } else {
               return StandardBasicTypes.FLOAT;
            }
         } else {
            return StandardBasicTypes.DOUBLE;
         }
      } else {
         return this.resolveDateTimeArithmeticResultType(lhType, rhType);
      }
   }

   private boolean isDateTimeType(Type type) {
      if (type == null) {
         return false;
      } else {
         return Date.class.isAssignableFrom(type.getReturnedClass()) || Calendar.class.isAssignableFrom(type.getReturnedClass());
      }
   }

   private Type resolveDateTimeArithmeticResultType(Type lhType, Type rhType) {
      boolean lhsIsDateTime = this.isDateTimeType(lhType);
      boolean rhsIsDateTime = this.isDateTimeType(rhType);
      if (this.getType() == 115) {
         return lhsIsDateTime ? lhType : rhType;
      } else {
         if (this.getType() == 116) {
            if (lhsIsDateTime && !rhsIsDateTime) {
               return lhType;
            }

            if (lhsIsDateTime && rhsIsDateTime) {
               return StandardBasicTypes.DOUBLE;
            }
         }

         return null;
      }
   }

   public void setScalarColumnText(int i) throws SemanticException {
      ColumnHelper.generateSingleScalarColumn(this, i);
   }

   public Node getLeftHandOperand() {
      return (Node)this.getFirstChild();
   }

   public Node getRightHandOperand() {
      return (Node)this.getFirstChild().getNextSibling();
   }

   public String getDisplayText() {
      return "{dataType=" + this.getDataType() + "}";
   }
}
