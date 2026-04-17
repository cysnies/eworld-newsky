package org.hibernate.hql.internal.ast.tree;

import antlr.collections.AST;
import org.hibernate.hql.internal.antlr.HqlSqlTokenTypes;
import org.hibernate.hql.internal.ast.util.ASTUtil;

public class OrderByClause extends HqlSqlWalkerNode implements HqlSqlTokenTypes {
   public void addOrderFragment(String orderByFragment) {
      AST fragment = ASTUtil.create(this.getASTFactory(), 142, orderByFragment);
      if (this.getFirstChild() == null) {
         this.setFirstChild(fragment);
      } else {
         this.addChild(fragment);
      }

   }
}
