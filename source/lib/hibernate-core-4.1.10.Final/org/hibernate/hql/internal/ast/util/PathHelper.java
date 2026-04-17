package org.hibernate.hql.internal.ast.util;

import antlr.ASTFactory;
import antlr.collections.AST;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.StringHelper;
import org.jboss.logging.Logger;

public final class PathHelper {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, PathHelper.class.getName());

   private PathHelper() {
   }

   public static AST parsePath(String path, ASTFactory factory) {
      String[] identifiers = StringHelper.split(".", path);
      AST lhs = null;

      for(int i = 0; i < identifiers.length; ++i) {
         String identifier = identifiers[i];
         AST child = ASTUtil.create(factory, 126, identifier);
         if (i == 0) {
            lhs = child;
         } else {
            lhs = ASTUtil.createBinarySubtree(factory, 15, ".", lhs, child);
         }
      }

      if (LOG.isDebugEnabled()) {
         LOG.debugf("parsePath() : %s -> %s", path, ASTUtil.getDebugString(lhs));
      }

      return lhs;
   }

   public static String getAlias(String path) {
      return StringHelper.root(path);
   }
}
