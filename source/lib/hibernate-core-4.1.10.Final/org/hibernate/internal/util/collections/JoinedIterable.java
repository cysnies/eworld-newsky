package org.hibernate.internal.util.collections;

import java.util.Iterator;
import java.util.List;

public class JoinedIterable implements Iterable {
   private final TypeSafeJoinedIterator iterator;

   public JoinedIterable(List iterables) {
      if (iterables == null) {
         throw new NullPointerException("Unexpected null iterables argument");
      } else {
         this.iterator = new TypeSafeJoinedIterator(iterables);
      }
   }

   public Iterator iterator() {
      return this.iterator;
   }

   private class TypeSafeJoinedIterator implements Iterator {
      private List iterables;
      private int currentIterableIndex;
      private Iterator currentIterator;
      private Iterator lastUsedIterator;

      public TypeSafeJoinedIterator(List iterables) {
         this.iterables = iterables;
      }

      public boolean hasNext() {
         this.updateCurrentIterator();
         return this.currentIterator.hasNext();
      }

      public Object next() {
         this.updateCurrentIterator();
         return this.currentIterator.next();
      }

      public void remove() {
         this.updateCurrentIterator();
         this.lastUsedIterator.remove();
      }

      protected void updateCurrentIterator() {
         if (this.currentIterator == null) {
            if (this.iterables.size() == 0) {
               this.currentIterator = EmptyIterator.INSTANCE;
            } else {
               this.currentIterator = ((Iterable)this.iterables.get(0)).iterator();
            }

            this.lastUsedIterator = this.currentIterator;
         }

         while(!this.currentIterator.hasNext() && this.currentIterableIndex < this.iterables.size() - 1) {
            ++this.currentIterableIndex;
            this.currentIterator = ((Iterable)this.iterables.get(this.currentIterableIndex)).iterator();
         }

      }
   }
}
