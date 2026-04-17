package org.hibernate.hql.internal.ast.util;

import antlr.ASTFactory;
import antlr.collections.AST;

public class ASTAppender {
   private AST parent;
   private AST last;
   private ASTFactory factory;

   public ASTAppender(ASTFactory factory, AST parent) {
      this(parent);
      this.factory = factory;
   }

   public ASTAppender(AST parent) {
      this.parent = parent;
      this.last = ASTUtil.getLastChild(parent);
   }

   public AST append(int type, String text, boolean appendIfEmpty) {
      return text == null || !appendIfEmpty && text.length() <= 0 ? null : this.append(this.factory.create(type, text));
   }

   public AST append(AST child) {
      if (this.last == null) {
         this.parent.setFirstChild(child);
      } else {
         this.last.setNextSibling(child);
      }

      this.last = child;
      return this.last;
   }
}
