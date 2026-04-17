package org.hibernate.hql.internal.ast.util;

import antlr.collections.AST;
import java.util.Iterator;
import java.util.LinkedList;

public class ASTParentsFirstIterator implements Iterator {
   private AST next;
   private AST current;
   private AST tree;
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

   public ASTParentsFirstIterator(AST tree) {
      this.tree = this.next = tree;
   }

   public AST nextNode() {
      this.current = this.next;
      if (this.next != null) {
         AST child = this.next.getFirstChild();
         if (child == null) {
            AST sibling = this.next.getNextSibling();
            if (sibling == null) {
               AST parent;
               for(parent = this.pop(); parent != null && parent.getNextSibling() == null; parent = this.pop()) {
               }

               this.next = parent != null ? parent.getNextSibling() : null;
            } else {
               this.next = sibling;
            }
         } else {
            if (this.next != this.tree) {
               this.push(this.next);
            }

            this.next = child;
         }
      }

      return this.current;
   }

   private void push(AST parent) {
      this.parents.addFirst(parent);
   }

   private AST pop() {
      return this.parents.size() == 0 ? null : (AST)this.parents.removeFirst();
   }
}
