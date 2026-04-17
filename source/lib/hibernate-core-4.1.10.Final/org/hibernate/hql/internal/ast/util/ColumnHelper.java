package org.hibernate.hql.internal.ast.util;

import antlr.ASTFactory;
import antlr.collections.AST;
import org.hibernate.hql.internal.NameGenerator;
import org.hibernate.hql.internal.ast.tree.HqlSqlWalkerNode;

public final class ColumnHelper {
   /** @deprecated */
   private ColumnHelper() {
   }

   public static void generateSingleScalarColumn(HqlSqlWalkerNode node, int i) {
      ASTFactory factory = node.getASTFactory();
      ASTUtil.createSibling(factory, 143, " as " + NameGenerator.scalarName(i, 0), node);
   }

   public static void generateScalarColumns(HqlSqlWalkerNode node, String[] sqlColumns, int i) {
      if (sqlColumns.length == 1) {
         generateSingleScalarColumn(node, i);
      } else {
         ASTFactory factory = node.getASTFactory();
         AST n = node;
         node.setText(sqlColumns[0]);

         for(int j = 0; j < sqlColumns.length; ++j) {
            if (j > 0) {
               n = ASTUtil.createSibling(factory, 142, sqlColumns[j], n);
            }

            n = ASTUtil.createSibling(factory, 143, " as " + NameGenerator.scalarName(i, j), n);
         }
      }

   }
}
