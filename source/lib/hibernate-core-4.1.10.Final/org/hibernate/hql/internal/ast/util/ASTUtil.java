package org.hibernate.hql.internal.ast.util;

import antlr.ASTFactory;
import antlr.collections.AST;
import antlr.collections.impl.ASTArray;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ASTUtil {
   /** @deprecated */
   private ASTUtil() {
   }

   /** @deprecated */
   public static AST create(ASTFactory astFactory, int type, String text) {
      return astFactory.create(type, text);
   }

   public static AST createSibling(ASTFactory astFactory, int type, String text, AST prevSibling) {
      AST node = astFactory.create(type, text);
      return insertSibling(node, prevSibling);
   }

   public static AST insertSibling(AST node, AST prevSibling) {
      node.setNextSibling(prevSibling.getNextSibling());
      prevSibling.setNextSibling(node);
      return node;
   }

   public static AST createBinarySubtree(ASTFactory factory, int parentType, String parentText, AST child1, AST child2) {
      ASTArray array = createAstArray(factory, 3, parentType, parentText, child1);
      array.add(child2);
      return factory.make(array);
   }

   public static AST createParent(ASTFactory factory, int parentType, String parentText, AST child) {
      ASTArray array = createAstArray(factory, 2, parentType, parentText, child);
      return factory.make(array);
   }

   public static AST createTree(ASTFactory factory, AST[] nestedChildren) {
      AST[] array = new AST[2];
      int limit = nestedChildren.length - 1;

      for(int i = limit; i >= 0; --i) {
         if (i != limit) {
            array[1] = nestedChildren[i + 1];
            array[0] = nestedChildren[i];
            factory.make(array);
         }
      }

      return array[0];
   }

   public static boolean isSubtreeChild(AST fixture, AST test) {
      for(AST n = fixture.getFirstChild(); n != null; n = n.getNextSibling()) {
         if (n == test) {
            return true;
         }

         if (n.getFirstChild() != null && isSubtreeChild(n, test)) {
            return true;
         }
      }

      return false;
   }

   public static AST findTypeInChildren(AST parent, int type) {
      AST n;
      for(n = parent.getFirstChild(); n != null && n.getType() != type; n = n.getNextSibling()) {
      }

      return n;
   }

   public static AST getLastChild(AST n) {
      return getLastSibling(n.getFirstChild());
   }

   private static AST getLastSibling(AST a) {
      AST last;
      for(last = null; a != null; a = a.getNextSibling()) {
         last = a;
      }

      return last;
   }

   public static String getDebugString(AST n) {
      StringBuilder buf = new StringBuilder();
      buf.append("[ ");
      buf.append(n == null ? "{null}" : n.toStringTree());
      buf.append(" ]");
      return buf.toString();
   }

   public static AST findPreviousSibling(AST parent, AST child) {
      AST prev = null;

      for(AST n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
         if (n == child) {
            return prev;
         }

         prev = n;
      }

      throw new IllegalArgumentException("Child not found in parent!");
   }

   public static void makeSiblingOfParent(AST parent, AST child) {
      AST prev = findPreviousSibling(parent, child);
      if (prev != null) {
         prev.setNextSibling(child.getNextSibling());
      } else {
         parent.setFirstChild(child.getNextSibling());
      }

      child.setNextSibling(parent.getNextSibling());
      parent.setNextSibling(child);
   }

   public static String getPathText(AST n) {
      StringBuilder buf = new StringBuilder();
      getPathText(buf, n);
      return buf.toString();
   }

   private static void getPathText(StringBuilder buf, AST n) {
      AST firstChild = n.getFirstChild();
      if (firstChild != null) {
         getPathText(buf, firstChild);
      }

      buf.append(n.getText());
      if (firstChild != null && firstChild.getNextSibling() != null) {
         getPathText(buf, firstChild.getNextSibling());
      }

   }

   public static boolean hasExactlyOneChild(AST n) {
      return n != null && n.getFirstChild() != null && n.getFirstChild().getNextSibling() == null;
   }

   public static void appendSibling(AST n, AST s) {
      while(n.getNextSibling() != null) {
         n = n.getNextSibling();
      }

      n.setNextSibling(s);
   }

   public static void insertChild(AST parent, AST child) {
      if (parent.getFirstChild() == null) {
         parent.setFirstChild(child);
      } else {
         AST n = parent.getFirstChild();
         parent.setFirstChild(child);
         child.setNextSibling(n);
      }

   }

   private static ASTArray createAstArray(ASTFactory factory, int size, int parentType, String parentText, AST child1) {
      ASTArray array = new ASTArray(size);
      array.add(factory.create(parentType, parentText));
      array.add(child1);
      return array;
   }

   public static List collectChildren(AST root, FilterPredicate predicate) {
      return (new CollectingNodeVisitor(predicate)).collect(root);
   }

   public static Map generateTokenNameCache(Class tokenTypeInterface) {
      Field[] fields = tokenTypeInterface.getFields();
      Map cache = new HashMap((int)((double)fields.length * (double)0.75F) + 1);

      for(int i = 0; i < fields.length; ++i) {
         Field field = fields[i];
         if (Modifier.isStatic(field.getModifiers())) {
            try {
               cache.put(field.get((Object)null), field.getName());
            } catch (Throwable var6) {
            }
         }
      }

      return cache;
   }

   /** @deprecated */
   public static String getConstantName(Class owner, int value) {
      return getTokenTypeName(owner, value);
   }

   public static String getTokenTypeName(Class tokenTypeInterface, int tokenType) {
      String tokenTypeName = Integer.toString(tokenType);
      if (tokenTypeInterface != null) {
         Field[] fields = tokenTypeInterface.getFields();

         for(int i = 0; i < fields.length; ++i) {
            Integer fieldValue = extractIntegerValue(fields[i]);
            if (fieldValue != null && fieldValue == tokenType) {
               tokenTypeName = fields[i].getName();
               break;
            }
         }
      }

      return tokenTypeName;
   }

   private static Integer extractIntegerValue(Field field) {
      Integer rtn = null;

      try {
         Object value = field.get((Object)null);
         if (value instanceof Integer) {
            rtn = (Integer)value;
         } else if (value instanceof Short) {
            rtn = ((Short)value).intValue();
         } else if (value instanceof Long && (Long)value <= 2147483647L) {
            rtn = ((Long)value).intValue();
         }
      } catch (IllegalAccessException var3) {
      }

      return rtn;
   }

   public abstract static class IncludePredicate implements FilterPredicate {
      public final boolean exclude(AST node) {
         return !this.include(node);
      }

      public abstract boolean include(AST var1);
   }

   private static class CollectingNodeVisitor implements NodeTraverser.VisitationStrategy {
      private final FilterPredicate predicate;
      private final List collectedNodes = new ArrayList();

      public CollectingNodeVisitor(FilterPredicate predicate) {
         this.predicate = predicate;
      }

      public void visit(AST node) {
         if (this.predicate == null || !this.predicate.exclude(node)) {
            this.collectedNodes.add(node);
         }

      }

      public List getCollectedNodes() {
         return this.collectedNodes;
      }

      public List collect(AST root) {
         NodeTraverser traverser = new NodeTraverser(this);
         traverser.traverseDepthFirst(root);
         return this.collectedNodes;
      }
   }

   public interface FilterPredicate {
      boolean exclude(AST var1);
   }
}
