package org.hibernate.hql.internal.ast.tree;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.param.ParameterSpecification;
import org.hibernate.type.Type;

public class ParameterNode extends HqlSqlWalkerNode implements DisplayableNode, ExpectedTypeAwareNode {
   private ParameterSpecification parameterSpecification;

   public ParameterSpecification getHqlParameterSpecification() {
      return this.parameterSpecification;
   }

   public void setHqlParameterSpecification(ParameterSpecification parameterSpecification) {
      this.parameterSpecification = parameterSpecification;
   }

   public String getDisplayText() {
      return "{" + (this.parameterSpecification == null ? "???" : this.parameterSpecification.renderDisplayInfo()) + "}";
   }

   public void setExpectedType(Type expectedType) {
      this.getHqlParameterSpecification().setExpectedType(expectedType);
      this.setDataType(expectedType);
   }

   public Type getExpectedType() {
      return this.getHqlParameterSpecification() == null ? null : this.getHqlParameterSpecification().getExpectedType();
   }

   public String getRenderText(SessionFactoryImplementor sessionFactory) {
      int count = 0;
      if (this.getExpectedType() != null && (count = this.getExpectedType().getColumnSpan(sessionFactory)) > 1) {
         StringBuilder buffer = new StringBuilder();
         buffer.append("(?");

         for(int i = 1; i < count; ++i) {
            buffer.append(", ?");
         }

         buffer.append(")");
         return buffer.toString();
      } else {
         return "?";
      }
   }
}
