package org.hibernate.hql.internal.ast.util;

import antlr.collections.AST;
import java.util.Stack;

public class NodeTraverser {
   private final VisitationStrategy strategy;

   public NodeTraverser(VisitationStrategy strategy) {
      this.strategy = strategy;
   }

   public void traverseDepthFirst(AST ast) {
      if (ast == null) {
         throw new IllegalArgumentException("node to traverse cannot be null!");
      } else {
         this.visitDepthFirst(ast.getFirstChild());
      }
   }

   private void visitDepthFirst(AST ast) {
      if (ast != null) {
         Stack stack = new Stack();
         if (ast != null) {
            stack.push(ast);

            while(!stack.empty()) {
               ast = (AST)stack.pop();
               this.strategy.visit(ast);
               if (ast.getNextSibling() != null) {
                  stack.push(ast.getNextSibling());
               }

               if (ast.getFirstChild() != null) {
                  stack.push(ast.getFirstChild());
               }
            }
         }

      }
   }

   public interface VisitationStrategy {
      void visit(AST var1);
   }
}
