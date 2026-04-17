package org.hibernate.hql.internal.ast.tree;

import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.type.Type;

public interface FunctionNode {
   SQLFunction getSQLFunction();

   Type getFirstArgumentType();
}
