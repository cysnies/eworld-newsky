package org.hibernate.hql.internal.ast.tree;

import org.hibernate.QueryException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.LiteralType;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;

public class BooleanLiteralNode extends LiteralNode implements ExpectedTypeAwareNode {
   private Type expectedType;

   public Type getDataType() {
      return (Type)(this.expectedType == null ? StandardBasicTypes.BOOLEAN : this.expectedType);
   }

   public Boolean getValue() {
      return this.getType() == 49;
   }

   public void setExpectedType(Type expectedType) {
      this.expectedType = expectedType;
   }

   public Type getExpectedType() {
      return this.expectedType;
   }

   public String getRenderText(SessionFactoryImplementor sessionFactory) {
      try {
         return this.typeAsLiteralType().objectToSQLString(this.getValue(), sessionFactory.getDialect());
      } catch (Throwable t) {
         throw new QueryException("Unable to render boolean literal value", t);
      }
   }

   private LiteralType typeAsLiteralType() {
      return (LiteralType)this.getDataType();
   }
}
