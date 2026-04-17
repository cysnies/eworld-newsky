package org.hibernate.hql.internal.ast.tree;

import org.hibernate.hql.internal.ast.HqlSqlWalker;

public interface Statement {
   HqlSqlWalker getWalker();

   int getStatementType();

   boolean needsExecutor();
}
