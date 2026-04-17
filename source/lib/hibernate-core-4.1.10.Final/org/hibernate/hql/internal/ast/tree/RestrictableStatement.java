package org.hibernate.hql.internal.ast.tree;

import antlr.collections.AST;

public interface RestrictableStatement extends Statement {
   FromClause getFromClause();

   boolean hasWhereClause();

   AST getWhereClause();
}
