package org.hibernate.hql.internal.ast.tree;

import org.hibernate.QueryException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.type.LiteralType;
import org.hibernate.type.Type;

public class JavaConstantNode extends Node implements ExpectedTypeAwareNode, SessionFactoryAwareNode {
   private SessionFactoryImplementor factory;
   private String constantExpression;
   private Object constantValue;
   private Type heuristicType;
   private Type expectedType;

   public void setText(String s) {
      if (StringHelper.isNotEmpty(s)) {
         this.constantExpression = s;
         this.constantValue = ReflectHelper.getConstantValue(s);
         this.heuristicType = this.factory.getTypeResolver().heuristicType(this.constantValue.getClass().getName());
         super.setText(s);
      }

   }

   public void setExpectedType(Type expectedType) {
      this.expectedType = expectedType;
   }

   public Type getExpectedType() {
      return this.expectedType;
   }

   public void setSessionFactory(SessionFactoryImplementor factory) {
      this.factory = factory;
   }

   public String getRenderText(SessionFactoryImplementor sessionFactory) {
      Type type = this.expectedType == null ? this.heuristicType : (Number.class.isAssignableFrom(this.heuristicType.getReturnedClass()) ? this.heuristicType : this.expectedType);

      try {
         LiteralType literalType = (LiteralType)type;
         Dialect dialect = this.factory.getDialect();
         return literalType.objectToSQLString(this.constantValue, dialect);
      } catch (Throwable t) {
         throw new QueryException("Could not format constant value to SQL literal: " + this.constantExpression, t);
      }
   }
}
