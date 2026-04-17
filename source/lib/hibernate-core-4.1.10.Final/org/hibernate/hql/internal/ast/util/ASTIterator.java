package org.hibernate.hql.internal.ast.util;

import antlr.collections.AST;
import java.util.Iterator;
import java.util.LinkedList;

public class ASTIterator implements Iterator {
   private AST next;
   private AST current;
   private LinkedList parents = new LinkedList();

   public void remove() {
      throw new UnsupportedOperationException("remove() is not supported");
   }

   public boolean hasNext() {
      return this.next != null;
   }

   public Object next() {
      return this.nextNode();
   }

   public ASTIterator(AST tree) {
      this.next = tree;
      this.down();
   }

   public AST nextNode() {
      this.current = this.next;
      if (this.next != null) {
         AST nextSibling = this.next.getNextSibling();
         if (nextSibling == null) {
            this.next = this.pop();
         } else {
            this.next = nextSibling;
            this.down();
         }
      }

      return this.current;
   }

   private void down() {
      while(this.next != null && this.next.getFirstChild() != null) {
         this.push(this.next);
         this.next = this.next.getFirstChild();
      }

   }

   private void push(AST parent) {
      this.parents.addFirst(parent);
   }

   private AST pop() {
      return this.parents.size() == 0 ? null : (AST)this.parents.removeFirst();
   }
}
