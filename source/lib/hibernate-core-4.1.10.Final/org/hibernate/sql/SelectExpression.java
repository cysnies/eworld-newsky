package org.hibernate.sql;

public interface SelectExpression {
   String getExpression();

   String getAlias();
}
